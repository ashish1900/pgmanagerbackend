package com.ash.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ash.main.config.OtpConfig;
import com.ash.main.util.OtpStorage;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpConfig otpConfig;
    
    // DEMO CONFIG
    @Value("${demo.login.enabled}")
    private boolean demoEnabled;

    @Value("${demo.owner.mobile}")
    private String ownerDemoMobile;

    @Value("${demo.guest.mobile}")
    private String guestDemoMobile;

    @Value("${demo.otp}")
    private String demoOtp;

    public String sendOtpToPhone(String mobileNumber) {

        //  INTERNAL FORMAT (DB / OTP STORAGE)
        mobileNumber = mobileNumber.replace("+91", "");
    
        
     //  DEMO NUMBER → SMS SKIP
        if (demoEnabled &&
                (mobileNumber.equals(ownerDemoMobile) ||
                 mobileNumber.equals(guestDemoMobile))) {

                System.out.println("DEMO LOGIN OTP (OWNER / GUEST)");
                OtpStorage.saveOtp(mobileNumber, demoOtp);
                return demoOtp;
            }
        
        

        String otp = generateOtp();

        System.out.println("mobileNumber_sendOtpToPhone " + mobileNumber);

        OtpStorage.saveOtp(mobileNumber, otp);

       
        try {
            String twilioNumber = "+91" + mobileNumber;

            PhoneNumber recipient = new PhoneNumber(twilioNumber);
            PhoneNumber sender = new PhoneNumber(otpConfig.getPhoneNumber());

            String msgBody = "Your OTP is : " + otp;

            Message.creator(recipient, sender, msgBody).create();

            System.out.println("✅ OTP sent via Twilio to " + twilioNumber);

        } catch (Exception e) {
            System.err.println("❌ Twilio error: " + e.getMessage());
        }

        System.out.println("OTP generated for " + mobileNumber + ": " + otp);

        return otp;
    }

    
    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    
    
    
    
    public boolean validateOtp(String mobileNumber, String inputOtp) {

        mobileNumber = mobileNumber.replace("+91", "");
        
        
        //  DEMO OTP VALIDATION
        if (demoEnabled &&
                (mobileNumber.equals(ownerDemoMobile) ||
                 mobileNumber.equals(guestDemoMobile)) &&
                inputOtp.equals(demoOtp)) {

                System.out.println("DEMO OTP VERIFIED");
                return true;
            }

        
        String storedOtp = OtpStorage.getOtp(mobileNumber);

        System.out.println("storedOtp " + storedOtp);
        System.out.println("inputOtp " + inputOtp);
        System.out.println("mobileNumber " + mobileNumber);

        if (storedOtp != null && storedOtp.equals(inputOtp)) {
            OtpStorage.removeOtp(mobileNumber);
            return true;
        }
        return false;
    }
}
