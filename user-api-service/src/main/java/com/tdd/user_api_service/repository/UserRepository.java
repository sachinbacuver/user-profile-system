package com.tdd.user_api_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tdd.user_api_service.model.User;

public interface UserRepository extends MongoRepository<User , String>{


}