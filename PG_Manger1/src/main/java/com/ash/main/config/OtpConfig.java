
package com.ash.main.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.twilio.Twilio;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@ConfigurationProperties(prefix = "twilio")
@Data
@NoArgsConstructor
public class OtpConfig {

	private String accountSid;
	private String authToken;
	private String phoneNumber;

	@PostConstruct
	public void initTwilio() {

	    if (accountSid == null || authToken == null) {
	        throw new IllegalStateException(" Twilio credentials missing");
	    }

	    Twilio.init(accountSid, authToken);
	    System.out.println(" Twilio initialized");
	}

	
}
