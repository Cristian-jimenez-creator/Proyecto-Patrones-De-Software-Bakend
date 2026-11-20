FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pool-safety-backend/pom.xml pool-safety-backend/pom.xml
COPY pool-safety-backend/src pool-safety-backend/src
RUN mvn -B -DskipTests -f pool-safety-backend/pom.xml package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/pool-safety-backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
