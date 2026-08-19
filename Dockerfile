# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Package stage
FROM eclipse-temurin:21-jre
COPY --from=build /target/trustproof-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-Djava.net.preferIPv4Addresses=true", "-jar", "app.jar"]