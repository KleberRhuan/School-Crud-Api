# Houer - Sistema de Gestão Escolar

## 💡 Descrição
Sistema fullstack completo para gestão de instalações escolares com autenticação JWT, upload de arquivos, CRUD de dados e sistema de notificações por email. Desenvolvido com Spring Boot e arquitetura hexagonal.

## 🚀 Tecnologias

### Backend
- **Java 21** + **Spring Boot 3.4**
- **Spring Security** com JWT
- **PostgreSQL** + **Flyway** (migrações)
- **Spring Data JPA** + **Hibernate**
- **Caffeine Cache** (cache em memória)
- **Brevo API** (envio de emails)
- **OpenAPI/Swagger** (documentação)
- **Docker** + **Docker Compose**

### Observabilidade
- **Prometheus** (métricas)
- **Jaeger** (tracing distribuído)
- **Logback** (logs estruturados)

### Arquitetura
- **Hexagonal Architecture** (Ports & Adapters)
- **Domain-Driven Design** (DDD)
- **Outbox Pattern** (eventos assíncronos)
- **Soft Delete** + **Auditoria automática**

## 🛠 Como Executar

### Pré-requisitos
- Docker e Docker Compose
- Java 21+ (opcional, para desenvolvimento)
- Maven 3.9+ (opcional, para desenvolvimento)

### 🐳 Execução com Docker (Recomendado)

```bash
# Clone o repositório
git clone https://github.com/kleberrhuan/houer.git
cd houer

# Inicie todos os serviços
docker-compose up -d

# Aguarde alguns segundos e acesse:
# - API: http://localhost:8080
# - Swagger: http://localhost:8080/swagger-ui.html
# - Prometheus: http://localhost:9090
# - Jaeger: http://localhost:16686
```

### 🔧 Desenvolvimento Local

```bash
# Clone o repositório
git clone https://github.com/kleberrhuan/houer.git
cd houer

# Configure o banco PostgreSQL
docker-compose up -d postgres

# Configure as variáveis de ambiente
cp .env.example .env
# Edite o arquivo .env conforme necessário

# Gere os certificados JWT
chmod +x scripts/generate-certificates.sh
./scripts/generate-certificates.sh

# Execute a aplicação
./mvnw spring-boot:run
```

### ⚙️ Configuração do .env

```env
# Banco de Dados
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/houer
JDBC_DATABASE_USERNAME=postgres
JDBC_DATABASE_PASSWORD=Teste123@

# JWT
JWT_ACCESS_EXPIRATION=900
JWT_REFRESH_EXPIRATION=86400
JWT_ISSUER=http://localhost:8080

# Email (Brevo)
BREVO_API_KEY=sua_chave_aqui
BREVO_NAME=houer
BREVO_EMAIL=seu_email@exemplo.com

# Frontend URL
HOUER_RESEARCH_URL=http://localhost:8080
```

## 📌 Funcionalidades

### 🔐 Autenticação & Autorização
- ✅ **Login/Logout** com JWT
- ✅ **Registro de usuários** com verificação por email
- ✅ **Refresh tokens** com cookies httpOnly
- ✅ **Rate limiting** por IP e endpoint
- ✅ **Blacklist de tokens** em cache
- ✅ **Validação robusta de senhas**

### 👥 Gestão de Usuários
- ✅ **CRUD completo** de usuários
- ✅ **Soft delete** com auditoria
- ✅ **Perfis e permissões**
- ✅ **Histórico de alterações**

### 📧 Sistema de Notificações
- ✅ **Outbox pattern** para garantia de entrega
- ✅ **Templates HTML** para emails
- ✅ **Retry automático** para falhas
- ✅ **Integração com Brevo**

### 📊 Observabilidade
- ✅ **Métricas** com Prometheus
- ✅ **Tracing distribuído** com Jaeger
- ✅ **Logs estruturados** com correlação
- ✅ **Health checks** e actuator

### 📚 Documentação
- ✅ **Swagger/OpenAPI** completo
- ✅ **Schemas reutilizáveis**
- ✅ **Exemplos de requisições**
- ✅ **Documentação de arquitetura**

## 🔐 Usuário de Teste

Após executar a aplicação, registre um novo usuário através do endpoint:

```bash
POST /v1/auth/register
{
  "name": "Admin Teste",
  "email": "admin@teste.com",
  "password": "Teste123@"
}
```

Verifique o email (ou logs da aplicação) e faça login:

```bash
POST /v1/auth/login
{
  "email": "admin@teste.com",
  "password": "Teste123@",
  "rememberMe": false
}
```

## 📖 Documentação da API

Acesse a documentação interativa em: **http://localhost:8080/swagger-ui.html**

### Principais Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/v1/auth/login` | Realizar login |
| `POST` | `/v1/auth/register` | Registrar usuário |
| `GET` | `/v1/auth/verify` | Verificar email |
| `POST` | `/v1/auth/refresh` | Renovar token |
| `POST` | `/v1/auth/logout` | Fazer logout |

## 🏗️ Arquitetura

```
src/main/java/com/kleberrhuan/houer/
├── auth/                    # Módulo de Autenticação
│   ├── application/         # Casos de uso
│   ├── domain/             # Entidades e regras de negócio
│   ├── infra/              # Implementações técnicas
│   └── interfaces/         # Controllers e DTOs
├── user/                   # Módulo de Usuários
├── common/                 # Código compartilhado
│   ├── application/        # Serviços compartilhados
│   ├── domain/            # Exceções e interfaces
│   ├── infra/             # Configurações e implementações
│   └── interfaces/        # DTOs e documentação
└── HouerApplication.java   # Classe principal
```

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar com coverage
./mvnw test jacoco:report
```

## 🚀 Deploy

### Docker Production

```bash
# Build da imagem
docker build -t houer:latest .

# Executar em produção
docker run -d \
  --name houer-app \
  -p 8080:8080 \
  --env-file .env.prod \
  houer:latest
```

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'feat: add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👨‍💻 Autor

**Kleber Rhuan**
- Email: kleber_rhuan@hotmail.com
- LinkedIn: [linkedin.com/in/kleberrhuan](https://linkedin.com/in/kleberrhuan)
- Website: [kleber.rhuan.cloud](https://kleber.rhuan.cloud)

---

⭐ **Se este projeto te ajudou, deixe uma estrela!** 