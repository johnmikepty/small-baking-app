# Small Banking App

A simplified online banking system built as a technical interview challenge.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 + Spring Boot 3 |
| Architecture | Clean Architecture + Mediator Pattern |
| SQL DB | PostgreSQL 16 (Flyway migrations) |
| NoSQL DB | MongoDB 7 |
| Messaging | Apache Kafka |
| Frontend | React + TypeScript (Vite) |
| Auth | Spring Security + JWT |
| Testing | JUnit 5 + Mockito + Testcontainers |
| API Docs | SpringDoc OpenAPI (Swagger UI) |

## Architecture

```
presentation/   → REST Controllers, DTOs, GlobalExceptionHandler
application/    → Mediator, Commands, Queries, Handlers (use cases)
domain/         → Entities, Value Objects, domain exceptions, repository interfaces
infrastructure/ → JPA repositories, MongoDB repos, Kafka producer/consumer, JWT, config
```

## Quick Start

```bash
# 1. Copy environment file
cp .env.example .env

# 2. Start everything
docker-compose up --build

# 3. Access
#   API:     http://localhost:8080
#   Swagger: http://localhost:8080/swagger-ui.html
#   Frontend: http://localhost:80
```

## Running Tests

```bash
cd backend
mvn test
```

> Testcontainers requires Docker running locally.

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_USER` | `banking` | PostgreSQL username |
| `DB_PASSWORD` | `banking` | PostgreSQL password |
| `JWT_SECRET` | *(see .env.example)* | HS256 signing key (min 32 chars) |
| `JWT_EXPIRATION_MS` | `3600000` | Token TTL in ms (1 hour) |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |

## Test Credentials (dev profile / mock data)

| Email | Password |
|---|---|
| ihernandez@email.com | Isabel2024! |
| mjimenez@example.com | Miguel2024! |
| paulamolina@mail.com | Paula2024! |

## API Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login → returns JWT |
| POST | `/api/auth/logout` | Logout (client-side) |
| GET | `/api/accounts/{id}` | Get account info |
| GET | `/api/accounts/{id}/balance` | Get balance |
| POST | `/api/transactions/deposit` | Deposit funds |
| POST | `/api/transactions/withdraw` | Withdraw funds |
| POST | `/api/transactions/transfer` | Transfer between accounts |
| GET | `/api/transactions/history` | Transaction history (paginated) |

## Kafka Events

Topic: `banking.transactions`

```json
{
  "transactionId": "uuid",
  "accountId": "uuid",
  "type": "DEPOSIT | WITHDRAWAL | TRANSFER",
  "amount": 100.00,
  "currency": "USD",
  "status": "COMPLETED",
  "timestamp": "2025-01-01T00:00:00Z"
}
```

Dead-letter topic: `banking.transactions.DLT`
