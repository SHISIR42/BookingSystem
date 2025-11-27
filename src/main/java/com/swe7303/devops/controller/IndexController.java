package com.swe7303.devops.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class IndexController {
	@GetMapping("/index")
	public String index() {
		return "index";
	}

	// Unified dashboard entry point - routes based on role
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {
		String username = (String) session.getAttribute("username");
		String role = (String) session.getAttribute("userRole");

		// If not logged in, redirect to login
		if (username == null || role == null) {
			return "redirect:/login";
		}

		// Route based on role
		if ("ADMIN".equals(role)) {
			return "redirect:/admin-dashboard";
		} else if ("CUSTOMER".equals(role)) {
			return "redirect:/customer-dashboard";
		}

		// Default fallback
		return "redirect:/login";
	}
}
