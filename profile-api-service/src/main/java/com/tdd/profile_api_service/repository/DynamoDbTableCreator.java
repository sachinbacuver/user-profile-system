package com.tdd.profile_api_service.repository;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tdd.profile_api_service.model.UserProfile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

@Slf4j
@Configuration
public class DynamoDbTableCreator {

    @Bean
    CommandLineRunner createUserProfileTable(DynamoDbEnhancedClient enhancedClient) {
        return args -> {
            DynamoDbTable<UserProfile> table = enhancedClient.table("UserProfile", TableSchema.fromBean(UserProfile.class));

            try {
//                table.createTable(CreateTableEnhancedRequest.builder()
//                        .provisionedThroughput(
//                                ProvisionedThroughput.builder()
//                                        .readCapacityUnits(5L)
//                                        .writeCapacityUnits(5L)
//                                        .build())
//                        .build());
            	
            	table.createTable();
                log.info("✅ Created DynamoDB table: UserProfile");
            } catch (Exception e) {
                log.info("⚠️ Table UserProfile already exists or could not be created: {}", e.getMessage());
            }
        };
    }
}

