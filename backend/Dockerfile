# --- Imagen de build ---
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Cache de dependencias
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Imagen de ejecucion ---
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN mkdir -p /app/logs /app/uploads

COPY --from=build /app/target/punto-venta-backend.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]