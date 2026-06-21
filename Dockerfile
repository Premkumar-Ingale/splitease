# Build stage
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/splitease-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# Restrict JVM memory so it doesn't exceed Render's 512MB free tier limit
ENTRYPOINT ["java", "-Xmx256m", "-Xss512k", "-XX:MaxMetaspaceSize=128m", "-jar", "app.jar", "--spring.profiles.active=prod"]
