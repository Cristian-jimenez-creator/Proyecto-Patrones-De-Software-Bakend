FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY PoolGuardServer/pom.xml PoolGuardServer/pom.xml
COPY PoolGuardServer/src PoolGuardServer/src
RUN mvn -B -DskipTests -f PoolGuardServer/pom.xml package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/PoolGuardServer/target/poolguard-camera-api.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
