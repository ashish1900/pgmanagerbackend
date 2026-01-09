package com.ash.main.rpository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ash.main.entity.GuestDetailEntity;
import com.ash.main.entity.PaymentTransaction;
import com.ash.main.entity.UserDetailsEntity;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByGuest(GuestDetailEntity guest);
    List<PaymentTransaction> findByGuestAndPaymentType(GuestDetailEntity guest, String paymentType);
	List<PaymentTransaction> findByOwnerAndStatus(UserDetailsEntity owner, String string);
	
	
	@Query("""
		    SELECT new map(
		        p.id as id,
		        g.name as guestName,
		        g.moNumber as guestMobile,
		        s.tempAddress as taddress,
		        g.pAddress as paddress,
		        p.paymentType as type,
		        p.amount as amount,
		        p.paymentDate as paymentDate,
		        p.status as status,
		        p.receiptImage as receiptImage,
		        s.idFront as idFront,
		        s.idBack as idBack
		    )
		    FROM PaymentTransaction p
		    JOIN p.guest g
		    LEFT JOIN StayRequestEntity s ON s.guest.id = g.id AND s.owner.id = p.owner.id
		    WHERE p.owner = :owner AND p.status = 'Pending'
		""")
		List<Map<String, Object>> findPendingPaymentsWithIdProofs(@Param("owner") UserDetailsEntity owner);
	
	
	
	@Query("""
		    SELECT new map(
		        p.id as id,
		        g.name as guestName,
		        g.id as guestId,
		        g.moNumber as guestMobile,
		        s.tempAddress as taddress,
		        g.pAddress as paddress,
		        p.paymentType as type,
		        p.amount as amount,
		        p.paymentDate as paymentDate,
		        p.verifiedDate as verifiedDate,
		        p.status as status,
		        p.receiptImage as receiptImage,
		        s.idFront as idFront,
		        s.idBack as idBack
		    )
		    FROM PaymentTransaction p
		    JOIN p.guest g
		    LEFT JOIN StayRequestEntity s ON s.guest.id = g.id AND s.owner.id = p.owner.id
		    WHERE p.owner = :owner AND (p.status = 'Verified' OR  p.status = 'Rejected')
		""")
		List<Map<String, Object>> findPaymentHistoryWithIdProofs(@Param("owner") UserDetailsEntity owner);
	
	List<PaymentTransaction> findByGuestAndOwner_MoNumber(GuestDetailEntity guest, String ownerMobile);

	
	
	
}
