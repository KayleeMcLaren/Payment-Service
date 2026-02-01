# Payment Service API - Manual Testing Guide

## Prerequisites
- Application running on `http://localhost:8080`
- Postman installed (or use curl/HTTP client)

## Test Scenarios

### 1. Create Payment
**Request:**
```
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

**Expected:** 201 Created with payment details

### 2. Get All Payments
**Request:** `GET /api/v1/payments`  
**Expected:** 200 OK with array of payments

### 3. Get Payment by ID
**Request:** `GET /api/v1/payments/1`  
**Expected:** 200 OK with payment details

### 4. Filter by Status
**Request:** `GET /api/v1/payments?status=PENDING`  
**Expected:** 200 OK with filtered payments

### 5. Update Payment Status
**Request:** `PATCH /api/v1/payments/1/status?status=COMPLETED`  
**Expected:** 200 OK with updated payment

### 6. Delete Payment
**Request:** `DELETE /api/v1/payments/2`  
**Expected:** 204 No Content

### 7. Validation Error
**Request:**
```
POST /api/v1/payments
{
  "amount": -10,
  "currency": "US",
  "senderId": ""
}
```
**Expected:** 400 Bad Request with validation errors

### 8. Not Found Error
**Request:** `GET /api/v1/payments/999`  
**Expected:** 404 Not Found with error message

## All Tests Passed ✅
- Date tested: 2026-02-01
- Tested by: Kaylee Mc Laren