package com.contactManager.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.contactManager.entities.User;
import com.contactManager.repositories.UserRepository;

@Service
public class UserServiceImpl {

	@Autowired
	private UserRepository userRepository;
	
	

	public User createUser(User user) {

		return userRepository.save(user);

	}

}
