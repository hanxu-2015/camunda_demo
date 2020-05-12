package com.acrel.camunda;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.form.engine.FormEngine;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class AcrelFormEngine implements FormEngine {

	private static final String NAME = AcrelFormEngine.class.getName();
	private final ObjectMapper mapper = new ObjectMapper();

	private static final String formJson;

	static {
		try (InputStream is = new ClassPathResource("form.json").getInputStream()) {
			formJson = new String(is.readAllBytes(), "utf8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private AcrelFormEngine() {
		super();
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Object renderStartForm(StartFormData startForm) {
		return getJson(startForm.getFormKey(), null);
	}

	@Override
	public Object renderTaskForm(TaskFormData taskForm) {
		if (!StringUtils.hasText(taskForm.getFormKey()))
			return null;
		CommandContext commandContext = Context.getCommandContext();

		List<VariableInstanceEntity> entities = commandContext.getVariableInstanceManager()
				.findVariableInstancesByProcessInstanceId(taskForm.getTask().getProcessInstanceId());

		Map<String, Object> variables = new HashMap<>();

		for (VariableInstanceEntity entity : entities) {
			variables.put(entity.getName(), entity.getValue());
		}

		return getJson(taskForm.getFormKey(), variables);
	}

	public static final AcrelFormEngine INSTANCE = new AcrelFormEngine();

	private String getJson(String formKey, Map<String, Object> variables) {
		if (variables == null || variables.isEmpty())
			return formJson;
		JsonNode array;
		try {
			array = mapper.readTree(formJson);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (JsonNode node : array) {
			JsonNode nameNode = node.get("name");
			if (nameNode != null) {
				String name = nameNode.asText();
				Object variable = variables.get(name);
				if (variable != null) {
					((ObjectNode) node).set("value", new TextNode(Objects.toString(variable)));
				}
			}
		}
		try {
			return mapper.writeValueAsString(array);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
