package com.ash.main.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ash.main.entity.GuestDetailEntity;
import com.ash.main.entity.LeaveRequestEntity;
import com.ash.main.entity.RequestStatus;
import com.ash.main.entity.UserDetailsEntity;
import com.ash.main.rpository.LeaveRequestRepository;
import com.ash.main.rpository.RepositoryG;
import com.ash.main.rpository.RepositoryO;

@Service
public class LeaveRequestService {

	    @Autowired
	    private RepositoryG guestRepo;

	    @Autowired
	    private RepositoryO ownerRepo;
	    
	    @Autowired
	    private LeaveRequestRepository leaveRequestRepo;
	
	
	public String createLeaveRequest(Long guestId, Long ownerId) {
        try {
            Optional<GuestDetailEntity> guestOpt = guestRepo.findById(guestId);
            Optional<UserDetailsEntity> ownerOpt = ownerRepo.findById(ownerId);

            if (guestOpt.isEmpty()) {
                return "Guest not found";
            }

            if (ownerOpt.isEmpty()) {
                return "Owner not found";
            }
            
            
         //  Check if existing pending request exists
            Optional<LeaveRequestEntity> existingReq = leaveRequestRepo
                    .findByGuestIdAndOwnerIdAndStatus(guestId, ownerId, RequestStatus.PENDING);

            if (existingReq.isPresent()) {
                return "Leave request already sent and pending approval.";
            }

            LeaveRequestEntity leaveReq = new LeaveRequestEntity();
            leaveReq.setGuest(guestOpt.get());
            leaveReq.setOwner(ownerOpt.get());
            leaveReq.setStatus(RequestStatus.PENDING);
            leaveReq.setLRequestDate(LocalDateTime.now());

            leaveRequestRepo.save(leaveReq);
            return "Leave request sent successfully to owner ";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
	
	
	
	
	
	public String deleteLeaveRequest(Long guestId, Long ownerId) {

	    LeaveRequestEntity req =
	            leaveRequestRepo.findByGuestIdAndOwnerId(guestId, ownerId);

	    if (req == null) {
	        return "No leave request found.";
	    }

	    leaveRequestRepo.delete(req);

	    return "Leave request cancelled successfully.";
	}

	
	
	
	
}
