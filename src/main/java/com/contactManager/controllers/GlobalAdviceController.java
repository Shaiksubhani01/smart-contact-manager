package com.contactManager.controllers;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.contactManager.entities.User;
import com.contactManager.repositories.UserRepository;

@ControllerAdvice
public class GlobalAdviceController {

	@Autowired
	private UserRepository userRepository;

	@ModelAttribute
	public void addCommonData(org.springframework.ui.Model model, Principal principal) {

		if (principal != null) {

			String username = principal.getName();

			Optional<User> userOptional = userRepository.findByEmail(username);

			if (userOptional.isPresent()) {
				model.addAttribute("user", userOptional.get());
			}
		}
	}
}
