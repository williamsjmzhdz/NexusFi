# NexusFi Dockerfile
# Multi-stage build for optimized production image

# ============================================
# Stage 1: Build the application
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for layer caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests - they require DB)
RUN ./mvnw clean package -DskipTests -B

# ============================================
# Stage 2: Create minimal runtime image
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 nexusfi && \
    adduser -u 1001 -G nexusfi -D nexusfi

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R nexusfi:nexusfi /app

USER nexusfi

# Expose port (Railway uses PORT env var)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/api/v1/auth/health || exit 1

# Run with production profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
