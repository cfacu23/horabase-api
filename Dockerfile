FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S horabase && adduser -S horabase -G horabase
WORKDIR /app
COPY --from=build /workspace/target/horabase-api-*.jar app.jar
USER horabase
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 CMD wget -qO- http://localhost:8080/api/health >/dev/null || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
