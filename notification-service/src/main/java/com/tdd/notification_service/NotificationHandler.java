package com.tdd.notification_service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NotificationHandler implements RequestHandler<KafkaEvent, String> {

    private static final String PROFILE_API_URL = "http://localhost:8080/profiles"; // or your deployed endpoint
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String handleRequest(KafkaEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        try {
            for (var records : event.getRecords().values()) {
                for (KafkaEvent.KafkaEventRecord record : records) {
                    logger.log("Received Kafka message: " + record.getValue());

                    JsonNode jsonNode = mapper.readTree(record.getValue());
                    String userId = jsonNode.get("id").asText(); // Extract only userId
                    logger.log("Extracted userId: " + userId);

                    // Prepare JSON body for Profile API
                    String requestBody = "{\"userId\": \"" + userId + "\"}";

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(PROFILE_API_URL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    logger.log("Profile API response: " + response.statusCode());
                    if (response.statusCode() == 201 || response.statusCode() == 200) {
                        logger.log("Welcome! Profile created for user: " + userId);
                    } else {
                        logger.log("Failed to create profile for user: " + userId);
                    }
                }
            }
        } catch (Exception e) {
            logger.log("Error processing event: " + e.getMessage());
        }

        return "Processed successfully";
    }
}
