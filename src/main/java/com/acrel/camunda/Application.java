package com.acrel.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.form.type.DateFormType;
import org.camunda.bpm.engine.impl.form.type.FormTypes;
import org.camunda.bpm.engine.impl.form.type.LongFormType;
import org.camunda.bpm.engine.impl.form.type.StringFormType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {
	public static void main(String... args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
		ProcessEngine engine = ctx.getBean(ProcessEngine.class);
		ProcessEngineConfigurationImpl impl = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
		FormTypes ft = new FormTypes();
		ft.addFormType(new StringFormType());
		ft.addFormType(new LongFormType());
		ft.addFormType(new DateFormType("yyyy-MM-dd"));
		impl.setFormTypes(ft);
		impl.getFormEngines().put(AcrelFormEngine.INSTANCE.getName(), AcrelFormEngine.INSTANCE);
	}
}