package com.acrel.camunda.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

class Util {

	private Util() {
		super();
	}

	public static final String getUser(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			return null;
		}
		return (String) session.getAttribute("user");
	}
}
