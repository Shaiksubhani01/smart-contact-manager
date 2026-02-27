package com.contactManager.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contactManager.entities.User;
import com.contactManager.helper.Message;
import com.contactManager.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class HomeController {

	private static final Logger log = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	// ================= HOME =================
	@GetMapping("/")
	public String home(Model model) {
		log.info("Home page requested");
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	// ================= SIGNUP PAGE =================
	@GetMapping("/signup")
	public String signup(Model model) {
		log.info("Signup page opened");
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// ================= REGISTER USER =================
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(defaultValue = "false") boolean agreement, Model model, HttpSession session) {

		log.info("Registration attempt for email: {}", user.getEmail());

		// 1️⃣ Validation errors
		if (result.hasErrors()) {
			log.warn("Validation errors during registration for email: {}", user.getEmail());
			return "signup";
		}

		// 2️⃣ Terms not accepted
		if (!agreement) {
			log.warn("Terms not accepted by user: {}", user.getEmail());
			session.setAttribute("message", new Message("Please accept Terms & Conditions", "alert-danger"));
			return "signup";
		}

		// 3️⃣ Duplicate email check
		if (userRepository.findByEmail(user.getEmail()).isPresent()) {
			log.warn("Duplicate registration attempt for email: {}", user.getEmail());
			session.setAttribute("message", new Message("User already exists with this email", "alert-warning"));
			return "signup";
		}

		try {
			// 4️⃣ Save user
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

			userRepository.save(user);

			log.info("User registered successfully: {}", user.getEmail());

			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully registered! Please login.", "alert-success"));

		} catch (Exception e) {
			log.error("Error during user registration for email: {}", user.getEmail(), e);
			session.setAttribute("message", new Message("Something went wrong! Please try again.", "alert-danger"));
		}

		return "signup";
	}

	// ================= LOGIN =================
	@GetMapping("/signin")
	public String login() {
		log.info("Login page opened");
		return "login";
	}
}
