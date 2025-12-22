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

@Testcontainers
@DataMongoTest 
public class UserRepositoryIntegrationTest {

    @Container
//    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0").withReuse(true);


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
        User newUser = new User(
                null,
                "test@example.com",
                "Test",
                "User",
                "password123"
        );

        User savedUser = userRepository.save(newUser);

        assertThat(savedUser.id()).isNotNull();

        Optional<User> foundUser = userRepository.findById(savedUser.id());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().email()).isEqualTo("test@example.com");
        assertThat(foundUser.get().firstName()).isEqualTo("Test");
        assertThat(foundUser.get().lastName()).isEqualTo("User");
    }

    @Test
    @DisplayName("Should return empty optional when user ID does not exist")
    void whenFindById_withInvalidId_shouldReturnEmpty() {

        Optional<User> foundUser = userRepository.findById("non-existent-id");

        assertThat(foundUser).isNotPresent();
    }
}
