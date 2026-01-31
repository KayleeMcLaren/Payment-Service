package com.kayleemclaren.payment.service

/**
 * Custom Exception - Payment Not Found
 *
 * Key Concepts:
 * - Custom exceptions help distinguish different error types
 * - Extends RuntimeException (unchecked exception in Java/Kotlin)
 * - Will be caught by global exception handler later
 *
 * Why custom exceptions?
 * - More descriptive than generic exceptions
 * - Can be handled differently (return 404 vs 500)
 * - Better error messages for API consumers
 *
 * RuntimeException vs Exception:
 * - RuntimeException = unchecked (don't need to declare in method signature)
 * - Exception = checked (must declare or handle)
 * - Spring/Kotlin prefer unchecked exceptions
 */
class PaymentNotFoundException(message: String) : RuntimeException(message)