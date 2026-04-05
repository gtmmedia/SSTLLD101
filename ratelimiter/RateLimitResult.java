/**
 * RateLimitResult
 * 
 * Encapsulates the result of a rate limit check.
 * Contains the decision (allowed/denied) and metadata for logging/monitoring.
 */
public class RateLimitResult {
    private final boolean allowed;
    private final int remainingRequests;
    private final long resetTimeMs;
    private final int totalLimit;
    
    /**
     * Creates a rate limit result.
     * 
     * @param allowed Whether the request is allowed
     * @param remainingRequests Number of requests remaining in current window
     * @param resetTimeMs Milliseconds until window resets
     * @param totalLimit Total limit configured
     */
    public RateLimitResult(boolean allowed, int remainingRequests, long resetTimeMs, int totalLimit) {
        this.allowed = allowed;
        this.remainingRequests = remainingRequests;
        this.resetTimeMs = resetTimeMs;
        this.totalLimit = totalLimit;
    }
    
    /**
     * Indicates if the request is allowed.
     */
    public boolean isAllowed() {
        return allowed;
    }
    
    /**
     * Number of requests still available in current window.
     * Useful for client-side throttling heuristics.
     */
    public int getRemainingRequests() {
        return remainingRequests;
    }
    
    /**
     * Milliseconds until rate limit window resets.
     * Useful for retry-after headers.
     */
    public long getResetTimeMs() {
        return resetTimeMs;
    }
    
    /**
     * Total limit configured for this rate limit check.
     */
    public int getTotalLimit() {
        return totalLimit;
    }
    
    @Override
    public String toString() {
        return String.format(
            "RateLimitResult{allowed=%s, remaining=%d, resetMs=%d, limit=%d}",
            allowed, remainingRequests, resetTimeMs, totalLimit
        );
    }
}
