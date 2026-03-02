# Payment Service API

A RESTful payment processing microservice built with Kotlin and Spring Boot, demonstrating modern backend development patterns for enterprise fintech applications.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)

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




