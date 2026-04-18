# ===============================
# Stage 1: Build
# ===============================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first (better caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# ===============================
# Stage 2: Run
# ===============================
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Render dynamic port support
ENV PORT=8080

EXPOSE 8080

# 🔥 IMPORTANT FIX
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
