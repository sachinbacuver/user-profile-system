package com.tdd.user_api_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "users")
public record User(
	    @Id
	    String id, 
	    String email,
	    String firstName,
	    String lastName,
	    String password 
	) {}
