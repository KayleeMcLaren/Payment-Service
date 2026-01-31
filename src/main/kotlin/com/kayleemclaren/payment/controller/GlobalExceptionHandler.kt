package com.kayleemclaren.payment.controller

import com.kayleemclaren.payment.service.PaymentNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

/**
 * Global Exception Handler - Centralized Error Handling
 *
 * Key Concepts:
 * - @RestControllerAdvice: Applies to all @RestController classes
 * - @ExceptionHandler: Catches specific exception types
 * - Converts exceptions to proper HTTP error responses
 * - Returns consistent error format across the API
 *
 * Why centralized exception handling?
 * - Don't repeat error handling in every controller method
 * - Consistent error response format
 * - Easier to maintain and update
 * - Clean separation of concerns
 *
 * Exception Handling Flow:
 * 1. Exception thrown in service/controller
 * 2. Spring catches exception
 * 3. Finds matching @ExceptionHandler method
 * 4. Converts exception to HTTP response
 * 5. Returns error response to client
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Handle Payment Not Found Exceptions
     *
     * Triggers when: paymentService.getPaymentById() can't find payment
     * Returns: 404 Not Found with error details
     *
     * @ExceptionHandler(PaymentNotFoundException::class):
     * - Catches PaymentNotFoundException
     * - ::class is Kotlin's way of referencing a class type
     *
     * Example Response (404 Not Found):
     * {
     *   "status": 404,
     *   "message": "Payment with ID 999 not found",
     *   "timestamp": "2024-01-27T10:30:00"
     * }
     */
    @ExceptionHandler(PaymentNotFoundException::class)
    fun handlePaymentNotFound(ex: PaymentNotFoundException): ResponseEntity<ErrorResponse> {
        // Create error response object
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),  // 404
            message = ex.message ?: "Payment not found",  // Use exception message or default
            timestamp = LocalDateTime.now()
        )

        // Return 404 response with error details
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    /**
     * Handle Validation Errors
     *
     * Triggers when: @Valid annotation finds validation errors
     * Returns: 400 Bad Request with validation error details
     *
     * MethodArgumentNotValidException:
     * - Thrown when @Valid fails on @RequestBody
     * - Contains all validation errors
     * - bindingResult.fieldErrors = list of failed validations
     *
     * Example Scenario:
     * POST /api/v1/payments
     * {
     *   "amount": -10,           ← Fails @DecimalMin validation
     *   "currency": "US",        ← Fails @Size validation (not 3 chars)
     *   "senderId": ""           ← Fails @NotBlank validation
     * }
     *
     * Example Response (400 Bad Request):
     * {
     *   "status": 400,
     *   "message": "Validation failed: amount: Amount must be greater than 0, currency: Currency must be 3 characters (e.g., USD, EUR), senderId: Sender ID is required",
     *   "timestamp": "2024-01-27T10:30:00"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        // Extract all validation error messages
        // .map { }: Transform each FieldError to a string
        // .joinToString(", "): Combine all messages with comma separator
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }  // "amount: Amount must be greater than 0"
            .joinToString(", ")  // Combine all errors into one string

        // Create error response
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),  // 400
            message = "Validation failed: $errors",
            timestamp = LocalDateTime.now()
        )

        // Return 400 Bad Request
        return ResponseEntity.badRequest().body(error)
    }

    /**
     * Handle All Other Unexpected Exceptions
     *
     * Catches: Any exception not handled by other handlers
     * Returns: 500 Internal Server Error
     *
     * Safety Net:
     * - Ensures API never returns ugly stack traces to clients
     * - Logs exception for debugging (in production, add logging here)
     * - Returns clean error response
     *
     * Example Response (500 Internal Server Error):
     * {
     *   "status": 500,
     *   "message": "An unexpected error occurred: ...",
     *   "timestamp": "2024-01-27T10:30:00"
     * }
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        // In production: Log the exception for debugging
        // logger.error("Unexpected error", ex)

        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 500
            message = "An unexpected error occurred: ${ex.message}",
            timestamp = LocalDateTime.now()
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}

/**
 * Error Response DTO - Consistent Error Format
 *
 * All error responses follow this structure
 * Makes it easy for API clients to handle errors
 *
 * Fields:
 * - status: HTTP status code (404, 400, 500, etc.)
 * - message: Human-readable error description
 * - timestamp: When the error occurred
 *
 * Data class benefits:
 * - Automatically serialized to JSON by Spring
 * - Auto-generated toString(), equals(), hashCode()
 */
data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: LocalDateTime
)