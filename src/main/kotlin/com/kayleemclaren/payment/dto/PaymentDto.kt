package com.kayleemclaren.payment.dto

import com.kayleemclaren.payment.model.PaymentStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime


// Request DTO (Data Transfer Objects) - what clients send to create a payment (POST request)
// Has validation annotations (@NotNull, @NotBlank, @DecimalMin)
// @field: targets the field (Kotlin-specific syntax for annotations)
// No id or timestamps - those are auto-generated
data class CreatePaymentRequest(
    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    val amount: BigDecimal,

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters (e.g., USD, EUR)")
    val currency: String,

    @field:NotBlank(message = "Sender ID is required")
    val senderId: String,

    @field:NotBlank(message = "Recipient ID is required")
    val recipientId: String,

    val description: String? = null
)

// Response DTO - what we return to clients
// Includes all fields including generated ones (id, timestamps)
// Clean, read-only representation
data class PaymentResponse(
    val id: Long,
    val amount: BigDecimal,
    val currency: String,
    val senderId: String,
    val recipientId: String,
    val status: PaymentStatus,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// Extension function to convert Entity to Response DTO
// Adds a method to the Payment class without modifying it
// Usage: payment.toResponse() converts entity to DTO
fun com.kayleemclaren.payment.model.Payment.toResponse(): PaymentResponse {
    return PaymentResponse(
        id = this.id!!,  // !! asserts non-null (safe here because saved entities always have IDs)
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