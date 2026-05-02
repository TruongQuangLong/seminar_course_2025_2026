# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy parent pom (needed for child module resolution)
COPY pom.xml /workspace/pom.xml

# Copy this service's pom and source
COPY api-gateway/pom.xml /workspace/api-gateway/pom.xml
COPY api-gateway/src /workspace/api-gateway/src

# Install parent pom first, then build the service
RUN mvn -f /workspace/pom.xml install -N -DskipTests && \
    mvn -f /workspace/api-gateway/pom.xml clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar /app/api-gateway.jar
ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]
EXPOSE 8181
