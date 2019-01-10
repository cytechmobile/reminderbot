#using maven 3.5.x until https://github.com/thorntail/thorntail/pull/1195 comes up
FROM maven:3.5-alpine as builder
ADD . /app
RUN cd /app && mvn clean package -DskipTests=true

FROM openjdk:8-jre
RUN mkdir /reminder
COPY --from=builder /app/target/reminder-thorntail.jar /reminder/reminder.jar
ENV JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true"
EXPOSE 8080
CMD java $JAVA_OPTS -jar /reminder/reminder.jar
