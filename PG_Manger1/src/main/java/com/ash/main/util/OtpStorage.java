package com.ash.main.util;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class OtpStorage {
	private static final Map<String, String> otpMap = new ConcurrentHashMap<>();
	private static final Map<String, Long> otpExpiryMap = new ConcurrentHashMap<>();

	private static final long EXPIRY_DURATION_MS = 5 * 60 * 1000; // 5 minutes

	public static void saveOtp(String mobileNumber, String otp) {
		otpMap.put(mobileNumber, otp);
		otpExpiryMap.put(mobileNumber, System.currentTimeMillis() + EXPIRY_DURATION_MS);
	}
	public static String getOtp(String mobileNumber) {
		Long expiryTime = otpExpiryMap.get(mobileNumber);
		if (expiryTime == null || System.currentTimeMillis() > expiryTime) {
			otpMap.remove(mobileNumber);
			otpExpiryMap.remove(mobileNumber);
			
			System.out.println("mobileNumber_OtpStorage "+mobileNumber);
			
			return null;
		}
		return otpMap.get(mobileNumber);
	}
	public static void removeOtp(String mobileNumber) {
		otpMap.remove(mobileNumber);
		otpExpiryMap.remove(mobileNumber);
	}
}
