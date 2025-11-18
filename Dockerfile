# Multi-stage Docker build for SiteWatch

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /build

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
COPY checkstyle.xml checkstyle-suppressions.xml pmd-ruleset.xml ./
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S sitewatch && adduser -S sitewatch -G sitewatch

# Copy JAR from build stage
COPY --from=build /build/target/*.jar app.jar

# Set ownership
RUN chown -R sitewatch:sitewatch /app

# Switch to non-root user
USER sitewatch

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
