# Dockerfile multi-etapa para Aura Eventos API Backend (Spring Boot Java 17)

FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S aura && adduser -S aura -G aura
COPY --from=build /app/target/*.jar app.jar
RUN chown -R aura:aura /app
USER aura
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
