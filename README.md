# ✅ Desafio Fullstack – Sistema de Gestão de Escolas do Município de São Paulo

## 💡 Descrição

Aplicação fullstack completa para gestão de **instalações físicas das escolas do município de São Paulo**, desenvolvida como resposta ao teste prático para **Desenvolvedor Fullstack**.

O sistema permite:
1. 📤 **Upload de arquivo CSV** com dados das instalações escolares
2. 💾 **Armazenamento** dos dados em base relacional PostgreSQL
3. 🔧 **CRUD completo** (Create, Read, Update, Delete) das escolas
4. 🔐 **Autenticação** por usuário e senha com JWT
5. 📊 **Processamento assíncrono** com monitoramento em tempo real

## 🚀 Tecnologias Utilizadas

### Backend

- **Java 21** + **Spring Boot 3.5** (Framework principal)
- **Spring Security** com JWT (Autenticação)
- **PostgreSQL** (Banco de dados relacional)
- **Spring Batch** (Processamento de CSV)
- **RabbitMQ** (Mensageria assíncrona)
- **WebSocket** (Notificações em tempo real)

### Observabilidade

- **Prometheus** (Métricas)
- **Grafana** (Dashboards)
- **Logback** (Logs estruturados)

### Arquitetura

- **Hexagonal Architecture** (Ports & Adapters)
- **Domain-Driven Design** (DDD)
- **Clean Code** e **SOLID**

## 🛠 Como Executar

### Pré-requisitos

- **Docker** e **Docker Compose** instalados
- **Git** para clonar o repositório

### 🐳 Execução Completa com Docker (Recomendado)

```bash
# 1. Clone o repositório
git clone https://github.com/kleberrhuan/School-Crud-Api.git
cd School-Crud-Api

# 2. Configure as variáveis de ambiente
cp .env.example .env

# 3. Inicie todos os serviços com Docker Compose
docker-compose up -d

# 4. Aguarde inicialização (30-60 segundos) e acesse:
```

### 🌐 URLs dos Serviços

|         Serviço         |                  URL                  |        Descrição         |
|-------------------------|---------------------------------------|--------------------------|
| **API Principal**       | http://localhost:8080                 | Aplicação Spring Boot    |
| **Swagger/OpenAPI**     | http://localhost:8080/swagger-ui.html | Documentação da API      |
| **RabbitMQ Management** | http://localhost:15672                | Gerenciamento de filas   |
| **Prometheus**          | http://localhost:9090                 | Métricas da aplicação    |
| **Grafana**             | http://localhost:3000                 | Dashboards (admin/admin) |

### 🔧 Desenvolvimento Local (Opcional)

```bash
# 1. Inicie apenas as dependências
docker-compose up -d postgres rabbitmq prometheus grafana

# 2. Configure variáveis de ambiente
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/houer
export JDBC_DATABASE_USERNAME=postgres
export JDBC_DATABASE_PASSWORD=Teste123@

# 3. Execute a aplicação
./mvnw spring-boot:run
```

## 📌 Funcionalidades Implementadas

### ✅ Funcionalidades Obrigatórias

#### 📤 **Upload de CSV**

- ✅ Interface para upload de arquivos CSV
- ✅ Parsing e validação dos dados
- ✅ Processamento assíncrono com Spring Batch
- ✅ Prevenção de registros duplicados/inválidos
- ✅ Feedback em tempo real via WebSocket

#### 🏫 **CRUD de Escolas**

- ✅ **Listagem** paginada com filtros
- ✅ **Criação** de novas escolas
- ✅ **Edição** de dados existentes
- ✅ **Exclusão** com soft delete
- ✅ **Busca avançada** por múltiplos critérios

### ⭐ **Diferenciais Implementados**

#### 🔐 **Autenticação Completa**

- ✅ Sistema de login/logout com JWT
- ✅ Registro de usuários com verificação por email
- ✅ Refresh tokens seguros
- ✅ Rate limiting e proteção CSRF
- ✅ Blacklist de tokens em cache

#### 🐳 **Docker & Observabilidade**

- ✅ Docker Compose para ambiente completo
- ✅ Monitoramento com Prometheus/Grafana
- ✅ Logs estruturados e correlacionados
- ✅ Health checks automatizados

#### 🧪 **Qualidade & CI/CD**

- ✅ Testes automatizados (80%+ cobertura)
- ✅ GitHub Actions para CI/CD
- ✅ SonarQube para qualidade de código
- ✅ Dockerfile otimizado para produção

## 🔐 Usuário de Teste

> ⚠️ **Observação importante:** para simplificar o teste técnico, **todo usuário registrado pela API já é criado com a permissão `ADMIN`** e, portanto, possui acesso total a todos os endpoints protegidos.

### Criação de Usuário

```bash
# Registrar novo usuário
curl -X POST http://localhost:8080/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin Teste",
    "email": "admin@teste.com", 
    "password": "Teste123@"
  }'
```

### Login

```bash
# Fazer login
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@teste.com",
    "password": "Teste123@"
  }'
```

**💡 Dica:** Use o Swagger UI em http://localhost:8080/swagger-ui.html para testar a API interativamente!

## 📊 Como Usar o Sistema

### 1️⃣ **Upload de CSV de Escolas**

```bash
# Endpoint para upload
POST /v1/csv/import

# Exemplo com curl
curl -X POST http://localhost:8080/v1/csv/import \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -F "file=@dados_escolas.csv" \
  -F "description=Importação inicial das escolas"
```

### 2️⃣ **Gestão de Escolas**

```bash
# Listar escolas (paginado)
GET /v1/schools?page=0&size=10&sort=name

# Criar nova escola
POST /v1/schools
{
  "name": "EMEF Nova Escola",
  "address": "Rua das Flores, 123",
  "district": "Vila Madalena",
  "metrics": {
    "totalStudents": 500,
    "totalTeachers": 25
  }
}

# Atualizar escola
PUT /v1/schools/{id}

# Excluir escola
DELETE /v1/schools/{id}
```

### 3️⃣ **Monitoramento em Tempo Real**

- **WebSocket**: Conecte em `ws://localhost:8080/ws/csv-progress` para receber updates do processamento
- **Métricas**: Acesse http://localhost:9090 para ver métricas detalhadas
- **Dashboards**: Visualize em http://localhost:3000 (admin/admin)

## 📖 Documentação da API

### Swagger/OpenAPI

Acesse a documentação completa e interativa em: **http://localhost:8080/swagger-ui.html**

### Principais Endpoints

|  Método  |      Endpoint       |     Descrição      | Autenticação |
|----------|---------------------|--------------------|--------------|
| `POST`   | `/v1/auth/register` | Registrar usuário  | ❌            |
| `POST`   | `/v1/auth/login`    | Fazer login        | ❌            |
| `POST`   | `/v1/auth/logout`   | Fazer logout       | ✅            |
| `POST`   | `/v1/csv/import`    | Upload CSV escolas | ✅            |
| `GET`    | `/v1/csv/jobs`      | Status dos imports | ✅            |
| `GET`    | `/v1/schools`       | Listar escolas     | ✅            |
| `POST`   | `/v1/schools`       | Criar escola       | ✅            |
| `PUT`    | `/v1/schools/{id}`  | Atualizar escola   | ✅            |
| `DELETE` | `/v1/schools/{id}`  | Excluir escola     | ✅            |

## 🏗️ Arquitetura do Sistema

```
src/main/java/com/kleberrhuan/houer/
├── auth/                    # 🔐 Módulo de Autenticação
│   ├── application/         #   Casos de uso (login, registro)
│   ├── domain/             #   Entidades (User, Token)  
│   ├── infra/              #   JWT, Security Config
│   └── interfaces/         #   AuthController, DTOs
│
├── school/                 # 🏫 Módulo de Escolas  
│   ├── application/        #   Casos de uso (CRUD)
│   ├── domain/            #   Entidade School
│   ├── infra/             #   Repository, Persistence
│   └── interfaces/        #   SchoolController, DTOs
│
├── csv/                   # 📊 Módulo de Processamento CSV
│   ├── application/       #   Batch Jobs, Validators
│   ├── domain/           #   CsvImportJob, Status
│   ├── infra/            #   Spring Batch Config
│   └── interfaces/       #   CsvController, WebSocket
│
└── common/               # 🔧 Código Compartilhado
    ├── application/      #   Serviços base
    ├── infra/           #   Configs, Exception Handlers
    └── interfaces/      #   DTOs base, Documentação
```

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Gerar relatório de cobertura
./mvnw verify jacoco:report

# Ver cobertura (mínimo 80%)
open target/site/jacoco/index.html
```

## 📊 Fonte dos Dados

**CSV - Instalações Físicas por Unidade Escolar – Município de São Paulo**

O sistema processa arquivos CSV com as seguintes colunas esperadas:
- `NOME_ESCOLA` - Nome da instituição
- `ENDERECO` - Endereço completo
- `DISTRITO` - Distrito/região
- `TIPO_ESCOLA` - Categoria (EMEF, EMEI, etc.)
- `TOTAL_ALUNOS` - Número de estudantes
- `TOTAL_PROFESSORES` - Número de docentes

## 🚀 Deploy e Produção

### Docker em Produção

```bash
# Build otimizado
docker build -t school-api:prod .

# Deploy com compose
docker-compose -f docker-compose.prod.yml up -d
```

### Variáveis de Ambiente Importantes

```env
# Banco de Dados
JDBC_DATABASE_URL=jdbc:postgresql://postgres:5432/houer
JDBC_DATABASE_USERNAME=postgres
JDBC_DATABASE_PASSWORD=SuaSenhaSegura

# JWT (gere chaves próprias!)
JWT_ACCESS_EXPIRATION=900
JWT_REFRESH_EXPIRATION=86400
JWT_ISSUER=https://sua-api.com

# Email (configure seu provedor)
BREVO_API_KEY=sua_chave_brevo
BREVO_EMAIL=noreply@sua-empresa.com
```

## 🧪 Critérios de Avaliação Atendidos

|              Critério              | Status |           Implementação            |
|------------------------------------|--------|------------------------------------|
| **Arquitetura e organização** ⭐⭐⭐⭐ | ✅      | Hexagonal + DDD + Clean Code       |
| **Fidelidade ao escopo** ⭐⭐⭐⭐      | ✅      | Todos os requisitos + diferenciais |
| **Usabilidade da interface** ⭐⭐⭐   | ✅      | Swagger UI + documentação clara    |
| **Boas práticas** ⭐⭐⭐⭐             | ✅      | REST, Security, Validações         |
| **Upload e CRUD funcionais** ⭐⭐⭐⭐  | ✅      | Processamento assíncrono + CRUD    |
| **Documentação** ⭐⭐⭐               | ✅      | README completo + OpenAPI          |
| **Git com histórico claro** ⭐⭐     | ✅      | Commits semânticos organizados     |
| **Autenticação** ⭐⭐                | ✅      | JWT + Security completa            |

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit as mudanças (`git commit -m 'feat: adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👨‍💻 Desenvolvedor

**Kleber Rhuan** - Desenvolvedor Fullstack
- 📧 Email: kleberrhuan12@gmail.com
- 💼 LinkedIn: [linkedin.com/in/kleberrhuan](https://linkedin.com/in/kleberrhuan)
- 🌐 Portfolio: [kleber.rhuan.cloud](https://kleber.rhuan.cloud)

---

✨ **Desenvolvido como resposta ao Teste Prático – Desenvolvedor Fullstack da Houer**

⭐ **Se este projeto demonstrou as competências esperadas, considere uma avaliação positiva!**
