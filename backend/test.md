Subject: Prueba Técnica — Sistema de Banca en Línea | Juan Miguel Torres

Estimado equipo,

Me complace presentar la entrega de la prueba técnica para el puesto de desarrollador. A continuación encontrarán todos los detalles del sistema desarrollado.

---

## 🌐 URLs de Producción (AWS)

| Servicio | URL |
|---|---|
| Frontend | http://smallbank-frontend-alb-fc0375e23609ee6c.us-east-2.elb.amazonaws.com |
| Backend API | http://smallbank-backend-alb-463522994.us-east-2.elb.amazonaws.com |
| Swagger UI | http://smallbank-backend-alb-463522994.us-east-2.elb.amazonaws.com/swagger-ui.html |
| Repositorio | https://github.com/johnmikepty/small-baking-app |

---

## 🔑 Credenciales de Prueba

| Email | Password |
|---|---|
| ihernandez@email.com | Isabel2024! |
| mjimenez@example.com | Miguel2024! |
| paulamolina@mail.com | Paula2024! |
| mperez@email.com | Miguel2024! |
| flopez@example.com | Francisco2024! |

---

## 🏗️ Stack Tecnológico

**Backend:** Java 21 + Spring Boot 3 — Clean Architecture + Patrón Mediador (custom)
**Frontend:** Angular 17 (standalone components) + Tailwind CSS
**SQL:** PostgreSQL 16 (AWS RDS, Flyway migrations)
**NoSQL:** MongoDB Atlas M0 (historial de transacciones + auditoría)
**Mensajería:** Apache Kafka (local/dev) + Amazon SQS (producción AWS)
**Infraestructura:** Docker + Docker Compose + AWS ECS Fargate + ALB
**CI/CD:** GitHub Actions (build, test y deploy automático a AWS en cada push a master)
**Auth:** Spring Security + JWT (HS256, claims con accountId)

---

## ✅ Funcionalidades Implementadas

**Obligatorias:**
- Registro, login y logout con JWT
- Creación automática de cuenta bancaria al registrar usuario
- Depósito, retiro y transferencia entre cuentas con validación de saldo
- Historial de transacciones paginado
- Eventos Kafka/SQS idempotentes con Dead Letter Topic
- Auditoría completa en MongoDB
- Tests unitarios (JUnit 5 + Mockito) e integración (Testcontainers)
- docker-compose up levanta todo el stack completo

**Bonus implementados:**
- CI/CD con GitHub Actions
- Rate limiting (Bucket4j) — 10 req/min en auth, 60 req/min en API
- Logs estructurados JSON (Logback, profile-aware)
- Swagger/OpenAPI interactivo con autenticación
- Paginación en historial de transacciones
- Deploy en AWS (ECS Fargate + RDS + MongoDB Atlas + SQS)

---

## 🏛️ Arquitectura

El proyecto implementa Clean Architecture con 4 capas bien definidas:
- **Domain:** Entidades, Value Objects, reglas de negocio puras
- **Application:** Casos de uso, Mediador custom (Commands/Queries + Handlers)
- **Infrastructure:** JPA, MongoDB, Kafka/SQS, JWT, configuración AWS
- **Presentation:** Controllers REST, DTOs, manejo de errores RFC-7807

Decisiones técnicas documentadas en el README del repositorio.

---

## 🚀 Ejecutar Localmente

```bash
git clone https://github.com/johnmikepty/small-baking-app.git
cd small-baking-app
cp .env.example .env
docker-compose up --build
```

Frontend: http://localhost:80
Swagger: http://localhost:8080/swagger-ui.html

---

Quedo atento a cualquier consulta.

Saludos,
Juan Miguel Torres