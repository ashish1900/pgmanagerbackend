package com.ash.main.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpRequest {
	
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;


}
