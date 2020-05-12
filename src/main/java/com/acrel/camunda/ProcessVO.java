package com.acrel.camunda;

import org.camunda.bpm.engine.history.HistoricProcessInstance;

public class ProcessVO {

	private final HistoricProcessInstance instance;
	private final String status;

	public ProcessVO(HistoricProcessInstance instance, String status) {
		super();
		this.instance = instance;
		this.status = status;
	}

	public HistoricProcessInstance getInstance() {
		return instance;
	}

	public synchronized String getStatus() {
		return status;
	}

}
