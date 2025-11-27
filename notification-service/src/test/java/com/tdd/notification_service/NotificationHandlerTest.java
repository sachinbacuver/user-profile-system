package com.tdd.notification_service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class NotificationHandlerTest {

    private static WireMockServer wireMockServer;
    private NotificationHandler handler;
    private Context mockContext;
    private LambdaLogger mockLogger;

    @BeforeAll
    static void startServer() {
        wireMockServer = new WireMockServer(8080); // same as handler URL
        wireMockServer.start();
    }

    @AfterAll
    static void stopServer() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setup() {
        handler = new NotificationHandler("http://localhost:8080/profiles");

        mockContext = Mockito.mock(Context.class);
        mockLogger = Mockito.mock(LambdaLogger.class);
        when(mockContext.getLogger()).thenReturn(mockLogger);

        wireMockServer.stubFor(post(urlEqualTo("/profiles"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody("{\"message\": \"Profile created\"}")));
    }


    @Test
    void testHandleRequest_ExtractsUserIdAndCallsProfileApi() {
        // Given a Kafka event that contains full profile data
        KafkaEvent event = new KafkaEvent();
        KafkaEvent.KafkaEventRecord record = new KafkaEvent.KafkaEventRecord();
//        record.setValue("""
//                {
//                    "id": "6914d469f36e19285fdc25c9",
//                    "email": "test1@gmail.com",
//                    "firstName": "Test1",
//                    "lastName": "A"
//                }
//                """);
        
        String rawJson = """
                {
                    "id": "6914d469f36e19285fdc25c9",
                    "email": "test1@gmail.com",
                    "firstName": "Test1",
                    "lastName": "A"
                }
                """;

        String encoded = Base64.getEncoder()
                .encodeToString(rawJson.getBytes(StandardCharsets.UTF_8));

        record.setValue(encoded);

        
        
        event.setRecords(Map.of("user-topic", List.of(record)));

        // When
        handler.handleRequest(event, mockContext);

        // Then verify that only userId is sent to /profiles
        WireMock.verify(
        	    postRequestedFor(urlEqualTo("/profiles"))
        	        .withRequestBody(equalToJson("{"
        	            + "\"userId\": \"6914d469f36e19285fdc25c9\","
        	            + "\"bio\": \"\","
        	            + "\"profilePhoto\": \"\","
        	            + "\"gender\": \"\""
        	            + "}")
        	        )
        	);


        verify(mockLogger, atLeastOnce()).log(contains("Extracted userId: 6914d469f36e19285fdc25c9"));
        verify(mockLogger, atLeastOnce()).log(contains("Welcome! Profile created"));
    }
}
