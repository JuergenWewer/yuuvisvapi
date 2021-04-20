FROM openjdk:11.0.3-jre-slim-stretch

USER root

# install some basic command line tools
RUN apt-get update; \
	  apt-get install -y curl wget vim tar bash

RUN mkdir /api

COPY target/yuuvis-v-api-1.5-SNAPSHOT-fat.jar /api/app.jar

USER 1001

CMD [ "java", "-jar", "/api/app.jar"]
