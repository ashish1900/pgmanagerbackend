package com.ash.main.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "room_assignments")
@Data
public class RoomAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomNumber;
    private String floorNumber;
    private String buildingNumber;
    private String address;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private GuestDetailEntity guest;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserDetailsEntity owner;
}
