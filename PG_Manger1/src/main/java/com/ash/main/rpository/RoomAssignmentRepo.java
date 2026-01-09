package com.ash.main.rpository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ash.main.entity.RoomAssignment;

@Repository
public interface RoomAssignmentRepo extends JpaRepository<RoomAssignment, Long> {
  
	// boolean existsByGuest(GuestDetailEntity guestId);
	
    boolean existsByGuestIdAndOwnerId(Long guestId, Long ownerId);


	Optional<RoomAssignment> findFirstByGuestIdAndOwnerId(Long id, Long id2);
	
 //   Optional<RoomAssignment> findByGuest_MoNumber(String moNumber);
	
	  //  Fetch all room assignments for a guest + owner
    @Query("SELECT r FROM RoomAssignment r JOIN r.guest g JOIN r.owner o " +
           "WHERE g.moNumber = :guestMobile AND o.moNumber = :ownerMobile")
    List<RoomAssignment> findByGuestMobileAndOwnerMobile(
            @Param("guestMobile") String guestMobile,
            @Param("ownerMobile") String ownerMobile);

	
}
	