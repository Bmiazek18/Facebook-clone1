FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xms64m -Xmx384m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Copy parent POM and module directories
COPY pom.xml .
COPY shared-proto shared-proto
COPY search-service search-service
COPY user-service/pom.xml user-service/pom.xml
COPY user-edge-service/pom.xml user-edge-service/pom.xml
COPY socialgraph-service/pom.xml socialgraph-service/pom.xml
COPY socialgraph-edge-service/pom.xml socialgraph-edge-service/pom.xml

# Build only the search-service and its dependencies
RUN mvn clean package -pl search-service -am -DskipTests && \
    rm -f search-service/target/*-plain.jar

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/search-service/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
