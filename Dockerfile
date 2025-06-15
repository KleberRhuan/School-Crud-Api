# ---------- build stage ------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

# Instalar OpenSSL para gerar certificados
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    nodejs \
    npm \
    openssl \
    && rm -rf /var/lib/apt/lists/*

# Criar diretório para certificados
RUN mkdir -p /workspace/src/main/resources/certificates

# Gerar chaves RSA
RUN openssl genrsa -out /workspace/src/main/resources/certificates/private.pem 2048 && \
    openssl rsa -in /workspace/src/main/resources/certificates/private.pem  \
    -pubout -out /workspace/src/main/resources/certificates/public.pub && \
    chmod 644 /workspace/src/main/resources/certificates/private.pem && \
    chmod 644 /workspace/src/main/resources/certificates/public.pub

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests
RUN ls -la /workspace/target/

# ---------- runtime stage ----------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime 
WORKDIR /app

# Instalar curl para o healthcheck
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --home /app --shell /sbin/nologin spring
RUN mkdir -p /app/logs && \
    chmod 777 /app/logs

COPY --from=build /workspace/target/*.jar /app/app.jar

RUN chmod +x /app/app.jar && \
    chown -R spring:spring /app

ENV SPRING_PROFILES_ACTIVE=dev 

EXPOSE 8080

HEALTHCHECK CMD curl --fail http://localhost:8080/actuator/health || exit 1

USER spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]