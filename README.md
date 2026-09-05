# EasyNotes API

API REST desenvolvida para a aplicação Android EasyNotes.

## Tecnologias utilizadas

- Kotlin
- Ktor Server
- JWT
- BCrypt
- Exposed
- PostgreSQL
- H2
- Swagger / OpenAPI
- Kotlinx Serialization

## Base de dados

Durante o desenvolvimento local é utilizada uma base de dados H2.

Em produção é utilizada uma base de dados PostgreSQL.

A configuração da base de dados de produção é obtida através da variável de ambiente:

`DATABASE_URL`

## Autenticação

A API utiliza autenticação através de JWT.

O segredo utilizado para assinar os tokens é obtido através da variável de ambiente:

`JWT_SECRET`

As passwords dos utilizadores são guardadas através de hash BCrypt.

## Endpoints principais

### Autenticação

- `POST /auth/register` - registar utilizador
- `POST /auth/login` - iniciar sessão
- `GET /me` - obter utilizador autenticado

### Notas

- `GET /notes` - listar notas
- `POST /notes` - criar nota
- `PUT /notes/{id}` - editar nota
- `DELETE /notes/{id}` - apagar nota

As operações sobre notas requerem autenticação JWT e cada utilizador apenas pode aceder às suas próprias notas.

## Outros endpoints

- `GET /` - identificação da API
- `GET /health` - estado da API
- `/swagger` - documentação Swagger

## Executar localmente

No Windows:

```powershell
.\gradlew.bat run