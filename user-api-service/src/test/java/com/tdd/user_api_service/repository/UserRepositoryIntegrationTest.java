package com.tdd.user_api_service.repository;

import com.tdd.user_api_service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test for the UserRepository.
 * This test uses @Testcontainers to start a real MongoDB database in a Docker container.
 */
@Testcontainers
@DataMongoTest // Configures an in-memory DB by default, but we override it
public class UserRepositoryIntegrationTest {

    /**
     * This defines the MongoDB Docker container.
     * The "mongo:7.0" image will be pulled from Docker Hub.
     */
    @Container
//    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0").withReuse(true);


    /**
     * This method dynamically overrides the Spring Boot 'spring.data.mongodb.uri'
     * property *before* the application context starts. It points Spring to our
     * Testcontainer instead of a hardcoded database.
     */
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save user and find them by ID")
    void whenSaveUser_thenFindById_shouldReturnUser() {
        // 1. Arrange (Setup)
        User newUser = new User(
                null,
                "test@example.com",
                "Test",
                "User",
                "password123"
        );

        // 2. Act (Execute)
        User savedUser = userRepository.save(newUser);

        // 3. Assert (Verify)
        assertThat(savedUser.id()).isNotNull();

        // Now, try to find the user we just saved
        Optional<User> foundUser = userRepository.findById(savedUser.id());

        // Verify the user was found and the data matches
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().email()).isEqualTo("test@example.com");
        assertThat(foundUser.get().firstName()).isEqualTo("Test");
        assertThat(foundUser.get().lastName()).isEqualTo("User");
    }

    @Test
    @DisplayName("Should return empty optional when user ID does not exist")
    void whenFindById_withInvalidId_shouldReturnEmpty() {
        // 1. Arrange (No setup needed, DB is empty)

        // 2. Act
        Optional<User> foundUser = userRepository.findById("non-existent-id");

        // 3. Assert
        assertThat(foundUser).isNotPresent();
    }
}
