FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY trading-core/build.gradle.kts ./trading-core/build.gradle.kts

RUN chmod +x gradlew

COPY trading-core/src ./trading-core/src

RUN ./gradlew :trading-core:bootJar --no-daemon -x test

RUN JAR_FILE="$(find trading-core/build/libs \
        -maxdepth 1 \
        -type f \
        -name '*.jar' \
        ! -name '*-plain.jar' \
        | head -n 1)" \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /workspace/app.jar

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system spring && useradd --system \
    --gid spring \
    --create-home \
    spring

COPY --from=build --chown=spring:spring \
    /workspace/app.jar \
    /app/app.jar

USER spring:spring

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
