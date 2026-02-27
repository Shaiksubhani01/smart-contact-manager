package com.contactManager.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Entity
@Table(name="Users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@NotBlank(message = "Name should not be empty..!")
	@NotEmpty(message = "Name should not be empty..!")
	@Size(min = 3, max = 20, message = "min 3 and max 20 charecters are allowed")
	private String name;
	@Column(unique = true)
	@Email
	private String email;
	@NotBlank(message = "password should not be empty..!")
	private String password;
	private String role;
	private String imageUrl;
	@Column(length = 500)
	private String about;
	private boolean enabled;

	public User() {

	}

	public User(int id,
			@NotBlank(message = "Name should not be empty..!") @NotEmpty(message = "Name should not be empty..!") @Size(min = 3, max = 20, message = "min 3 and max 20 charecters are allowed") String name,
			@Email String email, @NotBlank(message = "password should not be empty..!") String password, String role,
			String imageUrl, String about, boolean enabled) {
		super();
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
		this.imageUrl = imageUrl;
		this.about = about;
		this.enabled = enabled;
	}

	public User(int id, String name, String email, String password, String role, String imageUrl, String about,
			boolean enabled, List<Contact> contacts) {
		super();
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
		this.imageUrl = imageUrl;
		this.about = about;
		this.enabled = enabled;
		this.contacts = contacts;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Contact> contacts = new ArrayList<Contact>();

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + "]";
	}

}
