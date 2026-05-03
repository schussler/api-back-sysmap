# SysmapHub API

API REST para a Plataforma de Atividades do Bootcamp 2026-1.

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) e Docker Compose
- [Java 24](https://openjdk.org/projects/jdk/24/)
- [Maven 3.9+](https://maven.apache.org/download.cgi)
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) (para criar o bucket no LocalStack)

## Configuração

```bash
# 1. Clone o repositório
git clone https://github.com/schussler/api-back-sysmap
cd sysmap-hub-api

# 2. Copie e edite as variáveis de ambiente
cp env.example .env
```

## Execução com Docker Compose (recomendado)

```bash
# 1. Suba o banco de dados e o LocalStack
docker-compose up -d postgres localstack

# 2. Crie o bucket S3 no LocalStack
aws --endpoint-url=http://localhost:4566 s3 mb s3://platform-images --region us-east-1

# 3. Suba a aplicação (build automático)
docker-compose up --build app
```

A API estará disponível em `http://localhost:8080`.

## Execução local (sem Docker para a aplicação)

```bash
# 1. Suba apenas a infra
docker-compose up -d postgres localstack

# 2. Crie o bucket S3
aws --endpoint-url=http://localhost:4566 s3 mb s3://platform-images --region us-east-1

# 3. Rode a aplicação
mvn spring-boot:run
```

## Swagger / Documentação

Acesse a documentação interativa em:

```
http://localhost:8080/swagger-ui.html
```

Use o botão **Authorize** para inserir o token JWT obtido no endpoint `POST /auth/sign-in`.

## Testes

```bash
mvn test
```

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 24 |
| Spring Boot | 4.0.0 |
| PostgreSQL | 16 |
| Liquibase | 5.x |
| LocalStack (S3) | latest |
| JWT (jjwt) | 0.12.x |

## Endpoints principais

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/auth/register` | Cadastrar usuário | Não |
| POST | `/auth/sign-in` | Login — retorna JWT | Não |
| GET | `/user` | Dados do usuário logado | Sim |
| GET | `/activities/types` | Tipos de atividade | Sim |
| GET | `/activities` | Listagem paginada com filtros | Sim |
| POST | `/activities/new` | Criar atividade (multipart) | Sim |
| POST | `/activities/{id}/subscribe` | Inscrever-se em atividade | Sim |
| PUT | `/activities/{id}/check-in` | Confirmar presença | Sim |
