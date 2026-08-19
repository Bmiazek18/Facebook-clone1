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

RUN mvn clean package -pl user-service -am -DskipTests && \
    rm -f user-service/target/*-plain.jar

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
