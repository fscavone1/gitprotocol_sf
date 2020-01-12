FROM maven:latest as mvn
RUN apt-get update && apt-get -y install git
WORKDIR /app
ENV URL=https://github.com/fscavone1/gitprotocol_sf.git
ENV PROJECT=gitprotocol_sf
RUN git clone $URL
WORKDIR /app/$PROJECT
RUN mvn package
RUN mvn test -Dtest=GitProtocolImplSimulation

FROM openjdk:8-jre-alpine
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0
ENV PROJECT=gitprotocol_sf
ENV JAR=gitprotocol_sf-1.0-jar-with-dependencies.jar
COPY --from=mvn /app/$PROJECT/target/$JAR /app

CMD /usr/bin/java -jar $JAR -m $MASTERIP -id $ID