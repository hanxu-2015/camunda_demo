package com.acrel.camunda.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

	@GetMapping("login")
	public String index() {
		return "login";
	}

	@PostMapping("login")
	public String login(@RequestParam("name") String name, HttpSession session) {
		session.setAttribute("user", name);
		return "redirect:/";
	}

}
