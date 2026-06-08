# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Certificado TLS autofirmado para la demo (en local con mkcert ver README).
RUN keytool -genkeypair -alias api -keyalg RSA -keysize 2048 -storetype PKCS12 \
    -keystore /app/keystore.p12 -validity 3650 -storepass changeit \
    -dname "CN=localhost, OU=Demo, O=Demo, L=Demo, ST=Demo, C=ES"

COPY --from=build /app/target/*.jar app.jar

ENV SSL_KEY_STORE=file:/app/keystore.p12
ENV SSL_KEY_STORE_PASSWORD=changeit

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
