# Payment Service API

A RESTful payment processing microservice built with Kotlin and Spring Boot, demonstrating modern backend development patterns for enterprise fintech applications.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-blue.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Context: Why Kotlin?

**Short answer:** Built to demonstrate proficiency with JVM-based microservices architecture commonly used in enterprise fintech.

**Longer answer:** My production experience is Python/AWS serverless. I built this project to understand:
- **Dependency Injection:** How Spring manages object lifecycle vs Python's explicit wiring
- **ORM Patterns:** JPA/Hibernate vs Python's SQLAlchemy/raw SQL
- **Type Safety:** Kotlin's null safety and type system vs Python's dynamic typing  
- **Testing Patterns:** MockK and Spring Test vs Python's Pytest
- **Enterprise Patterns:** Service-repository separation, transaction management, REST best practices

**Target audience:** Companies using Kotlin/Java stacks (e.g., JUMO, enterprise financial services) who want to see I can work outside my primary Python ecosystem.

**Time investment:** Built in 3 weeks (January 2025) as targeted learning for specific job opportunities.

### What I Learned

**Kotlin Strengths:**
- Null safety catches bugs at compile time that would be runtime errors in Python
- Data classes eliminate boilerplate
- Coroutines (not used here, but studied) offer better async patterns than Python threads

**Spring Boot Insights:**
- Auto-configuration is powerful but requires understanding of annotations and lifecycle
- Dependency injection enforces better architecture than Python's "import everything"
- JPA/Hibernate is more "magical" than SQLAlchemy—trades explicitness for convenience

**Trade-offs I noticed:**
- JVM startup time is slower than Python (matters for Lambda, less for traditional apps)
- IDE support (IntelliJ) is exceptional—refactoring is safer than in Python
- Testing with Spring context is verbose compared to Pytest, but mocking is type-safe

**Verdict:** For high-throughput, long-running services with complex business logic, I'd choose Kotlin/Spring. For rapid prototyping, serverless functions, or data processing, I'd stick with Python. Both have their place.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Design Decisions](#design-decisions)
- [Architecture](#architecture)
- [My Role & Learning Context](#my-role--learning-context)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Production Readiness](#production-readiness)
- [Python vs Kotlin Comparison](#python-vs-kotlin-a-developers-perspective)
- [Learning Outcomes](#learning-outcomes)
- [Author](#author)

---

## Overview

This microservice provides a complete payment processing solution with features including:

- **RESTful API** for payment CRUD operations
- **Payment lifecycle management** (PENDING → PROCESSING → COMPLETED/FAILED)
- **Input validation** with comprehensive error handling
- **Database persistence** using JPA/Hibernate with H2
- **Unit testing** with 100% test success rate using MockK
- **Professional architecture** following service-repository pattern

Built as a portfolio project to demonstrate proficiency in Kotlin, Spring Boot, and modern backend development for fintech applications.

---

## Features

### Core Functionality
- ✅ Create payments with validation
- ✅ Retrieve payments by ID
- ✅ List all payments with optional status filtering
- ✅ Update payment status through lifecycle
- ✅ Delete payments
- ✅ Comprehensive error handling with custom exceptions

### Technical Features
- ✅ RESTful API design with proper HTTP status codes
- ✅ Request validation using Jakarta Bean Validation
- ✅ Global exception handling with consistent error responses
- ✅ Data Transfer Objects (DTOs) for API layer separation
- ✅ Extension functions for clean entity-to-DTO conversion
- ✅ In-memory H2 database with JPA/Hibernate
- ✅ Repository pattern with Spring Data JPA
- ✅ Service layer for business logic isolation
- ✅ Comprehensive unit tests with MockK
- ✅ Detailed code documentation and learning comments

---

## Tech Stack

### Core Technologies
- **Language:** Kotlin 1.9.25
- **Framework:** Spring Boot 3.5.10
- **Build Tool:** Gradle 8.14.4 with Kotlin DSL
- **Java Version:** 17

### Spring Boot Starters
- **Spring Web** - REST API development
- **Spring Data JPA** - Database operations
- **Spring Validation** - Input validation
- **Spring DevTools** - Hot reload during development

### Database
- **H2 Database** - In-memory database for development and testing
- **Hibernate** - ORM framework

### Testing
- **JUnit 5** - Testing framework
- **MockK 1.13.8** - Kotlin-friendly mocking library
- **Spring Boot Test** - Integration testing support

### Development Tools
- **IntelliJ IDEA** - IDE
- **Postman** - API testing
- **Git** - Version control

---

## Design Decisions

### Why H2 In-Memory Database?

**For this demo:** H2 allows the project to run with zero external dependencies. Clone, run, test—no PostgreSQL setup required.

**For production:** Would use PostgreSQL or MySQL with proper connection pooling, migrations with Flyway/Liquibase, and database backups.

### Why Service-Repository Pattern?

Separates business logic (PaymentService) from data access (PaymentRepository). This makes testing easier—I can mock the repository to test business logic in isolation.

**Alternative considered:** Direct repository access from controllers. Rejected because it couples HTTP layer to database, making refactoring harder.

### Why REST Instead of GraphQL?

Payment operations map naturally to REST verbs (POST to create, GET to retrieve, PATCH to update). GraphQL's flexibility isn't needed for this simple domain.

**When I'd use GraphQL:** Client-facing APIs with complex query needs (e.g., mobile apps requesting varied data shapes).

### State Management: Payment Lifecycle

Payments follow a strict state machine:
```
PENDING → PROCESSING → COMPLETED
                    ↘ FAILED → CANCELLED
```

The service validates state transitions—you can't mark a COMPLETED payment as FAILED. This prevents data corruption from invalid state changes.

**Why this matters in production:** Financial systems must maintain data integrity. State machines enforce valid transitions at the application level, even if database constraints don't.

---

## Architecture

### Layered Architecture
```
┌─────────────────────────────────────────┐
│          Controller Layer               │
│  (REST API, HTTP, Request/Response)     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│           Service Layer                 │
│     (Business Logic, Validation)        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Repository Layer                │
│    (Database Access, Spring Data JPA)   │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│           Database (H2)                 │
│      (In-Memory, Development)           │
└─────────────────────────────────────────┘
```

### Payment Lifecycle
```
┌─────────┐    ┌────────────┐    ┌───────────┐
│ PENDING │───▶│ PROCESSING │───▶│ COMPLETED │
└─────────┘    └────────────┘    └───────────┘
                      │
                      ▼
                ┌──────────┐
                │  FAILED  │
                └──────────┘
                      │
                      ▼
                ┌───────────┐
                │ CANCELLED │
                └───────────┘
```

### Package Structure
```
com.kayleemclaren.payment
├── controller/          # REST API endpoints
│   ├── PaymentController.kt
│   └── GlobalExceptionHandler.kt
├── service/            # Business logic
│   ├── PaymentService.kt
│   └── PaymentNotFoundException.kt
├── repository/         # Data access
│   └── PaymentRepository.kt
├── model/             # Domain entities
│   ├── Payment.kt
│   └── PaymentStatus.kt
├── dto/               # Data transfer objects
│   └── PaymentDto.kt
└── PaymentServiceApplication.kt
```

---

## My Role & Learning Context

**Solo project:** I built this entire microservice independently to learn JVM-based backend patterns.

**Why this project exists:** 
- My day job experience was Python/Lambda serverless
- Many fintech companies use Kotlin/Java with Spring Boot
- I wanted to demonstrate I can learn new stacks quickly
- Specific opportunity (JUMO) required Kotlin experience

**What I already knew (transferable):**
- REST API design principles (from Python work)
- Testing patterns (similar philosophy, different tools)
- Service-repository pattern (same in any language)
- Database persistence concepts

**What I learned specifically for this:**
- Kotlin syntax and language features
- Spring Boot framework and annotations
- JPA/Hibernate ORM patterns
- Gradle build system
- MockK mocking library

**Result:** Built production-ready microservice with comprehensive tests in 3 weeks, demonstrating rapid technology adoption.

---

## Getting Started

### Prerequisites

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Git** ([Download](https://git-scm.com/downloads))
- **Postman** (optional, for API testing) ([Download](https://www.postman.com/downloads/))

### Installation & Running

1. **Clone the repository**
```bash
git clone https://github.com/KayleeMcLaren/payment-service.git
cd payment-service
```

2. **Build the project**
```bash
./gradlew build
# On Windows: .\gradlew.bat build
```

3. **Run the application**
```bash
./gradlew bootRun
# On Windows: .\gradlew.bat bootRun
```

4. **Verify it's running**
    - Application starts on: `http://localhost:8080`
    - H2 Console: `http://localhost:8080/h2-console`
    - Test endpoint: `http://localhost:8080/api/v1/payments`

### Running Tests
```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Clean and test
./gradlew clean test
```

**Test Results:** View the HTML report at `build/reports/tests/test/index.html`

---

## API Endpoints

### Base URL
```
http://localhost:8080/api/v1/payments
```

### Endpoints Overview

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/v1/payments` | Create payment | CreatePaymentRequest | 201 Created |
| GET | `/api/v1/payments` | Get all payments | None | 200 OK |
| GET | `/api/v1/payments?status=PENDING` | Filter by status | None | 200 OK |
| GET | `/api/v1/payments/{id}` | Get payment by ID | None | 200 OK / 404 |
| PATCH | `/api/v1/payments/{id}/status?status=COMPLETED` | Update status | None | 200 OK / 404 |
| DELETE | `/api/v1/payments/{id}` | Delete payment | None | 204 No Content / 404 |

### Detailed API Examples

#### 1. Create Payment

**Request:**
```http
POST /api/v1/payments
Content-Type: application/json

{
  "amount": 100.50,
  "currency": "USD",
  "senderId": "user123",
  "recipientId": "user456",
  "description": "Payment for services"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "amount": 100.50,
  "currency": "USD",
  "senderId": "user123",
  "recipientId": "user456",
  "status": "PENDING",
  "description": "Payment for services",
  "createdAt": "2024-01-27T10:30:00",
  "updatedAt": "2024-01-27T10:30:00"
}
```

#### 2. Get All Payments

**Request:**
```http
GET /api/v1/payments
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "amount": 100.50,
    "currency": "USD",
    "senderId": "user123",
    "recipientId": "user456",
    "status": "PENDING",
    "description": "Payment for services",
    "createdAt": "2024-01-27T10:30:00",
    "updatedAt": "2024-01-27T10:30:00"
  }
]
```

#### 3. Get Payment by ID

**Request:**
```http
GET /api/v1/payments/1
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Payment with ID 999 not found",
  "timestamp": "2024-01-27T12:00:00"
}
```

#### 4. Update Payment Status

**Request:**
```http
PATCH /api/v1/payments/1/status?status=COMPLETED
```

**Response (200 OK):**
```json
{
  "id": 1,
  "amount": 100.50,
  "currency": "USD",
  "status": "COMPLETED",
  "updatedAt": "2024-01-27T12:30:00"
}
```

### Validation Rules

| Field | Rules | Example Error |
|-------|-------|---------------|
| **amount** | Must be > 0.01 | "Amount must be greater than 0" |
| **currency** | Exactly 3 characters | "Currency must be 3 characters (e.g., USD, EUR)" |
| **senderId** | Required, not blank | "Sender ID is required" |
| **recipientId** | Required, not blank | "Recipient ID is required" |
| **description** | Optional, max 500 chars | N/A |

---

## Testing

### Test Coverage

**Unit Tests:** 12 tests with 100% success rate
```
Test Summary
├── PaymentServiceTest (11 tests)
│   ├── Create payment - happy path ✓
│   ├── Create payment - data validation ✓
│   ├── Get payment by ID - found ✓
│   ├── Get payment by ID - not found ✓
│   ├── Get all payments ✓
│   ├── Get all payments - empty ✓
│   ├── Filter by status ✓
│   ├── Update status - success ✓
│   ├── Update status - not found ✓
│   ├── Delete payment - success ✓
│   └── Delete payment - not found ✓
└── PaymentServiceApplicationTests (1 test)
    └── Context loads ✓
```

### Manual API Testing

See [TESTING.md](TESTING.md) for comprehensive manual testing guide with Postman/curl examples.

---

## ⚠️ Production Readiness: What's Missing

**This is a demonstration project showing core patterns, not production software.**

**What's included:**
- ✅ Core business logic with state management  
- ✅ Input validation and error handling  
- ✅ Comprehensive unit tests (100% success rate)  
- ✅ Clean architecture (service-repository pattern)  
- ✅ REST best practices  

**What would be needed for production:**
- ❌ Actual database (PostgreSQL) with migrations  
- ❌ Security (authentication, authorization, rate limiting)  
- ❌ Observability (structured logging, metrics, tracing)  
- ❌ API documentation (OpenAPI/Swagger)  
- ❌ Containerization (Dockerfile for deployment)  
- ❌ Integration tests (TestContainers with real DB)  
- ❌ CI/CD pipeline (GitHub Actions for automated testing)  

**For hiring managers:** The architecture and patterns here are production-ready. The implementation demonstrates I understand Spring Boot and Kotlin. In a real production environment, I'd add operational maturity (monitoring, security hardening, documentation) as part of normal development process.

---

## Python vs Kotlin: A Developer's Perspective

Having built production systems in Python and this project in Kotlin, here's my honest comparison:

### Python Wins
- **Speed of development:** Faster iteration for prototypes and scripts
- **Serverless fit:** Cold start times make Python better for Lambda
- **Data processing:** Pandas, NumPy make data work simpler
- **Simplicity:** Less boilerplate, more readable for simple tasks

### Kotlin Wins
- **Type safety:** Catch bugs at compile time
- **Performance:** JVM is faster for CPU-intensive work
- **Null safety:** Eliminates entire class of runtime errors
- **Tooling:** IntelliJ IDEA is exceptional

### When I'd choose each:
- **Python:** Serverless functions, data processing, rapid prototypes, scripting
- **Kotlin/Spring:** High-throughput services, complex business logic, long-running processes
- **Both work:** REST APIs, microservices (comes down to team preference and ecosystem)

**For this project:** Kotlin was the right choice to demonstrate JVM competency. For my serverless ecosystem project, Python was the right choice for Lambda optimization.

---

## Development

### H2 Database Console

Access the H2 console for database inspection:

**URL:** `http://localhost:8080/h2-console`

**Connection Settings:**
- JDBC URL: `jdbc:h2:mem:paymentdb`
- Username: `sa`
- Password: *(leave blank)*

**Useful Queries:**
```sql
-- View all payments
SELECT * FROM PAYMENTS;

-- Filter by status
SELECT * FROM PAYMENTS WHERE STATUS = 'PENDING';

-- Count by status
SELECT STATUS, COUNT(*) FROM PAYMENTS GROUP BY STATUS;
```

### Application Configuration

**File:** `src/main/resources/application.properties`
```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:paymentdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console
spring.h2.console.enabled=true
```

---

## Learning Outcomes

**What this project taught me:**

1. **JVM ecosystem is mature:** Tooling, libraries, and community support are excellent
2. **Spring Boot is opinionated:** This is good—forces consistency, but requires learning "the Spring way"
3. **Type safety is worth the verbosity:** Catching errors at compile time saves debugging time
4. **Testing culture is strong:** Spring Test + MockK make testing feel built-in, not bolted-on
5. **Kotlin is pragmatic:** Takes best parts of Java, removes cruft, adds modern features

**How this complements my Python experience:**

I now understand both:
- **Python/Serverless:** Best for event-driven, auto-scaling, pay-per-use systems
- **Kotlin/Spring:** Best for traditional microservices with consistent load

This makes me a more versatile engineer—I can recommend the right tool for the problem, not just use what I know.

**Confidence level:**
- ✅ Can build production features in Kotlin/Spring with team support
- ✅ Can review Kotlin code and participate in architecture discussions
- ⚠️ Would need mentorship for complex JVM performance tuning or advanced Spring features
- ⚠️ Not yet proficient in reactive programming (WebFlux, Kotlin coroutines)

**Next steps for deeper Kotlin/Spring learning:**
- Add caching with Redis
- Implement WebSocket endpoint for real-time updates
- Deploy to Kubernetes with proper health checks
- Add API gateway (Spring Cloud Gateway)

---

## Related Projects

See my [Serverless Fintech Ecosystem](https://github.com/KayleeMcLaren/Serverless-Fintech-Ecosystem) for a complete Python/AWS microservices platform with event-driven architecture, Step Functions workflows, and Terraform IaC.

**Together, these projects show:**
- Python + AWS serverless expertise (Ecosystem project)
- Kotlin + Spring Boot capability (This project)
- Understanding of both paradigms (Serverless vs traditional microservices)

---

## Author

**Kaylee McLaren**

- LinkedIn: [Kaylee McLaren](https://www.linkedin.com/in/software-dev-kaylee-mclaren/)
- Email: mclaren.kaylee@gmail.com
- GitHub: [github.com/KayleeMcLaren](https://github.com/KayleeMcLaren)
- Portfolio: [Live Demo](https://d18l23eogq3lrf.cloudfront.net)

---

## License

This project is created for portfolio demonstration purposes.

---

## Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- Testing with [MockK](https://mockk.io/)
- Inspired by modern fintech architecture patterns
- Created as part of learning journey in backend development

---

## Code Style

This project follows:
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Spring Boot best practices
- Clean Code principles
- SOLID principles

---


*Last Updated: February 2026*
