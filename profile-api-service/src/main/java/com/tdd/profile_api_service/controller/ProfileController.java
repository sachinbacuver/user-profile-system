package com.tdd.profile_api_service.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tdd.profile_api_service.dto.CreateProfileRequest;
import com.tdd.profile_api_service.model.UserProfile;
import com.tdd.profile_api_service.service.ProfileServiceImpl;

import jakarta.validation.Valid;

/**
* REST Controller for profile operations
* GREEN PHASE: Minimal implementation to make tests pass
* 
* Handles creation of empty user profiles
* Called by notification-service after user registration
*/
@RestController
@RequestMapping("/profiles")
@Validated
public class ProfileController {
 
 private final ProfileServiceImpl profileService;
 
 public ProfileController(ProfileServiceImpl profileService) {
     this.profileService = profileService;
 }
 
 /**
  * Creates an empty user profile
  * POST /profiles
  * 
  * @param request contains only userId (other fields ignored by Jackson)
  * @return 201 Created with empty UserProfile
  */
 @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
              consumes = MediaType.APPLICATION_JSON_VALUE)
 public ResponseEntity<UserProfile> createProfile(@Valid @RequestBody CreateProfileRequest request) {
     UserProfile createdProfile = profileService.createProfile(request);
     return ResponseEntity.status(HttpStatus.CREATED).body(createdProfile);
 }
 
 @GetMapping("/{userId}")
 public ResponseEntity<UserProfile> getProfile(@PathVariable String userId) {
	 Optional<UserProfile> profile = profileService.getProfile(userId);
	 System.out.println(profile);
     return ResponseEntity.of(profile);
 }

}
