# Prior Auth Service - CMS-0057-F compliance (72hr/7day SLA, X12 denial codes)
# Multi-stage build for minimal image size

FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :services:prior-auth-service:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/services/prior-auth-service/build/libs/*.jar app.jar
COPY mappings/ /app/mappings/

RUN mkdir -p /var/log/app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
