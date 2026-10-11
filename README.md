# 🔗 Shortify API

API RESTful de encurtamento de URLs, com CRUD completo e estatísticas de acesso.

## ✨ Funcionalidades

- Criar uma short URL a partir de uma URL longa
- Consultar os dados de uma short URL
- Redirecionar para a URL original
- Atualizar a URL original de um código existente
- Remover uma short URL
- Consultar estatísticas de acesso (número de vezes que foi acessada)

## 🧱 Arquitetura

O projeto segue **arquitetura hexagonal (ports & adapters)**, isolando as regras de negócio de frameworks e infraestrutura:

```
domain/
  model/        → entidades de domínio puras
  exception/    → exceções de negócio

application/
  port/in/      → casos de uso (interfaces)
  port/out/     → contratos para infraestrutura (interfaces)
  service/      → implementação dos casos de uso

adapter/
  in/web/       → controllers, DTOs, tratamento de erros
  out/persistence/ → entidades JPA, repositórios Spring Data
  out/codegen/  → geração do código curto
```

## 🛠️ Stack

- Java 21
- Spring Boot 4.1.1 (Web, Validation, Data JPA)
- MySQL
- Liquibase (versionamento de schema)
- JUnit 5 + Mockito
- Testcontainers (MySQL) para testes de integração

## 📡 Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/shorten` | Cria uma short URL |
| GET | `/shorten/{code}` | Retorna os dados da short URL |
| GET | `/{code}` | Redireciona para a URL original |
| PUT | `/shorten/{code}` | Atualiza a URL original |
| DELETE | `/shorten/{code}` | Remove a short URL |
| GET | `/shorten/{code}/stats` | Retorna estatísticas de acesso |

## 🚀 Como executar

```bash
# Subir o banco MySQL (ajuste conforme seu ambiente)
./mvnw spring-boot:run
```

As migrations do Liquibase são executadas automaticamente na inicialização.

## 🧪 Testes

```bash
./mvnw test
```