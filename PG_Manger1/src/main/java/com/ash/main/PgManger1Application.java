package com.ash.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ash.main.config.OtpConfig;
import com.twilio.Twilio;

@SpringBootApplication
public class PgManger1Application {
	
	/*
	 * @Autowired private OtpConfig otpConfig;
	 * 
	 * @PostConstruct private void setup() { Twilio.init(otpConfig.getAccountSid(),
	 * otpConfig.getAuthToken()); }
	 */

	public static void main(String[] args) {
		SpringApplication.run(PgManger1Application.class, args);
	}

}
