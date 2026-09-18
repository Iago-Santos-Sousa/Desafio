FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY config config
COPY src src
RUN mvn -B -q -DskipTests verify
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/ingestion-api-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /data/uploads
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-jar","/app/app.jar"]
