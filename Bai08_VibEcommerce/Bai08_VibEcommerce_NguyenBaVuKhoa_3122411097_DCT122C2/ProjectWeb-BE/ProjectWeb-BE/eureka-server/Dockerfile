# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy parent pom (needed for child module resolution)
COPY pom.xml /workspace/pom.xml

# Copy this service's pom and source
COPY eureka-server/pom.xml /workspace/eureka-server/pom.xml
COPY eureka-server/src /workspace/eureka-server/src

# Install parent pom first, then build the service
RUN mvn -f /workspace/pom.xml install -N -DskipTests && \
    mvn -f /workspace/eureka-server/pom.xml clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar /app/eureka-server.jar
ENTRYPOINT ["java", "-jar", "/app/eureka-server.jar"]
EXPOSE 8761
