FROM maven:3.9.9-eclipse-temurin-21 AS dev

RUN apt-get update \
    && apt-get install -y --no-install-recommends entr \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
EXPOSE 8080

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY maven-settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package && cp target/*.jar app.jar

FROM eclipse-temurin:21-jre AS prod
RUN useradd --system --no-create-home spring
WORKDIR /app
COPY --from=build /build/app.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
