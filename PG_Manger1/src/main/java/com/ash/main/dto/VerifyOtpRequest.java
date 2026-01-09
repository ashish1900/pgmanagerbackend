package com.ash.main.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "Mobile number is required")
	private String mobileNumber;
   
    @NotBlank(message = "OTP is required")
	private String otp;
	
	
	
	
}
