FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xms64m -Xmx384m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

COPY pom.xml .
COPY shared-proto shared-proto
COPY chat-edge-service chat-edge-service
COPY user-service/pom.xml user-service/pom.xml
COPY chat-service/pom.xml chat-service/pom.xml
COPY search-service/pom.xml search-service/pom.xml
COPY groups-service/pom.xml groups-service/pom.xml
COPY socialgraph-service/pom.xml socialgraph-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY feed-service/pom.xml feed-service/pom.xml
COPY user-edge-service/pom.xml user-edge-service/pom.xml
COPY socialgraph-edge-service/pom.xml socialgraph-edge-service/pom.xml
COPY feed-edge-service/pom.xml feed-edge-service/pom.xml
COPY analytics-service/pom.xml analytics-service/pom.xml

RUN mvn clean package -pl chat-edge-service -am -DskipTests && \
    rm -f chat-edge-service/target/*-plain.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/chat-edge-service/target/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
