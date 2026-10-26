# ----- Build stage -----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
COPY .mvn/ .mvn/
RUN mvn -B dependency:go-offline

# Build the application
COPY src/ src/
RUN mvn -B clean package -DskipTests

# ----- Runtime stage -----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create a non-root user
RUN useradd --no-create-home --shell /usr/sbin/nologin appuser

COPY --from=build /app/target/*.jar app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
