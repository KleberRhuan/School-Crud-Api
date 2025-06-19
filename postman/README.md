# 📮 Coleção Postman - Houer School Management API

Esta coleção contém requests para testar as funcionalidades de criação e edição de escolas na API Houer School Management.

## 🚀 Como Importar

1. Abra o Postman
2. Clique em **Import**
3. Selecione o arquivo `School_API_Collection.json`
4. A coleção será importada com todas as pastas organizadas

## 🔧 Configuração Inicial

### Variáveis de Ambiente

Crie um ambiente no Postman com as seguintes variáveis:

|        Variável        |          Valor          |                       Descrição                        |
|------------------------|-------------------------|--------------------------------------------------------|
| `base_url`             | `http://localhost:8080` | URL base da API                                        |
| `jwt_token`            | (vazio inicialmente)    | Token JWT (preenchido automaticamente)                 |
| `refresh_token`        | (vazio inicialmente)    | Token de refresh (preenchido automaticamente)          |
| `created_school_code`  | (vazio inicialmente)    | Código da escola criada (preenchido automaticamente)   |
| `estadual_school_code` | (vazio inicialmente)    | Código da escola estadual (preenchido automaticamente) |

### Pré-requisitos

- API rodando em `localhost:8080`
- Usuário administrador criado com:
  - Email: `admin@houer.com`
  - Senha: `Admin123!`

## 📁 Estrutura da Coleção

### 🔐 Autenticação

- **Login Admin**: Autentica como administrador e salva o token JWT automaticamente

### 🏫 Escolas - CRUD

- **Criar Nova Escola**: Cria escola municipal com métricas básicas
- **Criar Escola Estadual**: Cria escola estadual com infraestrutura completa
- **Atualizar Escola Municipal**: Atualiza dados da escola criada
- **Atualizar Apenas Métricas**: Atualiza somente métricas da escola estadual
- **Buscar Escola Criada**: Verifica dados da escola criada
- **Listar Métricas Disponíveis**: Lista todas as métricas válidas

### ❌ Testes de Erro

- **Criar Escola - Dados Inválidos**: Testa validação com dados incorretos
- **Criar Escola - Código Duplicado**: Testa criação com código existente
- **Atualizar Escola Inexistente**: Testa atualização de escola que não existe
- **Criar Escola - Sem Autenticação**: Testa acesso sem token JWT

## 🎯 Como Usar

1. **Execute o Login Admin primeiro** para obter o token JWT
2. Os requests subsequentes usarão automaticamente o token salvo
3. As variáveis de ambiente são preenchidas automaticamente durante a execução
4. Execute os requests na ordem sugerida para melhores resultados

## 🧪 Scripts de Teste Automático

Cada request inclui testes automáticos que verificam:
- Status HTTP correto
- Estrutura da resposta
- Dados específicos retornados
- Salvamento automático de variáveis

## 📊 Métricas Disponíveis

As escolas podem ter as seguintes métricas (alguns exemplos):

### Salas e Espaços Educacionais

- `SALAS_AULA`: Salas de aula
- `BIBLIOTECA`: Biblioteca
- `SALA_LEITURA`: Sala de leitura
- `AUDITORIO`: Auditório
- `TEATRO`: Teatro

### Laboratórios

- `LAB_INFO`: Laboratório de informática
- `LAB_CIENCIAS`: Laboratório de ciências
- `LAB_FISICA`: Laboratório de física
- `LAB_QUIMICA`: Laboratório de química
- `LAB_BIOLOGIA`: Laboratório de biologia

### Esportes e Recreação

- `QUADRA_COBERTA`: Quadra coberta
- `QUADRA_DESCOBERTA`: Quadra descoberta
- `GINASIO`: Ginásio
- `PISCINA`: Piscina
- `PLAYGROUND`: Playground

### Alimentação

- `REFEITORIO`: Refeitório
- `COZINHA`: Cozinha
- `CANTINA`: Cantina

### Infraestrutura

- `VESTIARIO_FEM`: Vestiário feminino
- `VESTIARIO_MASC`: Vestiário masculino
- `SANITARIO_AL_FEM`: Sanitário alunos feminino
- `SANITARIO_AL_MASC`: Sanitário alunos masculino

> Use o request **"Listar Métricas Disponíveis"** para ver todas as métricas válidas

## 🔒 Autenticação e Autorização

- **POST/PUT/DELETE**: Requer token JWT de usuário ADMIN
- **GET**: Requer token JWT (qualquer usuário autenticado)
- **Métricas públicas**: Endpoint `/api/v1/schools/metrics` acessível com autenticação

## 📝 Exemplos de Payload

### Criação de Escola

```json
{
  "code": 12345678,
  "schoolName": "Escola Municipal Vila Nova",
  "administrativeDependency": "Municipal", 
  "stateCode": "SP",
  "municipality": "São Paulo",
  "district": "Vila Nova",
  "schoolType": 1,
  "schoolTypeDescription": "Pública Municipal",
  "situationCode": 1,
  "schoolCode": 12345678,
  "metrics": {
    "SALAS_AULA": 15,
    "BIBLIOTECA": 1,
    "QUADRA_COBERTA": 1,
    "LAB_INFO": 1,
    "REFEITORIO": 1
  }
}
```

### Atualização Parcial

```json
{
  "schoolName": "Escola Municipal Vila Nova - Atualizada",
  "metrics": {
    "SALAS_AULA": 18,
    "PLAYGROUND": 1,
    "SALA_LEITURA": 1
  }
}
```

## ⚠️ Observações Importantes

1. **Códigos únicos**: Cada escola deve ter um código único
2. **Métricas válidas**: Use apenas métricas listadas no endpoint de métricas
3. **Valores numéricos**: Métricas devem ser números inteiros não negativos
4. **Autenticação**: Token JWT expira - refaça login se necessário
5. **Dados obrigatórios**: `code` e `schoolName` são obrigatórios na criação

## 🐛 Troubleshooting

### Token JWT Inválido

- Execute novamente o request "Login Admin"
- Verifique se o usuário admin existe no banco

### Erro 400 - Bad Request

- Verifique se os dados estão no formato correto
- Confirme se o código da escola não já existe
- Valide se as métricas são válidas

### Erro 404 - Not Found

- Confirme se o código da escola existe para atualização
- Verifique se a URL está correta

### Erro 401 - Unauthorized

- Token JWT ausente ou inválido
- Execute o login admin novamente

