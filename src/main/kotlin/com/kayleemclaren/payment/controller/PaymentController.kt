package com.kayleemclaren.payment.controller

import com.kayleemclaren.payment.dto.CreatePaymentRequest
import com.kayleemclaren.payment.dto.PaymentResponse
import com.kayleemclaren.payment.model.PaymentStatus
import com.kayleemclaren.payment.service.PaymentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Payment Controller - REST API Layer
 *
 * Key Concepts:
 * - @RestController: Combines @Controller + @ResponseBody (auto-converts to JSON)
 * - @RequestMapping: Base path for all endpoints in this controller
 * - REST endpoints map HTTP methods to business operations
 * - ResponseEntity: Wrapper that allows control of HTTP status codes and headers
 *
 * REST API Design:
 * - POST /api/v1/payments          → Create new payment
 * - GET /api/v1/payments/{id}      → Get specific payment
 * - GET /api/v1/payments           → Get all payments (or filter by status)
 * - PATCH /api/v1/payments/{id}/status → Update payment status
 * - DELETE /api/v1/payments/{id}   → Delete payment
 *
 * Why @RestController instead of @Controller?
 * - @Controller returns view names (HTML templates)
 * - @RestController returns data (JSON/XML) automatically
 * - Every method has implicit @ResponseBody annotation
 *
 * Why /api/v1/?
 * - /api = clearly indicates this is an API endpoint
 * - /v1 = version number (allows future v2, v3 without breaking clients)
 * - Industry best practice for REST APIs
 */
@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    // Constructor injection - Spring provides PaymentService instance
    private val paymentService: PaymentService
) {

    /**
     * Create a new payment
     *
     * HTTP: POST /api/v1/payments
     * Request Body: CreatePaymentRequest JSON
     * Response: 201 Created with PaymentResponse JSON
     *
     * Key Annotations:
     * - @PostMapping: Handles HTTP POST requests
     * - @Valid: Triggers validation on CreatePaymentRequest (checks @NotNull, @NotBlank, etc.)
     * - @RequestBody: Deserializes JSON from request body to Kotlin object
     *
     * ResponseEntity:
     * - ResponseEntity.status(HttpStatus.CREATED): Returns 201 status code
     * - .body(payment): Includes the created payment in response body
     *
     * Example Request:
     * POST /api/v1/payments
     * {
     *   "amount": 100.50,
     *   "currency": "USD",
     *   "senderId": "user123",
     *   "recipientId": "user456",
     *   "description": "Payment for services"
     * }
     *
     * Example Response (201 Created):
     * {
     *   "id": 1,
     *   "amount": 100.50,
     *   "currency": "USD",
     *   "status": "PENDING",
     *   ...
     * }
     */
    @PostMapping
    fun createPayment(
        @Valid @RequestBody request: CreatePaymentRequest
    ): ResponseEntity<PaymentResponse> {
        // Call service layer to create payment
        val payment = paymentService.createPayment(request)

        // Return 201 Created with payment data
        return ResponseEntity.status(HttpStatus.CREATED).body(payment)
    }

    /**
     * Get a specific payment by ID
     *
     * HTTP: GET /api/v1/payments/{id}
     * Response: 200 OK with PaymentResponse JSON, or 404 Not Found
     *
     * Key Annotations:
     * - @GetMapping("/{id}"): Handles GET requests with path variable
     * - @PathVariable: Extracts {id} from URL path
     *
     * Path Variables:
     * - URL: /api/v1/payments/5 → id = 5
     * - Type conversion automatic (String → Long)
     * - Spring validates type (returns 400 if invalid)
     *
     * Example Request:
     * GET /api/v1/payments/1
     *
     * Example Response (200 OK):
     * {
     *   "id": 1,
     *   "amount": 100.50,
     *   "status": "PENDING",
     *   ...
     * }
     */
    @GetMapping("/{id}")
    fun getPayment(@PathVariable id: Long): ResponseEntity<PaymentResponse> {
        // Service throws PaymentNotFoundException if not found
        // Global exception handler will convert to 404 response
        val payment = paymentService.getPaymentById(id)

        // Return 200 OK with payment data
        return ResponseEntity.ok(payment)
    }

    /**
     * Get all payments, optionally filtered by status
     *
     * HTTP: GET /api/v1/payments
     * HTTP: GET /api/v1/payments?status=PENDING
     * Response: 200 OK with List<PaymentResponse> JSON
     *
     * Key Annotations:
     * - @GetMapping: Handles GET requests (no path = base path)
     * - @RequestParam: Extracts query parameter from URL
     * - required = false: Makes query parameter optional
     *
     * Query Parameters:
     * - /api/v1/payments → status = null → returns all
     * - /api/v1/payments?status=PENDING → status = PENDING → filters by status
     *
     * Conditional Logic:
     * - if (status != null): Filter by status
     * - else: Return all payments
     *
     * Example Request 1:
     * GET /api/v1/payments
     *
     * Example Response:
     * [
     *   { "id": 1, "status": "PENDING", ... },
     *   { "id": 2, "status": "COMPLETED", ... }
     * ]
     *
     * Example Request 2:
     * GET /api/v1/payments?status=PENDING
     *
     * Example Response:
     * [
     *   { "id": 1, "status": "PENDING", ... }
     * ]
     */
    @GetMapping
    fun getAllPayments(
        @RequestParam(required = false) status: PaymentStatus?
    ): ResponseEntity<List<PaymentResponse>> {
        // If status provided, filter; otherwise get all
        val payments = if (status != null) {
            paymentService.getPaymentsByStatus(status)
        } else {
            paymentService.getAllPayments()
        }

        // Return 200 OK with list of payments
        return ResponseEntity.ok(payments)
    }

    /**
     * Update payment status
     *
     * HTTP: PATCH /api/v1/payments/{id}/status?status=COMPLETED
     * Response: 200 OK with updated PaymentResponse
     *
     * Key Annotations:
     * - @PatchMapping: Handles HTTP PATCH requests (partial updates)
     * - @PathVariable: Extract payment ID from URL path
     * - @RequestParam: Extract new status from query parameter
     *
     * PATCH vs PUT:
     * - PATCH = partial update (change one field)
     * - PUT = full replacement (send entire object)
     * - We use PATCH because we're only updating status field
     *
     * Example Request:
     * PATCH /api/v1/payments/1/status?status=COMPLETED
     *
     * Example Response (200 OK):
     * {
     *   "id": 1,
     *   "status": "COMPLETED",  ← Updated!
     *   "updatedAt": "2024-01-27T14:30:00",  ← Updated timestamp
     *   ...
     * }
     */
    @PatchMapping("/{id}/status")
    fun updatePaymentStatus(
        @PathVariable id: Long,
        @RequestParam status: PaymentStatus
    ): ResponseEntity<PaymentResponse> {
        // Call service to update status
        val payment = paymentService.updatePaymentStatus(id, status)

        // Return 200 OK with updated payment
        return ResponseEntity.ok(payment)
    }

    /**
     * Delete a payment
     *
     * HTTP: DELETE /api/v1/payments/{id}
     * Response: 204 No Content (success, no body)
     *
     * Key Annotations:
     * - @DeleteMapping: Handles HTTP DELETE requests
     *
     * ResponseEntity<Void>:
     * - Void = no response body
     * - 204 No Content = successful deletion
     * - Common pattern for DELETE operations
     *
     * HTTP Status Codes:
     * - 204 No Content = deleted successfully
     * - 404 Not Found = payment doesn't exist (from exception handler)
     *
     * Example Request:
     * DELETE /api/v1/payments/1
     *
     * Example Response:
     * 204 No Content
     * (empty body)
     */
    @DeleteMapping("/{id}")
    fun deletePayment(@PathVariable id: Long): ResponseEntity<Void> {
        // Call service to delete payment
        paymentService.deletePayment(id)

        // Return 204 No Content (successful deletion, no body)
        return ResponseEntity.noContent().build()
    }
}