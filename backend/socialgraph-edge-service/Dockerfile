FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xms64m -Xmx384m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Copy only modules that exist in this repository snapshot.
COPY pom.xml .
COPY shared-proto shared-proto
COPY socialgraph-edge-service socialgraph-edge-service
COPY user-service/pom.xml user-service/pom.xml
COPY socialgraph-service/pom.xml socialgraph-service/pom.xml
COPY user-edge-service/pom.xml user-edge-service/pom.xml

RUN mvn clean package -pl socialgraph-edge-service -am -DskipTests && \
    rm -f socialgraph-edge-service/target/*-plain.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/socialgraph-edge-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
