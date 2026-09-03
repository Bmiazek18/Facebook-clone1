FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xms64m -Xmx384m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Copy only modules that exist in this repository snapshot.
COPY pom.xml .
COPY shared-proto shared-proto
COPY user-service user-service
COPY user-edge-service/pom.xml user-edge-service/pom.xml
COPY socialgraph-service/pom.xml socialgraph-service/pom.xml
COPY socialgraph-edge-service/pom.xml socialgraph-edge-service/pom.xml
COPY search-service/pom.xml search-service/pom.xml
COPY feed-service/pom.xml feed-service/pom.xml
COPY feed-edge-service/pom.xml feed-edge-service/pom.xml
COPY chat-service/pom.xml chat-service/pom.xml
COPY chat-edge-service/pom.xml chat-edge-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY analytics-service/pom.xml analytics-service/pom.xml
COPY groups-service/pom.xml groups-service/pom.xml
COPY marketplace-service/pom.xml marketplace-service/pom.xml
COPY marketplace-edge-service/pom.xml marketplace-edge-service/pom.xml
RUN mvn clean package -pl user-service -am -DskipTests && \
    rm -f user-service/target/*-plain.jar

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
