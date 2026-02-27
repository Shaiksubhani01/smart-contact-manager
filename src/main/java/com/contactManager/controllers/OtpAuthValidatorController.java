package com.contactManager.controllers;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contactManager.entities.User;
import com.contactManager.mailsender.EmailService;
import com.contactManager.repositories.UserRepository;

@Controller
public class OtpAuthValidatorController {

	private static final Logger log = LoggerFactory.getLogger(OtpAuthValidatorController.class);

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailService emailService;

	@Value("${mail.otp.subject}")
	private String otpSubject;

	@Value("${mail.otp.body}")
	private String otpBody;

	@Value("${app.otp.expiry}")
	private String otpExpiry;

	// ================= STEP 1 - Validate Username & Password =================
	@PostMapping("/login-verify")
	public String loginVerify(@RequestParam String username, @RequestParam String password, HttpSession session,
			Model model) {

		log.info("Login attempt for user: {}", username);

		Optional<User> optionalUser = userRepository.findByEmail(username);

		if (optionalUser.isEmpty()) {
			log.warn("Login failed - user not found: {}", username);
			model.addAttribute("error", "Invalid Username or Password");
			return "login";
		}

		User user = optionalUser.get();

		if (!passwordEncoder.matches(password, user.getPassword())) {
			log.warn("Login failed - wrong password for user: {}", username);
			model.addAttribute("error", "Invalid Username or Password");
			return "login";
		}

		// Generate 6-digit OTP
		String otp = String.valueOf(new Random().nextInt(900000) + 100000);

		session.setAttribute("otp", otp);
		session.setAttribute("user", user);

		log.info("Password verified successfully for user: {}", username);
		log.debug("Generated OTP for user {}: {}", username, otp); // remove in production if needed

		String subject = otpSubject;

		String body = String.format(otpBody, user.getName(), // %s → username
				otp, // %s → otp
				otpExpiry // %s → expiry minutes
		);

		boolean mailSent = emailService.sendEmail(user.getEmail(), subject, body);

		if (!mailSent) {
			log.error("Failed to send OTP email to user: {}", username);
			model.addAttribute("error", "Unable to send OTP. Please try again later.");
			return "login";
		}

		log.info("OTP sent successfully to user: {}", username);

		model.addAttribute("otpSent", true);
		model.addAttribute("success", "OTP sent successfully");

		return "login";
	}

	// ================= STEP 2 - Validate OTP =================
	@PostMapping("/validate-otp")
	public String validateOtp(@RequestParam String otp, HttpSession session, Model model) {

		String sessionOtp = (String) session.getAttribute("otp");
		User user = (User) session.getAttribute("user");

		if (sessionOtp == null || user == null) {
			log.warn("OTP validation failed - session expired");
			model.addAttribute("error", "Session expired. Please login again.");
			return "login";
		}

		if (!otp.equals(sessionOtp)) {
			log.warn("Invalid OTP entered for user: {}", user.getEmail());
			model.addAttribute("error", "Invalid OTP");
			return "login";
		}

		log.info("OTP verified successfully for user: {}", user.getEmail());

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
				Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));

		SecurityContextHolder.getContext().setAuthentication(auth);

		session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

		session.removeAttribute("otp");
		session.removeAttribute("user");

		log.info("User logged in successfully: {}", user.getEmail());

		return "redirect:/user/index";
	}
}
