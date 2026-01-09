package com.ash.main.dto;

import java.time.LocalDateTime;

import com.ash.main.entity.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllGuestsDto {
	private Long requestId;
    private Long guestId;
    private String guestName;
    private String guestMobile;
    private String taddress;
    private String pAddress;
    private RequestStatus status; 
   // private String requestDate;
    private LocalDateTime requestDate;
        
    private String idFront;  
    private String idBack;
    
    
    private LocalDateTime lRequestDate;
    private LocalDateTime lAcceptedDate;

	
}
