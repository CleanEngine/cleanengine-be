FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/coin-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "coin-0.0.1-SNAPSHOT.jar"]