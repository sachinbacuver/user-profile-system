package com.tdd.profile_api_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;

//import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;



@DynamoDbBean
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfile {

    private String userId;
    private String bio;
    private String profilePhoto;
    private String gender;
    
    
    public UserProfile() {
    	
    }
    
    public UserProfile(String userId) {
    	this.userId = userId;
    }

    public UserProfile(String userId, String bio, String profilePhoto, String gender) {
		super();
		this.userId = userId;
		this.bio = bio;
		this.profilePhoto = profilePhoto;
		this.gender = gender;
	}
    
	@DynamoDbPartitionKey
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
        return "UserProfile{" +
                "userId='" + userId + '\'' +
                ", bio='" + bio + '\'' +
                ", profilePhoto='" + profilePhoto + '\'' +
                ", gender=" + gender +
                '}';
    }
}
