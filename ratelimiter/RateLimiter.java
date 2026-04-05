/**
 * RateLimiter
 * 
 * Main facade for rate limiting functionality.
 * Uses a pluggable algorithm to make rate limit decisions.
 * 
 * Thread-safe through synchronized blocks.
 * 
 * Design Pattern: Strategy + Facade
 * - Encapsulates the algorithm choice
 * - Simple interface for clients
 * - Easy to swap algorithms at runtime
 */
public class RateLimiter {
    private RateLimitAlgorithm algorithm;
    
    /**
     * Creates a rate limiter with the specified algorithm.
     * 
     * @param algorithm The rate limiting algorithm to use
     */
    public RateLimiter(RateLimitAlgorithm algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }
        this.algorithm = algorithm;
    }
    
    /**
     * Checks if a request is allowed under the rate limit.
     * 
     * @param key The rate limit key (customer_id, tenant_id, api_key, etc.)
     * @param config The rate limit configuration
     * @return RateLimitResult indicating allow/deny
     */
    public synchronized RateLimitResult isAllowed(String key, RateLimitConfig config) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Rate limit key cannot be null or empty");
        }
        if (config == null) {
            throw new IllegalArgumentException("Rate limit config cannot be null");
        }
        return algorithm.isAllowed(key, config);
    }
    
    /**
     * Changes the rate limiting algorithm at runtime.
     * Allows switching between Fixed Window, Sliding Window, Token Bucket, etc.
     * 
     * @param newAlgorithm The new algorithm to use
     */
    public synchronized void setAlgorithm(RateLimitAlgorithm newAlgorithm) {
        if (newAlgorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }
        this.algorithm = newAlgorithm;
    }
    
    /**
     * Gets the currently configured algorithm.
     */
    public synchronized RateLimitAlgorithm getAlgorithm() {
        return algorithm;
    }
}
