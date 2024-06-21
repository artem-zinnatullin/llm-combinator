FROM openjdk:21-slim-bullseye

LABEL maintainer="Artem Zinnatullin"

# Requires `./gradlew build`
COPY build/libs/llm-combinator-all.jar /app/llm-combinator-all.jar

WORKDIR /app

ENTRYPOINT ["java", "-Xmx512m", "-Xmx896m", "-jar", "/app/llm-combinator-all.jar"]
