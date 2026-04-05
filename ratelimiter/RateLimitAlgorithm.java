/**
 * RateLimitAlgorithm interface
 * 
 * Defines contract for rate limiting algorithms.
 * Implementations can use different strategies to decide
 * whether a request should be allowed based on historical patterns.
 * 
 * This is the Strategy pattern - allows swapping algorithms at runtime.
 */
public interface RateLimitAlgorithm {
    
    /**
     * Determines if a request should be allowed based on the rate limit config.
     * 
     * @param key The identifier for rate limiting (customer_id, tenant_id, api_key, etc.)
     * @param config Configuration containing limit count and time window
     * @return RateLimitResult with decision and metadata
     */
    RateLimitResult isAllowed(String key, RateLimitConfig config);
}
