# 1. build jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# copy pom and download dependencies
COPY pom.xml .
RUN mvn -B dependency:go-offline
# skip tests
COPY src ./src
RUN mvn -B clean package -DskipTests

# 2. run jar
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]