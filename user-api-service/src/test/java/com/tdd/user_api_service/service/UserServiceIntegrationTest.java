package com.tdd.user_api_service.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import com.tdd.user_api_service.dto.CreateUserRequest;
import com.tdd.user_api_service.dto.UserResponse;
import com.tdd.user_api_service.model.User;
import com.tdd.user_api_service.repository.UserRepository;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
//    brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" },
//    brokerProperties = { "listeners=PLAINTEXT://kafka:9092", "port=9092" },
    topics = { "user-created-topic" } // Topic name defined in test
)
@ActiveProfiles("test") 
class UserServiceIntegrationTest {

    @Autowired
//	@MockBean
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private Consumer<String, UserResponse> consumer;
    private final String TOPIC_NAME = "user-created-topic";

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-consumer-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        org.springframework.kafka.support.serializer.JsonDeserializer<UserResponse> valueDeserializer = 
                new org.springframework.kafka.support.serializer.JsonDeserializer<>(UserResponse.class, false);
        
        valueDeserializer.addTrustedPackages("com.tdd.user_api_service.dto");
        
        ConsumerFactory<String, UserResponse> cf = new DefaultKafkaConsumerFactory<>(
        		consumerProps,
                new org.apache.kafka.common.serialization.StringDeserializer(),
                valueDeserializer
        );

        consumer = cf.createConsumer();
        consumer.subscribe(java.util.Collections.singletonList(TOPIC_NAME));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void whenCreateUser_shouldPublishUserCreatedEvent() {
        // --- Arrange ---
        CreateUserRequest request = new CreateUserRequest(
                "kafka.test@example.com", "Kafka", "Test", "kfk123"
        );
        User savedUser = new User(
                "mock-id-kafka-123", "kafka.test@example.com", "Kafka", "Test", "kfk123"
        );
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.createUser(request);

        ConsumerRecord<String, UserResponse> record = KafkaTestUtils.getSingleRecord(consumer, TOPIC_NAME, Duration.ofMillis(10000));

        assertThat(record).isNotNull();
        assertThat(record.value().email()).isEqualTo("kafka.test@example.com");
        assertThat(record.value().firstName()).isEqualTo("Kafka");
        assertThat(record.value().lastName()).isEqualTo("Test");
    }
}
