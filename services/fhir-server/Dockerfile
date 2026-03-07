# FHIR Server - Da Vinci Burden Reduction IGs 2.1 (PAS, DTR, CRD, CDex)
# Multi-stage build for minimal image size

FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :services:fhir-server:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/services/fhir-server/build/libs/*.jar app.jar
COPY mappings/ /app/mappings/

RUN mkdir -p /var/log/app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
