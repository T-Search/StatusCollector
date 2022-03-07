### Build Stage ###
FROM maven:3-openjdk-17-slim AS build
WORKDIR /opt/statuscollector/
ADD . .
RUN cp -f src/main/resources/application.properties.sample src/main/resources/application.properties
RUN mvn package

### Run Stage ###
FROM openjdk:17-slim
WORKDIR /opt/statuscollector/
COPY --from=build /opt/statuscollector/target/StatusCollector.jar .

EXPOSE 8080
ENTRYPOINT ["java","-jar","StatusCollector.jar"]