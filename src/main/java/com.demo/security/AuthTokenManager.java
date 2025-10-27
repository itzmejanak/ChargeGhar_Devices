package com.demo.security;

/**
 * Singleton class to manage JWT tokens for ChargeGhar Main API
 * Thread-safe implementation
 */
public class AuthTokenManager {
    
    private static volatile AuthTokenManager instance;
    private volatile String accessToken;
    private volatile String refreshToken;
    private volatile long tokenExpiresAt;  // Unix timestamp in milliseconds
    
    private static final long TOKEN_EXPIRY_BUFFER = 5 * 60 * 1000;  // 5 minutes buffer
    
    private AuthTokenManager() {}
    
    public static AuthTokenManager getInstance() {
        if (instance == null) {
            synchronized (AuthTokenManager.class) {
                if (instance == null) {
                    instance = new AuthTokenManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Set access token and calculate expiry time
     * JWT tokens typically expire in 60 minutes
     */
    public synchronized void setAccessToken(String token) {
        this.accessToken = token;
        // Assume 60 minute expiry, minus 5 minute buffer
        this.tokenExpiresAt = System.currentTimeMillis() + (55 * 60 * 1000);
    }
    
    public synchronized void setRefreshToken(String token) {
        this.refreshToken = token;
    }
    
    public synchronized String getAccessToken() {
        return accessToken;
    }
    
    public synchronized String getRefreshToken() {
        return refreshToken;
    }
    
    /**
     * Check if access token is expired or about to expire
     */
    public synchronized boolean isTokenExpired() {
        if (accessToken == null) {
            return true;
        }
        return System.currentTimeMillis() >= tokenExpiresAt;
    }
    
    /**
     * Clear all tokens (useful for logout or error recovery)
     */
    public synchronized void clearTokens() {
        this.accessToken = null;
        this.refreshToken = null;
        this.tokenExpiresAt = 0;
    }
}
