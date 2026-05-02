# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy parent pom (needed for child module resolution)
COPY pom.xml /workspace/pom.xml

# Copy this service's pom and source
COPY product-service/pom.xml /workspace/product-service/pom.xml
COPY product-service/src /workspace/product-service/src

# Install parent pom first, then build the service
RUN mvn -f /workspace/pom.xml install -N -DskipTests && \
    mvn -f /workspace/product-service/pom.xml clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/product-service/target/product-service-0.0.1-SNAPSHOT-exec.jar /app/product-service.jar
ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]
EXPOSE 8082
