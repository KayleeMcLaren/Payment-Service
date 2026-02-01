package com.kayleemclaren.payment.service

import com.kayleemclaren.payment.dto.CreatePaymentRequest
import com.kayleemclaren.payment.model.Payment
import com.kayleemclaren.payment.model.PaymentStatus
import com.kayleemclaren.payment.repository.PaymentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

/**
 * Payment Service Unit Tests
 *
 * Key Concepts:
 * - Unit testing: Test individual components in isolation
 * - Mocking: Create fake dependencies (repository) to isolate service logic
 * - AAA Pattern: Arrange, Act, Assert
 * - MockK: Kotlin-friendly mocking library (better than Mockito for Kotlin)
 *
 * Why unit tests?
 * - Catch bugs early before production
 * - Document expected behavior
 * - Enable refactoring with confidence
 * - Required for professional development
 *
 * Testing Philosophy:
 * - Test business logic, not frameworks (we test service, not repository)
 * - Test happy paths AND error scenarios
 * - One test = one scenario
 * - Tests should be independent (no shared state)
 *
 * MockK Basics:
 * - mockk<T>(): Creates a mock object
 * - every { mock.method() }: Define mock behavior
 * - returns: What the mock should return
 * - verify { mock.method() }: Verify method was called
 */
class PaymentServiceTest {

    /**
     * Test Setup - Arrange Dependencies
     *
     * Key Concepts:
     * - mockk<PaymentRepository>(): Creates fake repository
     * - PaymentService(paymentRepository): Inject mock into service
     * - No @Autowired, no Spring context (pure unit test)
     *
     * Why mock the repository?
     * - We're testing SERVICE logic, not database operations
     * - Tests run faster (no database)
     * - Tests are isolated (no external dependencies)
     * - Can simulate any scenario (errors, edge cases)
     */
    private val paymentRepository: PaymentRepository = mockk()
    private val paymentService = PaymentService(paymentRepository)

    /**
     * Test: Create Payment - Happy Path
     *
     * Scenario: Client creates valid payment
     * Expected: Payment saved with PENDING status, ID generated
     *
     * AAA Pattern:
     * - Arrange: Set up test data and mock behavior
     * - Act: Call the method being tested
     * - Assert: Verify results are correct
     *
     * MockK Concepts:
     * - every { repository.save(any()) }: "When save is called with any argument"
     * - returns savedPayment: "Return this fake payment"
     * - any(): Matches any argument (like Python's ANY)
     *
     * Verification:
     * - verify(exactly = 1) { }: Verify method called exactly once
     * - Ensures service actually calls repository
     */
    @Test
    fun `createPayment should save and return payment with PENDING status`() {
        // Arrange - Set up test data
        val request = CreatePaymentRequest(
            amount = BigDecimal("100.00"),
            currency = "USD",
            senderId = "sender123",
            recipientId = "recipient456",
            description = "Test payment"
        )

        // Create the payment that repository will "return"
        val savedPayment = Payment(
            id = 1L,
            amount = request.amount,
            currency = request.currency,
            senderId = request.senderId,
            recipientId = request.recipientId,
            description = request.description,
            status = PaymentStatus.PENDING
        )

        // Mock repository behavior
        // "When repository.save() is called with any Payment, return savedPayment"
        every { paymentRepository.save(any()) } returns savedPayment

        // Act - Call the method we're testing
        val result = paymentService.createPayment(request)

        // Assert - Verify results
        assertNotNull(result)  // Result exists
        assertEquals(1L, result.id)  // ID matches
        assertEquals(BigDecimal("100.00"), result.amount)  // Amount correct
        assertEquals("USD", result.currency)  // Currency correct
        assertEquals("sender123", result.senderId)  // Sender correct
        assertEquals("recipient456", result.recipientId)  // Recipient correct
        assertEquals(PaymentStatus.PENDING, result.status)  // Status is PENDING
        assertEquals("Test payment", result.description)  // Description correct

        // Verify repository.save() was called exactly once
        verify(exactly = 1) { paymentRepository.save(any()) }
    }

    /**
     * Test: Create Payment - Verify Correct Data Passed to Repository
     *
     * Scenario: Ensure service passes correct data to repository
     * Expected: Repository receives Payment with request data
     *
     * Slot Concept:
     * - slot<Payment>(): Captures the argument passed to mock
     * - Allows inspecting what was passed to mocked method
     * - Like "spy" in other testing frameworks
     *
     * Why this test?
     * - Previous test verifies returned data
     * - This test verifies what service SENDS to repository
     * - Ensures no data transformation bugs
     */
    @Test
    fun `createPayment should pass correct data to repository`() {
        // Arrange
        val request = CreatePaymentRequest(
            amount = BigDecimal("250.50"),
            currency = "EUR",
            senderId = "alice",
            recipientId = "bob",
            description = null  // Test null description
        )

        // Slot to capture the argument passed to save()
        val paymentSlot = slot<Payment>()

        // Mock: Capture argument and return it with ID
        every { paymentRepository.save(capture(paymentSlot)) } answers {
            paymentSlot.captured.copy(id = 1L)  // Return captured payment with ID
        }

        // Act
        paymentService.createPayment(request)

        // Assert - Verify captured payment has correct data
        val capturedPayment = paymentSlot.captured
        assertEquals(BigDecimal("250.50"), capturedPayment.amount)
        assertEquals("EUR", capturedPayment.currency)
        assertEquals("alice", capturedPayment.senderId)
        assertEquals("bob", capturedPayment.recipientId)
        assertNull(capturedPayment.description)  // Null description handled
        assertEquals(PaymentStatus.PENDING, capturedPayment.status)  // Default status
    }

    /**
     * Test: Get Payment By ID - Happy Path
     *
     * Scenario: Payment exists in database
     * Expected: Returns payment details
     *
     * Optional Concept:
     * - Optional.of(payment): Java's way of wrapping nullable values
     * - Repository.findById() returns Optional<Payment>
     * - If present: contains the payment
     * - If absent: empty Optional (like null)
     */
    @Test
    fun `getPaymentById should return payment when exists`() {
        // Arrange
        val payment = Payment(
            id = 1L,
            amount = BigDecimal("50.00"),
            currency = "USD",
            senderId = "sender123",
            recipientId = "recipient456",
            status = PaymentStatus.PENDING
        )

        // Mock: findById returns Optional containing payment
        every { paymentRepository.findById(1L) } returns Optional.of(payment)

        // Act
        val result = paymentService.getPaymentById(1L)

        // Assert
        assertEquals(1L, result.id)
        assertEquals(BigDecimal("50.00"), result.amount)
        assertEquals("USD", result.currency)
        assertEquals(PaymentStatus.PENDING, result.status)

        // Verify findById was called with correct ID
        verify(exactly = 1) { paymentRepository.findById(1L) }
    }

    /**
     * Test: Get Payment By ID - Not Found
     *
     * Scenario: Payment doesn't exist in database
     * Expected: Throws PaymentNotFoundException
     *
     * Exception Testing:
     * - assertThrows<ExceptionType> { code }: Verify exception is thrown
     * - Returns the exception so you can verify message
     *
     * Why test exceptions?
     * - Ensures proper error handling
     * - Verifies correct exception type
     * - Confirms error messages are helpful
     */
    @Test
    fun `getPaymentById should throw exception when payment not found`() {
        // Arrange
        // Mock: findById returns empty Optional (payment doesn't exist)
        every { paymentRepository.findById(999L) } returns Optional.empty()

        // Act & Assert
        // assertThrows: Verify that calling getPaymentById throws exception
        val exception = assertThrows<PaymentNotFoundException> {
            paymentService.getPaymentById(999L)
        }

        // Verify exception message
        assertTrue(exception.message!!.contains("999"))
        assertTrue(exception.message!!.contains("not found"))

        // Verify repository was called
        verify(exactly = 1) { paymentRepository.findById(999L) }
    }

    /**
     * Test: Get All Payments
     *
     * Scenario: Multiple payments in database
     * Expected: Returns all payments as list
     *
     * Testing Collections:
     * - Verify list size
     * - Verify list contents
     * - Check order (if relevant)
     */
    @Test
    fun `getAllPayments should return list of all payments`() {
        // Arrange
        val payment1 = Payment(
            id = 1L,
            amount = BigDecimal("100.00"),
            currency = "USD",
            senderId = "user1",
            recipientId = "user2"
        )

        val payment2 = Payment(
            id = 2L,
            amount = BigDecimal("200.00"),
            currency = "EUR",
            senderId = "user3",
            recipientId = "user4"
        )

        // Mock: findAll returns list of payments
        every { paymentRepository.findAll() } returns listOf(payment1, payment2)

        // Act
        val result = paymentService.getAllPayments()

        // Assert
        assertEquals(2, result.size)  // List has 2 payments
        assertEquals(1L, result[0].id)  // First payment
        assertEquals(2L, result[1].id)  // Second payment
        assertEquals(BigDecimal("100.00"), result[0].amount)
        assertEquals(BigDecimal("200.00"), result[1].amount)

        verify(exactly = 1) { paymentRepository.findAll() }
    }

    /**
     * Test: Get All Payments - Empty Database
     *
     * Scenario: No payments in database
     * Expected: Returns empty list (not null, not error)
     *
     * Edge Cases:
     * - Always test empty scenarios
     * - Verify app handles "no data" gracefully
     */
    @Test
    fun `getAllPayments should return empty list when no payments exist`() {
        // Arrange
        every { paymentRepository.findAll() } returns emptyList()

        // Act
        val result = paymentService.getAllPayments()

        // Assert
        assertNotNull(result)  // Not null
        assertTrue(result.isEmpty())  // Is empty list
        assertEquals(0, result.size)  // Size is 0
    }

    /**
     * Test: Get Payments By Status
     *
     * Scenario: Filter payments by PENDING status
     * Expected: Returns only PENDING payments
     *
     * Testing Filtering:
     * - Verify only correct items returned
     * - Verify count is correct
     */
    @Test
    fun `getPaymentsByStatus should return filtered payments`() {
        // Arrange
        val pendingPayment = Payment(
            id = 1L,
            amount = BigDecimal("100.00"),
            currency = "USD",
            senderId = "user1",
            recipientId = "user2",
            status = PaymentStatus.PENDING
        )

        // Mock: findByStatus returns only PENDING payments
        every { paymentRepository.findByStatus(PaymentStatus.PENDING) } returns listOf(pendingPayment)

        // Act
        val result = paymentService.getPaymentsByStatus(PaymentStatus.PENDING)

        // Assert
        assertEquals(1, result.size)
        assertEquals(PaymentStatus.PENDING, result[0].status)
        assertEquals(1L, result[0].id)

        verify(exactly = 1) { paymentRepository.findByStatus(PaymentStatus.PENDING) }
    }

    /**
     * Test: Update Payment Status - Happy Path
     *
     * Scenario: Update PENDING payment to COMPLETED
     * Expected: Status updated, timestamp updated, changes saved
     *
     * Testing Updates:
     * - Verify field changes
     * - Verify save called
     * - Verify return value correct
     */
    @Test
    fun `updatePaymentStatus should update status and save payment`() {
        // Arrange
        val payment = Payment(
            id = 1L,
            amount = BigDecimal("100.00"),
            currency = "USD",
            senderId = "user1",
            recipientId = "user2",
            status = PaymentStatus.PENDING
        )

        // Mock: findById returns payment
        every { paymentRepository.findById(1L) } returns Optional.of(payment)

        // Mock: save returns the payment (with updated status)
        every { paymentRepository.save(any()) } answers {
            firstArg<Payment>().copy()  // Return the payment that was passed
        }

        // Act
        val result = paymentService.updatePaymentStatus(1L, PaymentStatus.COMPLETED)

        // Assert
        assertEquals(PaymentStatus.COMPLETED, result.status)  // Status updated

        // Verify both methods called
        verify(exactly = 1) { paymentRepository.findById(1L) }
        verify(exactly = 1) { paymentRepository.save(any()) }
    }

    /**
     * Test: Update Payment Status - Payment Not Found
     *
     * Scenario: Try to update non-existent payment
     * Expected: Throws PaymentNotFoundException
     *
     * Error Path Testing:
     * - Test what happens when update fails
     * - Verify proper exception thrown
     * - Verify save is NOT called (no point saving if payment doesn't exist)
     */
    @Test
    fun `updatePaymentStatus should throw exception when payment not found`() {
        // Arrange
        every { paymentRepository.findById(999L) } returns Optional.empty()

        // Act & Assert
        assertThrows<PaymentNotFoundException> {
            paymentService.updatePaymentStatus(999L, PaymentStatus.COMPLETED)
        }

        // Verify save was NOT called (payment doesn't exist)
        verify(exactly = 0) { paymentRepository.save(any()) }
    }

    /**
     * Test: Delete Payment - Happy Path
     *
     * Scenario: Delete existing payment
     * Expected: Repository deleteById called
     *
     * Testing Void Methods:
     * - No return value to verify
     * - Verify correct methods called
     * - Verify correct arguments passed
     */
    @Test
    fun `deletePayment should call repository deleteById when payment exists`() {
        // Arrange
        // Mock: existsById returns true (payment exists)
        every { paymentRepository.existsById(1L) } returns true

        // Mock: deleteById returns Unit (void)
        every { paymentRepository.deleteById(1L) } returns Unit

        // Act
        paymentService.deletePayment(1L)

        // Assert - Verify methods called
        verify(exactly = 1) { paymentRepository.existsById(1L) }
        verify(exactly = 1) { paymentRepository.deleteById(1L) }
    }

    /**
     * Test: Delete Payment - Payment Not Found
     *
     * Scenario: Try to delete non-existent payment
     * Expected: Throws PaymentNotFoundException, deleteById NOT called
     *
     * Defensive Testing:
     * - Verify app doesn't blindly delete
     * - Checks existence first
     * - Proper error if doesn't exist
     */
    @Test
    fun `deletePayment should throw exception when payment not found`() {
        // Arrange
        every { paymentRepository.existsById(999L) } returns false

        // Act & Assert
        assertThrows<PaymentNotFoundException> {
            paymentService.deletePayment(999L)
        }

        // Verify deleteById was NOT called
        verify(exactly = 0) { paymentRepository.deleteById(any()) }
    }
}