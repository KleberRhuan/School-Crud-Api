# ---------- build stage ------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copie apenas o POM primeiro para aproveitar cache de dependências
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

# Instalar OpenSSL para gerar certificados
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openssl \
    nodejs \
    npm \
    && rm -rf /var/lib/apt/lists/*

# Criar diretório para certificados
RUN mkdir -p /workspace/src/main/resources/certificates

# Gerar chaves RSA
RUN openssl genrsa -out /workspace/src/main/resources/certificates/private.pem 2048 && \
    openssl rsa -in /workspace/src/main/resources/certificates/private.pem -pubout -out /workspace/src/main/resources/certificates/public.pub && \
    chmod 644 /workspace/src/main/resources/certificates/private.pem && \
    chmod 644 /workspace/src/main/resources/certificates/public.pub

# Agora copie o código-fonte
COPY src ./src

# Compile e gere um "fat-jar" camadas (Spring Boot 3+ gera layers por padrão)
RUN mvn -B -ntp clean package -DskipTests

# Verificar se o JAR foi gerado corretamente
RUN ls -la /workspace/target/

# ---------- runtime stage ----------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime 
WORKDIR /app

# Instalar curl para o healthcheck
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Crie um usuário sem privilégios
RUN useradd --system --home /app --shell /sbin/nologin spring

# Criar diretórios necessários para a aplicação
RUN mkdir -p /app/logs && \
    chmod 777 /app/logs

# Copie o JAR com o nome exato
COPY --from=build /workspace/target/houer-1.0.0.jar /app/app.jar

# Ajustar permissões do JAR
RUN chmod +x /app/app.jar && \
    chown -R spring:spring /app

# Variáveis default (podem ser sobrepostas por .env ou docker-compose)
ENV SPRING_PROFILES_ACTIVE=dev 

# Porta exposta
EXPOSE 8080

# Healthcheck simples (opcional)
HEALTHCHECK CMD curl --fail http://localhost:8080/actuator/health || exit 1

# Troque para o usuário não-root
USER spring

# Comando de entrada
ENTRYPOINT ["java", "-jar", "/app/app.jar"]