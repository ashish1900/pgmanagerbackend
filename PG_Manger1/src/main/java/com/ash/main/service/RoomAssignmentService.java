package com.ash.main.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ash.main.entity.RoomAssignment;
import com.ash.main.rpository.RoomAssignmentRepo;

@Service
public class RoomAssignmentService {

	@Autowired
    private RoomAssignmentRepo roomAssignmentRepo;   

	/*
	 * public Optional<RoomAssignment> getRoomByGuestMobile(String guestMobile) {
	 * return roomAssignmentRepo.findByGuest_MoNumber(guestMobile); }
	 */
	public List<RoomAssignment> getRoomsByGuestAndOwner(String guestMobile, String ownerMobile) {
        return roomAssignmentRepo.findByGuestMobileAndOwnerMobile(guestMobile, ownerMobile);
    }
	
	
}
