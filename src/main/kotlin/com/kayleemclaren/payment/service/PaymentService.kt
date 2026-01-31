package com.kayleemclaren.payment.service

import com.kayleemclaren.payment.dto.CreatePaymentRequest
import com.kayleemclaren.payment.dto.PaymentResponse
import com.kayleemclaren.payment.dto.toResponse
import com.kayleemclaren.payment.model.Payment
import com.kayleemclaren.payment.model.PaymentStatus
import com.kayleemclaren.payment.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Payment Service - Business Logic Layer
 *
 * Key Concepts:
 * - @Service: Spring stereotype annotation - marks this as a service component
 * - @Transactional: All public methods run in database transactions
 * - Constructor injection: Dependencies passed via constructor (best practice)
 * - Service layer contains business rules and coordinates between controller and repository
 *
 * Why a service layer?
 * - Separates business logic from HTTP/API concerns (controller)
 * - Can be reused by multiple controllers or other services
 * - Easier to test (no HTTP mocking needed)
 * - Single Responsibility Principle
 *
 * Transaction management:
 * - @Transactional ensures database operations are atomic
 * - If method throws exception, all database changes are rolled back
 * - Commits only when method completes successfully
 */
@Service
@Transactional
class PaymentService(
    // Constructor injection - Spring automatically provides PaymentRepository instance
    // private val = immutable dependency (cannot be reassigned)
    private val paymentRepository: PaymentRepository
) {

    /**
     * Create a new payment
     *
     * Process:
     * 1. Convert DTO to entity
     * 2. Save to database (repository generates ID)
     * 3. Convert saved entity back to DTO
     * 4. Return to controller
     *
     * @param request The payment creation request from API
     * @return PaymentResponse DTO with generated ID and timestamps
     */
    fun createPayment(request: CreatePaymentRequest): PaymentResponse {
        // Create Payment entity from request DTO
        // Status defaults to PENDING
        val payment = Payment(
            amount = request.amount,
            currency = request.currency,
            senderId = request.senderId,
            recipientId = request.recipientId,
            description = request.description,
            status = PaymentStatus.PENDING
        )

        // Save to database - repository.save() returns the saved entity with generated ID
        val savedPayment = paymentRepository.save(payment)

        // Convert entity to response DTO using extension function
        return savedPayment.toResponse()
    }

    /**
     * Get payment by ID
     *
     * Key Concepts:
     * - Optional<T>: Java's way of handling nullable values
     * - .orElseThrow(): Returns value if present, throws exception if empty
     * - Lambda expression: { } defines what exception to throw
     *
     * @param id Payment ID to retrieve
     * @return PaymentResponse if found
     * @throws PaymentNotFoundException if payment doesn't exist
     */
    fun getPaymentById(id: Long): PaymentResponse {
        // findById returns Optional<Payment>
        // If payment exists: Optional contains the payment
        // If not found: Optional is empty
        val payment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException("Payment with ID $id not found") }

        // Convert to DTO and return
        return payment.toResponse()
    }

    /**
     * Get all payments
     *
     * Key Concepts:
     * - .map { }: Transforms each element in a collection (like Python's map())
     * - it.toResponse(): 'it' is implicit parameter in single-parameter lambdas
     * - Returns List<PaymentResponse> (immutable list)
     *
     * @return List of all payments in the system
     */
    fun getAllPayments(): List<PaymentResponse> {
        // findAll() returns List<Payment> from database
        // .map { } converts each Payment to PaymentResponse
        // 'it' represents each Payment in the list
        return paymentRepository.findAll()
            .map { it.toResponse() }
    }

    /**
     * Get payments filtered by status
     *
     * Use cases:
     * - Get all pending payments for processing
     * - Get completed payments for reconciliation
     * - Get failed payments for retry logic
     *
     * @param status The payment status to filter by
     * @return List of payments with the specified status
     */
    fun getPaymentsByStatus(status: PaymentStatus): List<PaymentResponse> {
        // Custom repository method - Spring Data JPA generates the SQL
        return paymentRepository.findByStatus(status)
            .map { it.toResponse() }
    }

    /**
     * Update payment status
     *
     * Business Logic:
     * - Find the payment (throw exception if not found)
     * - Update status (mutable var field)
     * - Update timestamp to track modification time
     * - Save changes to database
     *
     * Real-world usage:
     * - PENDING → PROCESSING (payment is being processed)
     * - PROCESSING → COMPLETED (payment succeeded)
     * - PROCESSING → FAILED (payment failed)
     *
     * @param id Payment ID to update
     * @param newStatus New status to set
     * @return Updated payment
     * @throws PaymentNotFoundException if payment doesn't exist
     */
    fun updatePaymentStatus(id: Long, newStatus: PaymentStatus): PaymentResponse {
        // Retrieve payment or throw exception
        val payment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException("Payment with ID $id not found") }

        // Update mutable fields (var not val)
        payment.status = newStatus
        payment.updatedAt = LocalDateTime.now()

        // Save changes - JPA tracks changes and updates database
        val updatedPayment = paymentRepository.save(payment)

        return updatedPayment.toResponse()
    }

    /**
     * Delete a payment
     *
     * Note: In production, you'd rarely delete payments
     * Better approach: Add 'deleted' flag or 'status = CANCELLED'
     * This is here for CRUD completeness
     *
     * @param id Payment ID to delete
     * @throws PaymentNotFoundException if payment doesn't exist
     */
    fun deletePayment(id: Long) {
        // Check if payment exists before deleting
        if (!paymentRepository.existsById(id)) {
            throw PaymentNotFoundException("Payment with ID $id not found")
        }

        // Delete from database
        paymentRepository.deleteById(id)
        // No return value (Unit type, like Python's None)
    }
}