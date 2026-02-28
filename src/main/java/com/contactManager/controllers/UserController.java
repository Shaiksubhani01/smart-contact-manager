package com.contactManager.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contactManager.entities.Contact;
import com.contactManager.entities.User;
import com.contactManager.helper.Message;
import com.contactManager.repositories.ContactRepository;
import com.contactManager.repositories.UserRepository;
import com.contactManager.services.UserServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	/*
	 * ========================= COMMON USER DATA =========================
	 */
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		if (principal == null) {
			log.warn("Principal is null while adding common data");
			return;
		}

		Optional<User> optionalUser = userRepository.findByEmail(principal.getName());

		if (optionalUser.isPresent()) {
			model.addAttribute("user", optionalUser.get());
			log.debug("Common user data added for: {}", principal.getName());
		} else {
			log.warn("User not found for principal: {}", principal.getName());
		}
	}

	/*
	 * ========================= DASHBOARD =========================
	 */
	@GetMapping("/index")
	public String dashboard(Authentication authentication) {
		log.info("Dashboard accessed by user: {}", authentication.getName());
		return "normal/user_dashboard";
	}

	/*
	 * ========================= ADD CONTACT =========================
	 */
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("contact", new Contact());
		log.info("Add contact page opened");
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processForm(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {

			log.info("Adding contact for user: {}", principal.getName());

			Optional<User> optionalUser = userRepository.findByEmail(principal.getName());

			if (!optionalUser.isPresent()) {
				log.warn("User not found while adding contact: {}", principal.getName());
				return "redirect:/signin";
			}

			User user = optionalUser.get();

			handleImageUpload(contact, file);
			sanitizeDescription(contact);

			user.getContacts().add(contact);
			contact.setUser(user);

			userRepository.save(user);

			log.info("Contact added successfully for user: {}", principal.getName());

			session.setAttribute("message", new Message("Contact added successfully", "alert-success"));

		} catch (Exception e) {

			log.error("Error while adding contact", e);

			session.setAttribute("message", new Message("Something went wrong: " + e.getMessage(), "alert-danger"));
		}

		return "normal/add_contact_form";
	}

	/*
	 * ========================= SHOW CONTACTS =========================
	 */
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable Integer page, Model model, Authentication authentication,
			HttpSession session) {

		try {

			if (authentication == null || !authentication.isAuthenticated()) {
				log.warn("Unauthenticated access attempt to show contacts");
				return "redirect:/signin";
			}

			String email = authentication.getName();
			log.info("Fetching contacts for user: {} | Page: {}", email, page);

			Optional<User> optionalUser = userRepository.findByEmail(email);

			if (!optionalUser.isPresent()) {
				log.warn("User not found while fetching contacts: {}", email);
				return "redirect:/signin";
			}

			User user = optionalUser.get();

			Pageable pageable = PageRequest.of(page, 6);
			Page<Contact> contacts = contactRepository.findContactByUser(user.getId(), pageable);

			if (contacts.isEmpty()) {
				log.info("No contacts found for user: {}", email);
				model.addAttribute("noContacts", true);
			}

			model.addAttribute("allContacts", contacts);
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", contacts.getTotalPages());

			return "normal/show_contacts";

		} catch (Exception e) {

			log.error("Error while showing contacts", e);

			session.setAttribute("message", new Message("Something went wrong: " + e.getMessage(), "alert-danger"));

			return "redirect:/user/index";
		}
	}

	/*
	 * ========================= CONTACT DETAILS =========================
	 */
	@GetMapping("/{contact_id}/contact")
	public String showContactDetails(@PathVariable("contact_id") Integer contactId, Model model, Principal principal) {

		try {

			log.info("Fetching contact details for ID: {}", contactId);

			Optional<Contact> optionalContact = contactRepository.findById(contactId);

			if (!optionalContact.isPresent()) {
				log.warn("Contact not found: {}", contactId);
				return "redirect:/user/show_contacts/0";
			}

			Contact contact = optionalContact.get();
			User user = userRepository.findByEmail(principal.getName()).get();

			if (user.getId() == contact.getUser().getId()) {
				model.addAttribute("contact", contact);
				log.info("Contact details shown for ID: {}", contactId);
			} else {
				log.warn("Unauthorized access to contact ID: {}", contactId);
			}

		} catch (Exception e) {
			log.error("Error fetching contact details", e);
		}

		return "normal/contact-detail";
	}

	/*
	 * ========================= DELETE CONTACT =========================
	 */
	@GetMapping("/delete/{contact_id}")
	public String deleteContact(@PathVariable("contact_id") Integer contactId, Principal principal,
			HttpSession session) {

		try {

			log.info("Delete request for contact ID: {}", contactId);

			Optional<Contact> optionalContact = contactRepository.findById(contactId);

			if (!optionalContact.isPresent()) {
				log.warn("Contact not found for deletion: {}", contactId);
				return "redirect:/user/show_contacts/0";
			}

			Contact contact = optionalContact.get();
			User user = userRepository.findByEmail(principal.getName()).get();

			if (user.getId() == contact.getUser().getId()) {

				user.getContacts().remove(contact);
				userRepository.save(user);

				log.info("Contact deleted successfully: {}", contactId);

				session.setAttribute("message", new Message("Contact deleted successfully", "alert-success"));
			} else {

				log.warn("Unauthorized delete attempt for contact ID: {}", contactId);

				session.setAttribute("message", new Message("Unauthorized action", "alert-danger"));
			}

		} catch (Exception e) {

			log.error("Error deleting contact ID: {}", contactId, e);

			session.setAttribute("message", new Message("Error deleting contact", "alert-danger"));
		}

		return "redirect:/user/show_contacts/0";
	}

	/*
	 * ========================= UPDATE CONTACT =========================
	 */
	@PostMapping("/update-contact/{contact_id}")
	public String updateContact(@PathVariable("contact_id") Integer contactId, Model model) {

		Optional<Contact> optionalContact = contactRepository.findById(contactId);

		if (optionalContact.isPresent()) {
			model.addAttribute("contact", optionalContact.get());
			log.info("Update page opened for contact ID: {}", contactId);
		} else {
			log.warn("Contact not found for update: {}", contactId);
		}

		return "normal/update_form";
	}

	@PostMapping("/process-update")
	public String processUpdate(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {

			log.info("Updating contact ID: {}", contact.getContact_id());

			User user = userRepository.findByEmail(principal.getName()).get();
			Contact oldContact = contactRepository.findById(contact.getContact_id()).get();

			updateImage(contact, file, oldContact);
			sanitizeDescription(contact);

			contact.setUser(user);
			contactRepository.save(contact);

			log.info("Contact updated successfully: {}", contact.getContact_id());

			session.setAttribute("message", new Message("Contact updated successfully", "alert-success"));

		} catch (Exception e) {

			log.error("Error updating contact ID: {}", contact.getContact_id(), e);

			session.setAttribute("message", new Message("Something went wrong: " + e.getMessage(), "alert-danger"));
		}

		return "redirect:/user/" + contact.getContact_id() + "/contact";
	}

	/*
	 * ========================= HELPER METHODS =========================
	 */

	private void handleImageUpload(Contact contact, MultipartFile file) throws Exception {

		if (file.isEmpty()) {
			contact.setImage("contact.png");
			return;
		}

		contact.setImage(file.getOriginalFilename());

		File saveDir = new ClassPathResource("static/img").getFile();
		Path path = Paths.get(saveDir.getAbsolutePath(), file.getOriginalFilename());

		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	}

	private void updateImage(Contact contact, MultipartFile file, Contact oldContact) throws Exception {

		if (file.isEmpty()) {
			contact.setImage(oldContact.getImage());
			return;
		}

		File imgDir = new ClassPathResource("static/img").getFile();
		File oldFile = new File(imgDir, oldContact.getImage());

		if (oldFile.exists())
			oldFile.delete();

		contact.setImage(file.getOriginalFilename());

		Path path = Paths.get(imgDir.getAbsolutePath(), file.getOriginalFilename());

		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	}

	private void sanitizeDescription(Contact contact) {

		if (contact.getDescription() != null) {
			String clean = contact.getDescription().replaceAll("\\<.*?>", "");
			contact.setDescription(clean);
		}
	}
	
	
	  // Mapping for Search Contacts page-Done 
    @GetMapping("/search_contact_form")
    public String searchContactPage() {
        
        return "normal/search_contact";
    }
    
    
    @GetMapping("/search_contact")
    public String searchContact(
            @RequestParam("query") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Principal principal,
            Model model) {

        // Get logged-in user email  
        String email = principal.getName();

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) {
            return "redirect:/signin";
        }

        User user = optionalUser.get();

        Pageable pageable = PageRequest.of(page, 5);

        Page<Contact> results =
                contactRepository.findByNameContainingIgnoreCaseAndUser(query, user, pageable);

        model.addAttribute("contacts", results);
        model.addAttribute("currentPage", page);
        model.addAttribute("query", query);

        return "normal/search_contact";
    }



}
