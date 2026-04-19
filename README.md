# Small Banking App

A simplified online banking system built as a technical interview challenge, demonstrating Clean Architecture, Mediator Pattern, dual-database strategy, event-driven messaging, and a modern frontend.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 + Spring Boot 3 |
| Architecture | Clean Architecture + Mediator Pattern |
| SQL DB | PostgreSQL 16 (Flyway migrations) |
| NoSQL DB | MongoDB 7 |
| Messaging | Apache Kafka (+ Dead-Letter Topic) |
| Frontend | Angular 17 + Tailwind CSS |
| Auth | Spring Security + JWT (HS256) |
| Testing | JUnit 5 + Mockito + Testcontainers + Awaitility |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Containerization | Docker + Docker Compose |

---

## Architecture

```
presentation/   → REST Controllers, DTOs, GlobalExceptionHandler
application/    → Mediator, Commands, Queries, Handlers (use cases)
domain/         → Entities, Value Objects, domain exceptions, repository interfaces
infrastructure/ → JPA repositories, MongoDB repos, Kafka producer/consumer, JWT, config
```

### Key Design Decisions

- **Mediator Pattern** — Custom `SpringMediator` scans the ApplicationContext for all `RequestHandler` beans at startup. No reflection magic, fully testable.
- **Dual DB consistency** — PostgreSQL is the source of truth. Every completed transaction publishes a Kafka event, consumed by two independent groups that write to MongoDB (`audit_logs` and `transaction_history`).
- **Idempotency** — All write commands accept an `idempotencyKey`. Duplicate requests return the existing result without side effects.
- **Error handling** — RFC-7807 `ProblemDetail` (Spring 6 native) for all error responses.
- **JWT** — Stateless HS256, 1h expiry. Embeds `accountId` claim so the frontend resolves the account without an extra API call.
- **Kafka resilience** — `@RetryableTopic` with 3 attempts + exponential backoff. Failed messages route to `.DLT`.

---

## Quick Start

### Prerequisites
- Docker Desktop running
- Java 21 (for running tests locally)

### Run everything

```bash
# 1. Copy environment file
cp .env.example .env

# 2. Start all services (PostgreSQL, MongoDB, Kafka, Backend, Frontend)
docker-compose up --build

# 3. Access
#   Frontend: http://localhost:80
#   API:      http://localhost:8080
#   Swagger:  http://localhost:8080/swagger-ui.html
```

### Run backend tests only

```bash
cd backend
mvn test
```

> Testcontainers requires Docker running locally.

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_USER` | `banking` | PostgreSQL username |
| `DB_PASSWORD` | `banking` | PostgreSQL password |
| `DB_URL` | `jdbc:postgresql://localhost:5432/banking` | PostgreSQL JDBC URL |
| `MONGO_URI` | `mongodb://localhost:27017/banking` | MongoDB connection URI |
| `KAFKA_BROKERS` | `localhost:9092` | Kafka bootstrap servers |
| `JWT_SECRET` | *(see .env.example)* | HS256 signing key (min 32 chars) |
| `JWT_EXPIRATION_MS` | `3600000` | Token TTL in ms (1 hour) |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |

---

## Test Credentials (dev profile)

The `DataSeeder` loads 20 users from `mock-data.json` on startup (dev profile only).

| Email | Password |
|---|---|
| ihernandez@email.com | Isabel2024! |
| mjimenez@example.com | Miguel2024! |
| paulamolina@mail.com | Paula2024! |

---

## API Endpoints

### Authentication
| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user + create linked account |
| POST | `/api/auth/login` | Login → returns JWT |
| POST | `/api/auth/logout` | Logout (client-side token invalidation) |

### Accounts
| Method | Path | Description |
|---|---|---|
| GET | `/api/accounts/{id}` | Get account info |
| GET | `/api/accounts/{id}/balance` | Get current balance |

### Transactions
| Method | Path | Description |
|---|---|---|
| POST | `/api/transactions/deposit` | Deposit funds |
| POST | `/api/transactions/withdraw` | Withdraw funds |
| POST | `/api/transactions/transfer` | Transfer between accounts |
| GET | `/api/transactions/history` | Paginated transaction history |

> All endpoints except `/api/auth/**` require `Authorization: Bearer <token>` header.
> Full interactive docs available at `http://localhost:8080/swagger-ui.html`

---

## Kafka Events

**Topic:** `banking.transactions`  
**Dead-letter topic:** `banking.transactions.DLT`

```json
{
  "transactionId": "uuid",
  "sourceAccountId": "uuid",
  "targetAccountId": "uuid",
  "type": "DEPOSIT | WITHDRAWAL | TRANSFER",
  "amount": 100.00,
  "currency": "USD",
  "status": "COMPLETED",
  "timestamp": "2026-01-01T00:00:00Z"
}
```

**Consumer groups:**
- `audit-log-group` → writes to MongoDB `audit_logs`
- `transaction-history-group` → writes to MongoDB `transaction_history`

Both consumers are idempotent and use `@RetryableTopic` (3 attempts, exponential backoff).

---

## Frontend

Built with **Angular 17** (standalone components) + **Tailwind CSS**.

| Page | Path | Description |
|---|---|---|
| Login | `/login` | JWT authentication |
| Register | `/register` | New user + account creation |
| Dashboard | `/dashboard` | Balance overview + quick actions |
| Transactions | `/transactions` | Deposit / Withdraw / Transfer forms |
| History | `/history` | Paginated transaction history table |

---

## Project Structure

```
small-baking-app/
├── backend/
│   ├── src/main/java/com/smallbankapp/banking/
│   │   ├── application/        → Mediator, Commands, Queries, Handlers
│   │   ├── domain/             → Entities, Value Objects, domain exceptions
│   │   ├── infrastructure/     → JPA, MongoDB, Kafka, JWT, config
│   │   └── presentation/       → REST Controllers, DTOs
│   └── src/test/               → Unit + Integration tests
├── frontend/
│   └── src/app/
│       ├── core/               → Services, interceptors, guards
│       ├── models/             → TypeScript interfaces
│       ├── pages/              → Login, Register, Dashboard, Transactions, History
│       └── shared/             → Navbar, Sidebar components
├── docker-compose.yml
└── .env.example
```

---

## License

MIT
