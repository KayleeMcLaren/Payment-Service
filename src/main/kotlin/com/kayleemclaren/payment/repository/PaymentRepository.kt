package com.kayleemclaren.payment.repository

import com.kayleemclaren.payment.model.Payment
import com.kayleemclaren.payment.model.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Payment Repository - Data Access Layer
 * 
 * Key Concepts:
 * - @Repository: Spring stereotype annotation for data access components
 * - JpaRepository<Payment, Long>: Provides built-in methods (save, findById, delete, etc.)
 * - Generic types: <Entity, ID Type>
 * - Method name conventions: Spring generates SQL automatically
 *   Example: findByStatus -> SELECT * FROM payments WHERE status = ?
 */
@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {

    // Query derivation methods - Spring Data JPA implements these automatically
    fun findByStatus(status: PaymentStatus): List<Payment>        // WHERE status = ?
    fun findBySenderId(senderId: String): List<Payment>           // WHERE sender_id = ?
    fun findByRecipientId(recipientId: String): List<Payment>     // WHERE recipient_id = ?
}