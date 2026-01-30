// enum defines a set of constant values for the for payment statuses
package com.kayleemclaren.payment.model

// These are the possible states a payment can be in
enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}