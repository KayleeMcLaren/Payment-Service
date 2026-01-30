// This is a JPA entity that maps to the database table.

package com.kayleemclaren.payment.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity // Marks this as a JPA entity (database table)
@Table(name = "payments")   //  Table name in database
data class Payment(     // Auto-generates equals(), hashCode(), toString(), copy()
    @Id // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    val id: Long? = null,   //  Nullable because database generates it

    @Column(nullable = false)
    val amount: BigDecimal, // Use BigDecimal for money (never Double/Float)

    @Column(nullable = false, length = 3)
    val currency: String,

    @Column(nullable = false)
    val senderId: String,

    @Column(nullable = false)
    val recipientId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,  //  Mutable because we'll update it

    @Column(length = 500)
    var description: String? = null,    // Nullable type (description is optional)

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)