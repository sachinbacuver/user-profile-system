package com.tdd.profile_api_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.tdd.profile_api_service.configuration.TestConfig;
import com.tdd.profile_api_service.model.UserProfile;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

@SpringBootTest
@Testcontainers
//@SpringBootTest@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestConfig.class)
@ActiveProfiles("test")
class ProfileRepositoryIntegrationTest {

//    @SuppressWarnings("resource")
	@Container
	static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
	        .withServices(LocalStackContainer.Service.DYNAMODB);
	
	@Autowired
//	private static LocalStackContainer localstack;


    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.region.static", localstack::getRegion);
        registry.add("spring.cloud.aws.credentials.access-key", () -> "test");
        registry.add("spring.cloud.aws.credentials.secret-key", () -> "test");
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
        registry.add("aws.endpoint",() -> "http://localhost:" + localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).getPort());
    }

    @Autowired
    private ProfileRepository profileRepository;
    
//    @Autowired
//    private DynamoDbTemplate dynamoDbTemplate;
    
    
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;
    
    private DynamoDbTable<UserProfile> table;
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    
    private void waitUntilActive(DynamoDbTable<UserProfile> table) throws InterruptedException {
        while (true) {
            try {
                String status = table.describeTable().table().tableStatusAsString();
                if ("ACTIVE".equals(status)) {
                    break;
                }
                Thread.sleep(500);
            } catch (ResourceNotFoundException e) {
                Thread.sleep(500);
            }
        }
    }

    @BeforeEach
    void setupTable() {
        table = enhancedClient.table("UserProfile", TableSchema.fromBean(UserProfile.class)
        );

        try {
            table.describeTable();
        } catch (ResourceNotFoundException e) {
            table.createTable();
            try {
				waitUntilActive(table);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
    
//    @AfterEach
//    void cleanup() {
//        // Clean up test data after each test
//        try {
//            table.scan().items().forEach(profile -> {
//                profileRepository.deleteById(profile.getUserId());
//            });
//        } catch (Exception e) {
//            // Ignore cleanup errors
//        }
//    }
    
    
    
    @Test
    void whenSaveProfile_thenRetrieveItSuccessfully() {
        // Arrange
    	String userId = "user-1";
        UserProfile profile = new UserProfile(userId);
        
        profile.setBio("Hi this is me");

        // Act
        profileRepository.save(profile);
        Optional<UserProfile> found = profileRepository.findById( "user-1");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getBio()).isEqualTo("Hi this is me");
    }
}
