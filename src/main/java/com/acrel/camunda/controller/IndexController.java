package com.acrel.camunda.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.acrel.camunda.ProcessVO;
import com.acrel.camunda.TaskVO;

@Controller
public class IndexController {

	private final ProcessEngine engine;

	public IndexController(ProcessEngine engine) {
		super();
		this.engine = engine;
	}

	@GetMapping
	public String index(HttpServletRequest req, Model model) {
		String name = Util.getUser(req);
		if (name == null)
			return "redirect:/login";
		List<ProcessDefinition> definitions = engine.getRepositoryService().createProcessDefinitionQuery().active()
				.list();
		model.addAttribute("definitions", definitions);

		model.addAttribute("tasks", queryTasks(name));
		model.addAttribute("historicTasks", queryHistoryTasks(name));
		model.addAttribute("historicProcesses", queryMyProcess(name));
		return "index";
	}

	private List<TaskVO> queryTasks(String name) {
		HistoricTaskInstanceQuery query = engine.getHistoryService().createHistoricTaskInstanceQuery()
				.taskAssignee(name).unfinished().orderByHistoricActivityInstanceStartTime().asc();
		List<HistoricTaskInstance> instances = query.list();
		List<TaskVO> list = new ArrayList<>(instances.size());
		for (HistoricTaskInstance instance : instances) {
			TaskVO taskVO = new TaskVO(instance);
			HistoricProcessInstance processInstance = engine.getHistoryService().createHistoricProcessInstanceQuery()
					.processInstanceId(instance.getProcessInstanceId()).singleResult();
			taskVO.setBusinessKey(processInstance.getBusinessKey());
			list.add(taskVO);
		}
		return list;
	}

	private List<ProcessVO> queryMyProcess(String name) {
		HistoricProcessInstanceQuery query = engine.getHistoryService().createHistoricProcessInstanceQuery()
				.startedBy(name);
		List<HistoricProcessInstance> instances = query.list();
		List<ProcessVO> vos = new ArrayList<>(instances.size());
		for (HistoricProcessInstance instance : instances) {
			if (instance.getEndTime() != null) {
				List<HistoricVariableInstance> variables = engine.getHistoryService()
						.createHistoricVariableInstanceQuery().processInstanceId(instance.getId())
						.variableName(instance.getProcessDefinitionKey() + "_output").list();
				if (!variables.isEmpty()) {
					HistoricVariableInstance last = variables.get(variables.size() - 1);
					vos.add(new ProcessVO(instance, (String) last.getValue()));
					continue;
				}
			}
			vos.add(new ProcessVO(instance, null));
		}
		return vos;
	}

	private List<TaskVO> queryHistoryTasks(String name) {
		HistoricTaskInstanceQuery query = engine.getHistoryService().createHistoricTaskInstanceQuery()
				.taskAssignee(name).finished().orderByHistoricActivityInstanceStartTime().desc();
		List<HistoricTaskInstance> instances = query.list();
		List<TaskVO> list = new ArrayList<>(instances.size());
		for (HistoricTaskInstance instance : instances) {
			TaskVO taskVO = new TaskVO(instance);
			HistoricProcessInstance processInstance = engine.getHistoryService().createHistoricProcessInstanceQuery()
					.processInstanceId(instance.getProcessInstanceId()).singleResult();
			HistoricVariableInstance variable = engine.getHistoryService().createHistoricVariableInstanceQuery()
					.variableName(instance.getId()).singleResult();
			if (variable != null) {
				taskVO.setStatus(variable.getValue().toString());
			}
			taskVO.setBusinessKey(processInstance.getBusinessKey());
			list.add(taskVO);
		}
		return list;
	}
}
