package com.acrel.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.model.bpmn.instance.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class V {

    public final ProcessEngine processEngine;

    public V(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public boolean rate(double rate) {
        Execution e = Context.getBpmnExecutionContext().getExecution();
        Calc calc = getCalc((ExecutionEntity) e);
        return calc.rate(rate);
    }

    private Calc getCalc(ExecutionEntity execution) {
        Gateway gateway = getGateway(execution);
        Calc calc = new Calc();
        for (SequenceFlow flow : gateway.getIncoming()) {
            FlowNode node = flow.getSource();
            if (node instanceof Task) {
                calc.results.add(getTaskResult(flow, execution));
                break;
            }
            if (node instanceof ParallelGateway) {
                for (SequenceFlow pflow : node.getIncoming()) {
                    calc.results.add(getTaskResult(pflow, execution));
                }
            }
        }
        calc.processInstanceId = execution.getProcessInstanceId();
        return calc;
    }

    private Gateway getGateway(ExecutionEntity execution) {
        FlowElement flowElement = execution.getBpmnModelElementInstance();
        if (!(flowElement instanceof Gateway)) {
            if (flowElement instanceof SequenceFlow) {
                flowElement = ((SequenceFlow) flowElement).getSource();
            }
        }

        if (flowElement instanceof Gateway) {
            return (Gateway) flowElement;
        }

        throw new RuntimeException("表达式必须指向一个网关");
    }

    private String getTaskResult(SequenceFlow flow, Execution execution) {
        FlowNode node = flow.getSource();
        if (!(node instanceof Task)) {
            return null;
        }
        Task task = (Task) node;
        org.camunda.bpm.engine.task.Task dbTask = processEngine.getTaskService().createTaskQuery().active().taskDefinitionKey(task.getId()).singleResult();
        if (dbTask != null) {
            Object variable = processEngine.getTaskService().getVariable(dbTask.getId(), dbTask.getId());
            return Objects.toString(variable, null);
        }
        List<HistoricTaskInstance> taskInstances = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .taskDefinitionKey(task.getId())
                .processInstanceId(execution.getProcessInstanceId())
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .listPage(0, 1);

        if (!taskInstances.isEmpty()) {
            HistoricVariableInstance variableInstance = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName(taskInstances.get(0).getId()).singleResult();
            if (variableInstance != null) {
                return Objects.toString(variableInstance.getValue(), null);
            }
        }
        return null;
    }

    private final class Calc {
        private String processInstanceId;
        private List<String> results = new ArrayList<>();

        public boolean rate(double rate) {
            if (results.isEmpty()) {
                return false;
            }
            boolean flag = (results.stream().filter(s -> "approve".equals(s)).count() / results.size()) > rate;

            String value = flag ? "approve" : "reject";
            processEngine.getRuntimeService().setVariable(processInstanceId, processInstanceId, value);
            return flag;
        }
    }
}
