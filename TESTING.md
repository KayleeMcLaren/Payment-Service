# Payment Service API - Testing Guide

Complete guide for manual and automated testing of the Payment Service API.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Running Unit Tests](#running-unit-tests)
- [Manual API Testing](#manual-api-testing)
- [Test Scenarios](#test-scenarios)
- [Database Verification](#database-verification)

---

## Prerequisites

- Application running on `http://localhost:8080`
- Postman, curl, or similar HTTP client
- (Optional) H2 Console access for database verification

---

## Running Unit Tests

### Execute All Tests
```bash
# Run tests
./gradlew clean test

# On Windows
.\gradlew.bat clean test
```

### View Test Results

**Console Output:**
Tests are displayed in console with PASSED/FAILED status.

**HTML Report:**
Open `build/reports/tests/test/index.html` in browser for detailed results.

### Expected Results
```
Test Summary
- Tests: 12
- Failures: 0
- Success Rate: 100%
- Duration: ~1.2s
```

---

## Manual API Testing

### Using Postman

Import or manually create these requests in Postman.

### Using curl

All examples below use curl for command-line testing.

### Using IntelliJ HTTP Client

See `api-tests.http` file in project root for executable HTTP requests.

---

## Test Scenarios

### 1. Create Payment (Happy Path)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.50,
    "currency": "USD",
    "senderId": "user123",
    "recipientId": "user456",
    "description": "Payment for services"
  }'
```

**Expected Response:** `201 Created`
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

**Verification:**
✅ Status code is 201  
✅ ID is generated (1)  
✅ Status is PENDING  
✅ Timestamps are set

---

### 2. Create Payment (Validation Error)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "amount": -10,
    "currency": "US",
    "senderId": "",
    "recipientId": "user456"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "status": 400,
  "message": "Validation failed: amount: Amount must be greater than 0, currency: Currency must be 3 characters (e.g., USD, EUR), senderId: Sender ID is required",
  "timestamp": "2024-01-27T10:30:00"
}
```

**Verification:**
✅ Status code is 400  
✅ Error message lists all validation failures  
✅ Timestamp is present

---

### 3. Get All Payments

**Request:**
```bash
curl http://localhost:8080/api/v1/payments
```

**Expected Response:** `200 OK`
```json
[
  {
    "id": 1,
    "amount": 100.50,
    "status": "PENDING",
    ...
  }
]
```

**Verification:**
✅ Status code is 200  
✅ Returns array of payments  
✅ Includes all created payments

---

### 4. Get Payment by ID (Found)

**Request:**
```bash
curl http://localhost:8080/api/v1/payments/1
```

**Expected Response:** `200 OK`
```json
{
  "id": 1,
  "amount": 100.50,
  ...
}
```

**Verification:**
✅ Status code is 200  
✅ Returns correct payment  
✅ All fields present

---

### 5. Get Payment by ID (Not Found)

**Request:**
```bash
curl http://localhost:8080/api/v1/payments/999
```

**Expected Response:** `404 Not Found`
```json
{
  "status": 404,
  "message": "Payment with ID 999 not found",
  "timestamp": "2024-01-27T10:30:00"
}
```

**Verification:**
✅ Status code is 404  
✅ Error message is descriptive  
✅ Consistent error format

---

### 6. Filter Payments by Status

**Request:**
```bash
curl "http://localhost:8080/api/v1/payments?status=PENDING"
```

**Expected Response:** `200 OK`
```json
[
  {
    "id": 1,
    "status": "PENDING",
    ...
  }
]
```

**Verification:**
✅ Status code is 200  
✅ Only returns PENDING payments  
✅ Other statuses excluded

---

### 7. Update Payment Status

**Request:**
```bash
curl -X PATCH "http://localhost:8080/api/v1/payments/1/status?status=COMPLETED"
```

**Expected Response:** `200 OK`
```json
{
  "id": 1,
  "status": "COMPLETED",
  "updatedAt": "2024-01-27T11:00:00",
  ...
}
```

**Verification:**
✅ Status code is 200  
✅ Status changed to COMPLETED  
✅ updatedAt timestamp changed  
✅ createdAt unchanged

---

### 8. Delete Payment

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/v1/payments/1
```

**Expected Response:** `204 No Content`

**Verification:**
✅ Status code is 204  
✅ No response body  
✅ Payment deleted from database  
✅ Subsequent GET returns 404

---

## Database Verification

### Access H2 Console

1. **URL:** `http://localhost:8080/h2-console`
2. **Login:**
    - JDBC URL: `jdbc:h2:mem:paymentdb`
    - Username: `sa`
    - Password: *(leave blank)*

### Verify Data

**Check all payments:**
```sql
SELECT * FROM PAYMENTS;
```

**Check by status:**
```sql
SELECT * FROM PAYMENTS WHERE STATUS = 'PENDING';
```

**Count payments:**
```sql
SELECT COUNT(*) FROM PAYMENTS;
```

**Verify payment details:**
```sql
SELECT * FROM PAYMENTS WHERE ID = 1;
```

---

## Complete Test Checklist

### Create Operations
- [ ] Create payment with valid data → 201
- [ ] Create with negative amount → 400
- [ ] Create with invalid currency (2 chars) → 400
- [ ] Create with blank senderId → 400
- [ ] Create with null description → 201 (allowed)

### Read Operations
- [ ] Get all payments → 200
- [ ] Get all when empty → 200 with []
- [ ] Get payment by ID (exists) → 200
- [ ] Get payment by ID (not exists) → 404
- [ ] Filter by status PENDING → 200
- [ ] Filter by status COMPLETED → 200

### Update Operations
- [ ] Update existing payment status → 200
- [ ] Update non-existent payment → 404
- [ ] Verify updatedAt changes
- [ ] Verify createdAt unchanged

### Delete Operations
- [ ] Delete existing payment → 204
- [ ] Delete non-existent payment → 404
- [ ] Verify deletion in database

### Error Handling
- [ ] All validation errors return 400
- [ ] All not found errors return 404
- [ ] Error responses have consistent format
- [ ] Error messages are descriptive

---

## Test Data Examples

### Valid Payment
```json
{
  "amount": 100.50,
  "currency": "USD",
  "senderId": "user123",
  "recipientId": "user456",
  "description": "Test payment"
}
```

### Different Currencies
```json
{ "amount": 50.00, "currency": "EUR", ... }
{ "amount": 75.25, "currency": "GBP", ... }
{ "amount": 1000.00, "currency": "ZAR", ... }
```

### Edge Cases
```json
{ "amount": 0.01, "currency": "USD", ... }  // Minimum amount
{ "amount": 999999.99, "currency": "USD", ... }  // Large amount
{ "description": null, ... }  // Null description (valid)
```

---

## Test Results Log

**Date Tested:** February 1, 2026  
**Tested By:** Kaylee McLaren  
**Environment:** Local Development

| Test | Status | Notes |
|------|--------|-------|
| Unit Tests | ✅ PASS | 12/12 tests passing |
| Create Payment | ✅ PASS | All validations working |
| Get Operations | ✅ PASS | Filtering works correctly |
| Update Status | ✅ PASS | Timestamps update properly |
| Delete Payment | ✅ PASS | Returns 204, removes from DB |
| Error Handling | ✅ PASS | Consistent error responses |

**Overall Result:** ✅ ALL TESTS PASSING

---

*For automated testing, see unit tests in `src/test/kotlin/`*ee Mc Laren