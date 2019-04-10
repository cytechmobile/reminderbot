FROM maven:3-jdk-11 as builder
ADD . /app
RUN cd /app && mvn clean package -DskipTests=true

FROM openjdk:11-jre
RUN mkdir /reminder
COPY --from=builder /app/target/reminder-thorntail.jar /reminder/reminder.jar
ENV JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true"
EXPOSE 8080
CMD java $JAVA_OPTS -jar /reminder/reminder.jar
