package com.acrel.camunda;

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Task;

public class TaskVO {

	private String taskId;
	private Object data;
	/**
	 * 任务完成时间
	 */
	private Date endTime;

	/**
	 * 任务开始时间
	 */
	private Date startTime;

	/**
	 * 任务完成状态
	 */
	private String status;

	private String businessKey;

	public TaskVO() {
		super();
	}

	public TaskVO(Task task) {
		this.taskId = task.getId();
	}

	public TaskVO(HistoricTaskInstance instance) {
		this.taskId = instance.getId();
		this.endTime = instance.getEndTime();
		this.startTime = instance.getStartTime();
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

}
