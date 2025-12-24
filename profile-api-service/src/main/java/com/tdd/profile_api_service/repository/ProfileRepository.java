package com.tdd.profile_api_service.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tdd.profile_api_service.model.UserProfile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

@Repository
@RequiredArgsConstructor
public class ProfileRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    private DynamoDbTable<UserProfile> table() {
        return enhancedClient.table("UserProfile", TableSchema.fromBean(UserProfile.class));
    }

    public UserProfile save(UserProfile profile) {
        try {
            table().putItem(profile);
            return profile;
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to save UserProfile: " + e.getMessage(), e);
        }
    }

    public Optional<UserProfile> findById(String userId) {
        try {
            UserProfile item = table().getItem(r -> r.key(k -> k.partitionValue(userId)));
//            System.out.println(item);
            return Optional.ofNullable(item);
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to load UserProfile: " + e.getMessage(), e);
        }
    }
    
    
    public List<UserProfile> findAll() {
        try {
            List<UserProfile> items = new ArrayList<>();

            table().scan().items().forEach(items::add);

            return items;
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to load all UserProfiles: " + e.getMessage(), e);
        }
    }

}