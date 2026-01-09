package com.ash.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class VerifyAndRegister {

	private String otp;
	private String moNumber;
	private String name;
	private String pgName;
	private String address;
	private String city;
	private String profileImage;
	
}
