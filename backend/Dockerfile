FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /workspace

COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon dependencies

COPY src ./src
RUN ./gradlew --no-daemon bootJar \
    && mv build/libs/product-finder-0.0.1-SNAPSHOT.jar application.jar

FROM eclipse-temurin:25-jre-alpine

RUN addgroup --system app \
    && adduser --system --ingroup app app

WORKDIR /app
COPY --from=builder --chown=app:app /workspace/application.jar ./application.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]
