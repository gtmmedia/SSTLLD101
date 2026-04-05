/**
 * RateLimitConfig
 * 
 * Configuration for rate limiting parameters.
 * Contains the limit count and the time window in seconds.
 * 
 * Example: new RateLimitConfig(5, 60) means 5 requests per 60 seconds
 */
public class RateLimitConfig {
    private final int limit;           // Number of allowed requests
    private final int windowSeconds;   // Time window in seconds
    
    /**
     * Creates a rate limit configuration.
     * 
     * @param limit Maximum number of requests allowed
     * @param windowSeconds Time window in seconds (e.g., 60 for per-minute)
     */
    public RateLimitConfig(int limit, int windowSeconds) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        if (windowSeconds <= 0) {
            throw new IllegalArgumentException("Window must be positive");
        }
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public int getWindowSeconds() {
        return windowSeconds;
    }
    
    @Override
    public String toString() {
        return String.format("%d requests per %d seconds", limit, windowSeconds);
    }
}
