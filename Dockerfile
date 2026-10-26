# Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home shortify
COPY --from=build /app/target/shortify-api-*.jar app.jar
USER shortify
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
