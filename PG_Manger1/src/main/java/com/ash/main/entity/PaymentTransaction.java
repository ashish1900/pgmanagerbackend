package com.ash.main.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    
 // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private GuestDetailEntity guest;

    
    
    // Owner reference (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserDetailsEntity owner;
    
    
    @Column
    private String paymentType;          
    
    @Column
    private Double amount;               
   
    @Column
    private String receiptImage;         
   
    @Column
    private LocalDateTime paymentDate;   
   
    @Column
    private LocalDateTime verifiedDate;  
   
    @Column
    private String status;               

    
    
}
