package com.contactManager.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.contactManager.entities.Contact;
import com.contactManager.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	@Query("from Contact as d where d.user.id =:userId")
	public Page<Contact> findContactByUser(@Param("userId") int userid, Pageable pageable);

	Page<Contact> findByNameContainingAndUser(String name, User user, Pageable pageable);
	Page<Contact> findByNameContainingIgnoreCaseAndUser(String name,User user,Pageable pageable);

}
