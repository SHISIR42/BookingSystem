package com.swe7303.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.swe7303.devops.model.User;
import com.swe7303.devops.service.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	@Autowired
	private UserService userService;

	@GetMapping({ "/login", "/" })
	public String getLogin() {

		return "login";

	}

	@PostMapping("/login")
	public String postLogin(@ModelAttribute User user, String loginRole, Model model, HttpSession session) {
		User usr = userService.userLogin(user.getEmail(), user.getPassword());
		if (usr != null) {
			// Validate that the selected role matches the user's actual role
			if (!usr.getRole().equalsIgnoreCase(loginRole)) {
				model.addAttribute("error", "Invalid credentials for the selected role!");
				return "login";
			}

			session.setAttribute("loggedInUser", usr);
			session.setAttribute("username", usr.getUsername());
			session.setAttribute("userId", usr.getId());
			session.setAttribute("userRole", usr.getRole());
			session.setAttribute("userEmail", usr.getEmail());

			// Redirect to unified dashboard - it will route based on role
			return "redirect:/dashboard";
		}
		model.addAttribute("error", "Sorry, user not found!");
		return "login";
	}

	@GetMapping("/home")
	public String getHome(HttpSession session, Model model) {
		// Redirect to unified dashboard
		return "redirect:/dashboard";
	}

	@GetMapping("/admin-dashboard")
	public String getAdminDashboard(HttpSession session, Model model) {
		String username = (String) session.getAttribute("username");
		if (username == null) {
			return "redirect:/login";
		}
		model.addAttribute("un", username);
		return "admin_dashboard";
	}

	@GetMapping("/customer-dashboard")
	public String getCustomerDashboard(HttpSession session, Model model) {
		String username = (String) session.getAttribute("username");
		if (username == null) {
			return "redirect:/login";
		}
		model.addAttribute("un", username);
		return "customer_dashboard";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login";
	}

	// READ - List all users
	@GetMapping("/users")
	public String listUsers(Model model) {
		model.addAttribute("users", userService.getAllUsers());
		return "users";
	}

	// UPDATE - Show edit form
	@GetMapping("/edit-user/{id}")
	public String editUserForm(@PathVariable("id") Integer id, Model model) {
		User user = userService.getUserById(id);
		model.addAttribute("user", user);
		return "edit_user";
	}

	// UPDATE - Save edited user
	@PostMapping("/update-user/{id}")
	public String updateUser(@PathVariable("id") Integer id,
			@ModelAttribute("user") User user) {
		user.setId(id);
		userService.userSignup(user); // Reuse signup method to save
		return "redirect:/users";
	}

	// DELETE - Remove user
	@GetMapping("/delete-user/{id}")
	public String deleteUser(@PathVariable("id") Integer id) {
		userService.deleteUser(id);
		return "redirect:/users";
	}

	@GetMapping("/signup")
	public String getSignup() {
		return "signup";
	}

	@PostMapping("/signup")
	public String postSignup(@ModelAttribute User user) {
		if (user.getRole() == null || user.getRole().isEmpty()) {
			user.setRole("CUSTOMER"); // Default role
		}
		userService.userSignup(user);
		return "redirect:/login";

	}

}
