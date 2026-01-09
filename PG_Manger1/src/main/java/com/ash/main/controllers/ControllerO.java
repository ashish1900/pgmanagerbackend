package com.ash.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ash.main.dto.VerifyAndRegister;
import com.ash.main.service.ServiceO;
import com.ash.main.dto.UserLoginDetails;

@RestController
public class ControllerO {	
	
	@Autowired
	ServiceO serviceO;
	
	
	@PostMapping("/login")
	public ResponseEntity<VerifyAndRegister> userLogin(@RequestBody UserLoginDetails userLoginDetails){
		
		VerifyAndRegister userData = serviceO.userLogin(userLoginDetails);
		 
		
		if(userData != null)
		{
			return new ResponseEntity<VerifyAndRegister>(userData, HttpStatus.OK);
		}
		
		
	   return new ResponseEntity<VerifyAndRegister>(userData, HttpStatus.UNAUTHORIZED);
	   
	}
	
	
	
	
	

}
