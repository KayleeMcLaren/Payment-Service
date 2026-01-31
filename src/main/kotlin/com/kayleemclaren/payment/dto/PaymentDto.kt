package com.kayleemclaren.payment.dto

import com.kayleemclaren.payment.model.PaymentStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Data Transfer Objects (DTOs) - API Layer
 *
 * Key Concepts:
 * - DTOs separate API contracts from database entities
 * - Request DTOs: What clients send to the API
 * - Response DTOs: What the API returns to clients
 * - Validation: Ensures data integrity before processing
 *
 * Why separate DTOs from entities?
 * - API can change without affecting database schema
 * - Hide internal database details from clients
 * - Add validation specific to API input
 * - Control exactly what data is exposed
 */

/**
 * CreatePaymentRequest - Used for POST /api/v1/payments
 *
 * Validation Annotations:
 * - @field: Kotlin-specific - targets the property field (not getter/constructor param)
 * - @NotNull: Value cannot be null
 * - @NotBlank: String cannot be null, empty, or whitespace
 * - @DecimalMin: Minimum value for numeric fields
 * - @Size: String length constraints
 *
 * Note: No id, createdAt, updatedAt - these are auto-generated server-side
 */
data class CreatePaymentRequest(

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    val amount: BigDecimal,  // Must be positive, non-null

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters (e.g., USD, EUR)")
    val currency: String,  // Exactly 3 characters (ISO 4217 currency codes)

    @field:NotBlank(message = "Sender ID is required")
    val senderId: String,  // Who is sending the payment

    @field:NotBlank(message = "Recipient ID is required")
    val recipientId: String,  // Who is receiving the payment

    val description: String? = null  // Optional - no validation, can be null
)

/**
 * PaymentResponse - Returned by all read operations (GET requests)
 *
 * Includes all fields:
 * - Server-generated fields (id, timestamps)
 * - Client-provided fields (amount, currency, etc.)
 * - Computed fields (status)
 *
 * This is a read-only representation - all fields are val (immutable)
 */
data class PaymentResponse(
    val id: Long,  // Database-generated ID
    val amount: BigDecimal,
    val currency: String,
    val senderId: String,
    val recipientId: String,
    val status: PaymentStatus,  // Current payment status
    val description: String?,  // Nullable - may not have description
    val createdAt: LocalDateTime,  // When payment was created
    val updatedAt: LocalDateTime  // When payment was last modified
)

/**
 * Extension Function - Converts Payment entity to PaymentResponse DTO
 *
 * Key Concepts:
 * - Extension functions add methods to classes without modifying them
 * - Syntax: fun ClassName.methodName()
 * - Usage: payment.toResponse() (called on Payment instances)
 *
 * Why use extension functions?
 * - Keeps conversion logic separate from entity class
 * - Clean, readable syntax
 * - Can be in different file/package
 *
 * The !! operator:
 * - Asserts that a nullable value is NOT null
 * - Throws exception if value is null
 * - Safe here because saved entities always have IDs
 * - Don't overuse !! - prefer safe calls (?.) in most cases
 */
fun com.kayleemclaren.payment.model.Payment.toResponse(): PaymentResponse {
    return PaymentResponse(
        id = this.id!!,  // !! = "I guarantee this is not null"
        amount = this.amount,
        currency = this.currency,
        senderId = this.senderId,
        recipientId = this.recipientId,
        status = this.status,
        description = this.description,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}