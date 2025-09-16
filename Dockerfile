# Multi-stage build for production deployment
# Build stage
FROM maven:3.9.4-eclipse-temurin-8 AS build

LABEL stage=builder

# Set working directory
WORKDIR /build

# Copy Maven files for dependency caching
COPY pom.xml .

# Copy source code
COPY src ./src

# Build application with dependency resolution
RUN mvn clean package -DskipTests -B -U

# Verify WAR file was created
RUN ls -la target/ && test -f target/ROOT.war

# Production stage
FROM tomcat:8.5.93-jdk8-temurin

LABEL maintainer="IoT Demo Team" \
      description="IoT Demo Application with EMQX Cloud Integration" \
      version="1.0.0"

# Remove default Tomcat applications for security
RUN rm -rf /usr/local/tomcat/webapps/* && \
    rm -rf /usr/local/tomcat/temp/* && \
    rm -rf /usr/local/tomcat/work/*

# Set JVM options for production
ENV CATALINA_OPTS="-server -Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication" \
    JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true"

# Create non-root user for security
RUN groupadd -r iotdemo && useradd -r -g iotdemo iotdemo

# Copy WAR file from build stage
COPY --from=build /build/target/ROOT.war /usr/local/tomcat/webapps/

# Set ownership and permissions
RUN chown -R iotdemo:iotdemo /usr/local/tomcat && \
    chmod -R 755 /usr/local/tomcat

# Switch to non-root user
USER iotdemo

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Start Tomcat
CMD ["catalina.sh", "run"]