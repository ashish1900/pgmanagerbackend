package com.ash.main.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "leave_requests")
public class LeaveRequestEntity {

	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private GuestDetailEntity guest;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserDetailsEntity owner;
    

    @Enumerated(EnumType.STRING)
    private RequestStatus status; 

    @Column
    private LocalDateTime lRequestDate;
    
    @Column
    private LocalDateTime lAcceptedDate;
	
	
}
