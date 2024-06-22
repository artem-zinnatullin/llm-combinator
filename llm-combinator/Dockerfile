FROM openjdk:21-slim-bullseye

LABEL maintainer="Artem Zinnatullin"

COPY container-entrypoint.sh /app/

# Requires `./gradlew build`
COPY build/libs/llm-combinator-all.jar /app/llm-combinator-all.jar

WORKDIR /app
ENTRYPOINT ["/app/container-entrypoint.sh"]
