package com.contactManager.mailsender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private JavaMailSender mailSender;

	public boolean sendEmail(String to, String subject, String message) {

		log.info("Preparing to send email to: {}", to);

		try {
			SimpleMailMessage mail = new SimpleMailMessage();
			mail.setTo(to);
			mail.setSubject(subject);
			mail.setText(message);

			mailSender.send(mail);

			log.info("Email sent successfully to: {}", to);

			return true;

		} catch (Exception e) {
			log.error("Failed to send email to: {}", to, e);
			return false;
		}
	}
}
