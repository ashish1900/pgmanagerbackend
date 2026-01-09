package com.ash.main.rpository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ash.main.dto.AllGuestsDto;
import com.ash.main.dto.GuestRequestDto;
import com.ash.main.dto.LeaveRquestDto;
import com.ash.main.entity.GuestDetailEntity;
import com.ash.main.entity.RequestStatus;
import com.ash.main.entity.StayRequestEntity;
import com.ash.main.entity.UserDetailsEntity;

@Repository
public interface StayRequestRepository extends  JpaRepository<StayRequestEntity, Long> {
	
	List<StayRequestEntity> findByGuestId(Long guestId);
	List<StayRequestEntity> findByOwnerId(Long ownerId);
	
    List<StayRequestEntity> findByGuestIdAndStatus(Long guestId, RequestStatus status);
    
    StayRequestEntity findByGuestIdAndOwnerId(Long guestId, Long ownerId);
   
    
 //  Guest 
    List<StayRequestEntity>
    findByGuest_MoNumber(String moNumber);

    // Owner 
    List<StayRequestEntity>
    findByOwner_MoNumberAndGuest_MoNumber(
            String ownerMobile,
            String guestMobile
    );

    
    
    
    List<StayRequestEntity> findByGuestIdOrderByRequestDateDesc(Long guestId);
                            
                        
    
    
    @Query("""
            SELECT 
                sr,
                lr.lRequestDate,
                lr.lAcceptedDate
            FROM StayRequestEntity sr
            LEFT JOIN LeaveRequestEntity lr
                ON lr.guest.id = sr.guest.id
                AND lr.owner.id = sr.owner.id
            WHERE sr.guest.id = :guestId
            ORDER BY sr.requestDate DESC
        """)
        List<Object[]> fetchFullStayHistory(Long guestId);
    
    
    boolean existsByGuestIdAndStatus(Long guestId, RequestStatus status);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
    	       "FROM StayRequestEntity s " +
    	       "WHERE s.guest.id = :guestId " +
    	       "AND (s.status = com.ash.main.entity.RequestStatus.ACCEPTED " +
    	       "OR s.status = com.ash.main.entity.RequestStatus.REMOVED)")
    	boolean existsByGuestIdAndAcceptedOrRemoved(@Param("guestId") Long guestId);

	
    
    
	 boolean existsByGuestIdAndOwnerId(Long guestId, Long ownerId);
	 
//	 Optional<StayRequestEntity> findFirstByGuestIdAndStatusOrderByRequestDateDesc(Long guestId, RequestStatus status);
	 
	 @Query("SELECT s FROM StayRequestEntity s " +
	           "JOIN s.guest g " +
	           "JOIN s.owner o " +
	           "WHERE g.moNumber = :guestMobile " +
	           "AND o.moNumber = :ownerMobile " +
	           "AND (s.status = com.ash.main.entity.RequestStatus.ACCEPTED OR s.status = com.ash.main.entity.RequestStatus.REMOVED)")
	    Optional<StayRequestEntity> findAcceptedRequest(
	            @Param("guestMobile") String guestMobile,
	            @Param("ownerMobile") String ownerMobile);


	 @Query("SELECT s FROM StayRequestEntity s " +
	           "JOIN s.guest g " +
	           "JOIN s.owner o " +
	           "WHERE g.moNumber = :guestMobile " +
	           "AND o.moNumber = :ownerMobile " +
	           "AND (s.status = com.ash.main.entity.RequestStatus.ACCEPTED)")
	    Optional<StayRequestEntity> findAcceptedRequestforNotice(
	            @Param("guestMobile") String guestMobile,
	            @Param("ownerMobile") String ownerMobile);
	 
	 
//	 List<Object[]> findGuestRequestsByOwnerId(Long id);
	boolean existsByGuestAndOwner(GuestDetailEntity guest, UserDetailsEntity owner);
	
	
	  
	/*
	 * @Query("SELECT new com.ash.main.dto.GuestRequestDto(" +
	 * "s.id, g.id, g.name, g.moNumber, g.tAddress, g.pAddress, s.status, s.requestDate) "
	 * + "FROM StayRequestEntity s " + "JOIN s.guest g " +
	 * "WHERE s.owner.id = :ownerId AND s.status = com.ash.main.entity.RequestStatus.PENDING"
	 * ) List<GuestRequestDto> findPendingGuestRequestsByOwnerId(@Param("ownerId")
	 * Long ownerId);
	 */

	
	@Query("SELECT new com.ash.main.dto.GuestRequestDto(" +
		       "s.id, g.id, g.name, g.moNumber, s.tempAddress, g.pAddress, " +  //  note: s.tempAddress instead of g.tAddress
		       "s.status, s.requestDate, s.idFront, s.idBack) " +
		       "FROM StayRequestEntity s " +
		       "JOIN s.guest g " +
		       "WHERE s.owner.id = :ownerId AND s.status = com.ash.main.entity.RequestStatus.PENDING")
		List<GuestRequestDto> findPendingGuestRequestsByOwnerId(@Param("ownerId") Long ownerId);


	
	@Query("""
		    SELECT new com.ash.main.dto.AllGuestsDto(
		        s.id,
		        g.id,
		        g.name,
		        g.moNumber,
		        s.tempAddress,
		        g.pAddress,
		        s.status,
		        s.requestDate,
		        s.idFront,
		        s.idBack,
		        l.lRequestDate,
		        l.lAcceptedDate
		    )
		    FROM StayRequestEntity s
		    JOIN s.guest g
		    LEFT JOIN LeaveRequestEntity l 
		        ON l.guest.id = g.id AND l.owner.id = s.owner.id
		    WHERE s.owner.id = :ownerId 
		      AND (s.status = com.ash.main.entity.RequestStatus.ACCEPTED 
		           OR s.status = com.ash.main.entity.RequestStatus.REMOVED)
		""")
		List<AllGuestsDto> findAllGustsByOwnerId(@Param("ownerId") Long ownerId);

	
	
	Optional<StayRequestEntity> findTopByGuestIdAndOwnerIdOrderByRequestDateDesc(Long guestId, Long ownerId);
	
	
	@Query("SELECT s FROM StayRequestEntity s WHERE s.guest.id = :guestId AND (s.status = com.ash.main.entity.RequestStatus.ACCEPTED OR s.status = com.ash.main.entity.RequestStatus.REMOVED) ORDER BY s.requestDate DESC")
	List<StayRequestEntity> findAcceptedByGuestIdOrderByDateDesc(@Param("guestId") Long guestId);

	
	
	 // Check if there exists any stay request with given guest and status ACCEPTED
    boolean existsByGuestAndStatus(GuestDetailEntity guest, RequestStatus status);
    
    
    
    
    @Query("""
    	    SELECT new com.ash.main.dto.LeaveRquestDto(
    	        s.id,
    	        g.id,
    	        g.name,
    	        g.moNumber,
    	        s.tempAddress,
    	        g.pAddress,
    	        s.status,
    	        s.requestDate,
    	        s.idFront,
    	        s.idBack,
    	        l.lRequestDate
    	    )
    	    FROM StayRequestEntity s
    	    JOIN s.guest g
    	    JOIN LeaveRequestEntity l ON l.guest.id = g.id AND l.owner.id = s.owner.id
    	    WHERE s.owner.id = :ownerId
    	      AND s.status = com.ash.main.entity.RequestStatus.ACCEPTED
    	      AND l.status = com.ash.main.entity.RequestStatus.PENDING
    	""")
    	List<LeaveRquestDto> findGuestsWithPendingLeaveRequests(@Param("ownerId") Long ownerId);

	
}
