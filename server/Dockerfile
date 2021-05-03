#FROM ghcr.io/graalvm/graalvm-ce:21.1.0
FROM openjdk:16-alpine
RUN apk --no-cache add curl
COPY build/libs/*-all.jar server.jar
CMD java ${JAVA_OPTS} -jar server.jar