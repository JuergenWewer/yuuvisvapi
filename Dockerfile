FROM openjdk:11.0.3-jre-slim-stretch

USER root

# install some basic command line tools
RUN apt-get update; \
	  apt-get install -y curl wget vim tar bash

RUN mkdir /api
RUN mkdir /file-uploads
RUN chmod 777 /file-uploads

COPY src/main/resources/dist /src/main/resources/dist
COPY target/yuuvis-v-api-2.4.0-SNAPSHOT-fat.jar /api/app.jar

USER 1001

CMD [ "java", "-jar", "/api/app.jar"]
