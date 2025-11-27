package com.tdd.notification_service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NotificationHandler implements RequestHandler<KafkaEvent, String> {

//    private static final String PROFILE_API_URL = "http://localhost:8081/profiles"; 
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String profileApiUrl;
    
    public NotificationHandler() {
        this.profileApiUrl = "http://profile-api-discovery.profile-api-service:8081/profiles";
    }
    
    public NotificationHandler(String profileApiUrl) {
        this.profileApiUrl = profileApiUrl;
    }

    @Override
    public String handleRequest(KafkaEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        try {
            for (var records : event.getRecords().values()) {
                for (KafkaEvent.KafkaEventRecord record : records) {
                    logger.log("Received Kafka message: " + record.getValue());
                    
                    
                    // 2️⃣ Decode Base64
                    byte[] decodedBytes = Base64.getDecoder().decode(record.getValue());
                    String json = new String(decodedBytes, StandardCharsets.UTF_8);

                    logger.log("Decoded JSON string: " + json);

                    // 3️⃣ Parse JSON
                    JsonNode jsonNode = mapper.readTree(json);
                    String userId = jsonNode.get("id").asText();

                    logger.log("Extracted userId: " + userId);

                    // 4️⃣ Build Profile API request JSON
                    JsonNode profileRequest = mapper.createObjectNode()
                            .put("userId", userId)
                            .put("bio", "")
                            .put("profilePhoto", "")
                            .put("gender", "");

                    String requestBody = mapper.writeValueAsString(profileRequest);

//                    JsonNode jsonNode = mapper.readTree(record.getValue());
//                    String userId = jsonNode.get("id").asText();
//                    logger.log("Extracted userId: " + userId);

//                    // Prepare JSON body for Profile API
//                    String requestBody = "{"
//                            + "\"userId\": \"" + userId + "\","
//                            + "\"bio\": \"\","
//                            + "\"profilePhoto\": \"\","
//                            + "\"gender\": \"\""
//                            + "}";


                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(profileApiUrl))
                            .timeout(Duration.ofSeconds(3))  
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
        	logger.log("HTTP ERROR: " + e.toString());
            logger.log("Error processing event: " + e.getMessage());
        }
        
        return "Processed successfully";
    }
}
