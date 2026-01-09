package com.ash.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyAndRegisterG {
	
	private String otp;
	private String moNumber;
	private String name;
	private String taddress;
	private String paddress;
	private String profileImage;

}
