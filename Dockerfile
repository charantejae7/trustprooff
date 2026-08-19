# Build stage: Uses Maven and Java 21 to build the project inside the cloud container
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Package stage: Runs the compiled application using a lightweight Java 21 runtime
FROM eclipse-temurin:21-jre
COPY --from=build /target/trustproof-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]