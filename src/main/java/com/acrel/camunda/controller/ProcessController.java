package com.acrel.camunda.controller;

import com.acrel.camunda.AcrelFormEngine;
import com.acrel.camunda.ProcessFlowVO;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProcessController {
    private final ProcessEngine engine;

    public ProcessController(ProcessEngine engine) {
        super();
        this.engine = engine;
    }

    @GetMapping("startProcess")
    public String start(@RequestParam("id") String id, Model model) {
        model.addAttribute("processDefinitionId", id);
        model.addAttribute("form",
                engine.getFormService().getRenderedStartForm(id, AcrelFormEngine.INSTANCE.getName()));
        return "start";
    }

    @PostMapping("startProcess")
    public String start(@RequestParam("processDefinitionId") String processDefinitionId,
                        @RequestParam Map<String, Object> map, HttpServletRequest req) {
        map.remove("processDefinitionId");
        engine.getFormService().submitStartForm(processDefinitionId, map);
        return "redirect:/";
    }

    @GetMapping("startTask")
    public String task(@RequestParam("id") String id, Model model) {
        model.addAttribute("taskId", id);
        model.addAttribute("form", engine.getFormService().getRenderedTaskForm(id, AcrelFormEngine.INSTANCE.getName()));
        return "task";
    }

    @PostMapping("startTask")
    public String task(@RequestParam("taskId") String taskId, @RequestParam Map<String, Object> map) {
        String output = (String) map.remove("_output");
        HistoricTaskInstance task = engine.getHistoryService().createHistoricTaskInstanceQuery().taskId(taskId)
                .singleResult();

        List<SubProcess> subProcesses = getAllSubProcesses(engine, task);
        for (SubProcess subProcess : subProcesses) {
            map.put(subProcess.getId(), output);
        }

        map.put(task.getTaskDefinitionKey(), output);
        map.put(task.getProcessDefinitionKey(), output);
        map.put(task.getId(), output);
        String comment = (String) map.remove("comment");
        if (StringUtils.hasText(comment))
            engine.getTaskService().createComment(taskId, task.getProcessInstanceId(), comment);
        engine.getFormService().submitTaskForm(taskId, map);
        return "redirect:/";
    }

    private List<SubProcess> getAllSubProcesses(ProcessEngine engine, HistoricTaskInstance task) {
        List<SubProcess> list = new ArrayList<>();
        BpmnModelInstance instance = engine.getRepositoryService().getBpmnModelInstance(task.getProcessDefinitionId());
        ModelElementInstance taskInstance = instance.getModelElementById(task.getTaskDefinitionKey());
        ModelElementInstance parent = taskInstance.getParentElement();
        while (parent != null) {
            if (parent instanceof SubProcess) {
                list.add((SubProcess) parent);
            }
            parent = parent.getParentElement();
        }
        return list;
    }

    @GetMapping("process/{id}/flow")
    @ResponseBody
    public List<ProcessFlowVO> processFlow(@PathVariable("id") String id) {
        List<ProcessFlowVO> result = new ArrayList<>();
        HistoryService hs = engine.getHistoryService();
        List<HistoricTaskInstance> list = hs.createHistoricTaskInstanceQuery().processInstanceId(id)
                .orderByHistoricActivityInstanceStartTime().asc().list();
        for (HistoricTaskInstance ins : list) {
            if (ins.getAssignee() == null)
                continue;
            ProcessFlowVO vo = new ProcessFlowVO();
            vo.setStartTime(ins.getStartTime());
            vo.setEndTime(ins.getEndTime());
            vo.setAssignee(ins.getAssignee());
            vo.setName(ins.getName());
            HistoricVariableInstance variable = engine.getHistoryService().createHistoricVariableInstanceQuery()
                    .variableName(ins.getId()).singleResult();

            if (variable != null) {
                vo.setStatus(variable.getValue().toString());
            }

            vo.setComments(engine.getTaskService().getTaskComments(ins.getId()).stream().map(Comment::getFullMessage)
                    .collect(Collectors.toList()));

            result.add(vo);
        }

        return result;
    }

}
