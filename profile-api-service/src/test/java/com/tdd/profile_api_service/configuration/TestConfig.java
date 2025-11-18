package com.tdd.profile_api_service.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@TestConfiguration(proxyBeanMethods = false)
@Profile("test")
public class TestConfig {
	
//	private static final DockerImageName LOCALSTACK_IMAGE =
//            DockerImageName.parse("localstack/localstack:3.0");

//	@Bean
//    public LocalStackContainer localStackContainer() {
//        LocalStackContainer localStack =
//                new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
//                        .withServices(LocalStackContainer.Service.DYNAMODB);
//        localStack.start();
//        return localStack;
//    }

    @Bean
    @Primary
    public DynamoDbClient dynamoDbClient(LocalStackContainer localstack) {
        return DynamoDbClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
                .region(Region.of(localstack.getRegion()))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }
    
    @Bean
    public DynamoDbTemplate dynamoDbTemplate(DynamoDbEnhancedClient enhancedClient) {
        return new DynamoDbTemplate(enhancedClient);
    }
}
