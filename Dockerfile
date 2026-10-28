# ==============================================================================
# Dockerfile for MedCare - Hospital Management System (HMS)
# Runtime: Eclipse Temurin OpenJDK 17 (Alpine)
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="MedCare HMS Team"
WORKDIR /app

# Create upload directory for lab reports and medical scan attachments
RUN mkdir -p /app/uploads/lab-reports

# Copy pre-packaged executable fat JAR
COPY target/hospital-management-system-1.0.0.jar app.jar

# Expose application port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/login || exit 1

# Environment variables (overridable at deployment)
ENV DB_URL=""
ENV DB_USERNAME="root"
ENV DB_PASSWORD=""
ENV DDL_AUTO="update"
ENV JAVA_OPTS="-Xms256m -Xmx768m"

# Execute application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
