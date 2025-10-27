package com.demo.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Signature generation and validation for ChargeGhar Main API integration
 * Uses HMAC-SHA256 algorithm for request signing
 */
public class SignChargeGharMain {
    
    private static final String ALGORITHM = "HmacSHA256";
    private final String secretKey;
    
    public SignChargeGharMain(String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        this.secretKey = secretKey;
    }
    
    /**
     * Generate HMAC-SHA256 signature for request
     * 
     * @param payload JSON request body as string
     * @param timestamp Unix timestamp in seconds
     * @return Base64 encoded signature
     */
    public String generateSignature(String payload, long timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String message = payload + timestamp;
        
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(keySpec);
        
        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }
    
    /**
     * Validate signature from response (if needed)
     * 
     * @param payload Response body
     * @param timestamp Timestamp from response
     * @param receivedSignature Signature from X-Signature header
     * @return true if valid, false otherwise
     */
    public boolean validateSignature(String payload, long timestamp, String receivedSignature) {
        try {
            String computedSignature = generateSignature(payload, timestamp);
            return computedSignature.equals(receivedSignature);
        } catch (Exception e) {
            System.err.println("Signature validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current timestamp in seconds
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }
}
