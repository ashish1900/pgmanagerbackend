package com.ash.main.rpository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ash.main.entity.PaymentUpi;
import com.ash.main.entity.UserDetailsEntity;

public interface PaymentUpiRepository extends JpaRepository<PaymentUpi, Long> {
    Optional<PaymentUpi> findByOwnerAndPaymentType(UserDetailsEntity owner, String paymentType);


    
}
