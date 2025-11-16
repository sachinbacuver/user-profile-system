
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Use the exact JAR filename
COPY target/user-api-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8102

ENTRYPOINT ["java", "-jar", "app.jar"]

