package com.acrel.camunda.controller;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
public class Config {

	private static final AntPathMatcher apm = new AntPathMatcher();

	private final ProcessEngine engine;

	public Config(ProcessEngine engine) {
		super();
		this.engine = engine;
	}

	@Bean
	public FilterRegistrationBean<Filter> filterRegist() {
		FilterRegistrationBean<Filter> frBean = new FilterRegistrationBean<>();
		frBean.setFilter(new Filter() {

			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
					throws IOException, ServletException {
				HttpServletRequest req = (HttpServletRequest) request;
				String name = null;
				if (apm.match("/static/**", req.getRequestURI()) || req.getRequestURI().indexOf("/login") != -1
						|| (name = Util.getUser(req)) != null) {
					if (name != null) {
						engine.getIdentityService().setAuthenticatedUserId(name);
					}
					try {
						chain.doFilter(request, response);
					} finally {
						if (name != null) {
							engine.getIdentityService().clearAuthentication();
						}
					}
					return;
				}
				HttpServletResponse resp = (HttpServletResponse) response;
				resp.sendRedirect("/login");
			}
		});
		frBean.addUrlPatterns("/*");
		frBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
		return frBean;
	}

}
