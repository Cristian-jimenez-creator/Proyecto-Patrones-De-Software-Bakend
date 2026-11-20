FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY PoolGuardBackend/pom.xml PoolGuardBackend/pom.xml
COPY PoolGuardBackend/src PoolGuardBackend/src
RUN mvn -B -DskipTests -f PoolGuardBackend/pom.xml package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/PoolGuardBackend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
