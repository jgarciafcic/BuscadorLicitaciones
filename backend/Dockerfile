FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Ensure SSL certificates are available for Maven downloads
RUN apk add --no-cache ca-certificates

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:resolve
COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Ensure runtime has CA certs (needed for outbound HTTPS calls if used)
RUN apk add --no-cache ca-certificates

COPY --from=build /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx150m", "-Xss256k", "-XX:ReservedCodeCacheSize=32m", "-XX:+UseSerialGC", "-jar", "app.jar"]
