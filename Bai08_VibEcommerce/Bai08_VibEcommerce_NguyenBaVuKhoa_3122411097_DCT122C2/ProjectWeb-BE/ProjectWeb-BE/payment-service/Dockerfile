# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy parent pom (needed for child module resolution)
COPY pom.xml /workspace/pom.xml

# Copy this service's pom and source
COPY payment-service/pom.xml /workspace/payment-service/pom.xml
COPY payment-service/src /workspace/payment-service/src

# Install parent pom first, then build the service
RUN mvn -f /workspace/pom.xml install -N -DskipTests && \
    mvn -f /workspace/payment-service/pom.xml clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar /app/payment-service.jar
ENTRYPOINT ["java", "-jar", "/app/payment-service.jar"]
EXPOSE 8085
