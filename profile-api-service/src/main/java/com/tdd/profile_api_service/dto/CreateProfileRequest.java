package com.tdd.profile_api_service.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateProfileRequest {
	
	@NotBlank(message = "userId is required")
	private String userId;
	private String bio;
	private String profilePhoto;
	private String gender;
	
	public CreateProfileRequest() {
		
	}
	
	public CreateProfileRequest(String userId) {
		this.userId = userId;
	}

	public CreateProfileRequest(String userId, String bio, String profilePhoto, String gender) {
		super();
		this.userId = userId;
		this.bio = bio;
		this.profilePhoto = profilePhoto;
		this.gender = gender;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getProfilePhoto() {
		return profilePhoto;
	}

	public void setProfilePhoto(String profilePhoto) {
		this.profilePhoto = profilePhoto;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	public String toString() {
		return "CreateProfileRequest [userId=" + userId + ", bio=" + bio + ", profilePhoto=" + profilePhoto
				+ ", gender=" + gender + "]";
	}
	
	

}
