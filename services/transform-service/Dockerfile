# Transform Service - HL7v2 → FHIR R4 transformation microservice
# Multi-stage build for minimal image size

FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :services:transform-service:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy service JAR
COPY --from=build /app/services/transform-service/build/libs/*.jar app.jar

# Copy Whistle mappings (HL7v2, FHIR version conversion, Da Vinci)
COPY mappings/ /app/mappings/

# Log directory for Fluent Bit sidecar scraping
RUN mkdir -p /var/log/app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
