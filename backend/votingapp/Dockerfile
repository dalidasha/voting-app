FROM openjdk:17-jdk-alpine AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN chmod +x gradlew
RUN ./gradlew dependencies

COPY src src
RUN ./gradlew build

FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/votingapp.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
