package com.ash.main.rpository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ash.main.entity.LeaveRequestEntity;
import com.ash.main.entity.RequestStatus;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {
	
	Optional<LeaveRequestEntity> findByGuestIdAndOwnerIdAndStatus(Long guestId, Long ownerId, RequestStatus status);
	
	LeaveRequestEntity findByGuestIdAndOwnerId(Long guestId, Long ownerId);
	
	

	
	
}
