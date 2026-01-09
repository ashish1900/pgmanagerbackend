package com.ash.main.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payment_upi")
public class PaymentUpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner reference (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserDetailsEntity owner;

    @Column(nullable = false)
    private String paymentType; 

    @Column(nullable = false)
    private String upiId;

	/*
	 * @Column private String qrCode; 
	 */
    
    @Column
    private String qrPublicId; 
}
