# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy parent pom (needed for child module resolution)
COPY pom.xml /workspace/pom.xml

# Copy this service's pom and source
COPY order-service/pom.xml /workspace/order-service/pom.xml
COPY order-service/src /workspace/order-service/src

# Install parent pom (non-recursive), then build order-service
RUN mvn -f /workspace/pom.xml install -N -DskipTests && \
    mvn -f /workspace/order-service/pom.xml clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/order-service/target/order-service-0.0.1-SNAPSHOT.jar /app/order-service.jar
ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]
EXPOSE 8083
