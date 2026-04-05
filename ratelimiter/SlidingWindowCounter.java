import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SlidingWindowCounter
 * 
 * Rate limiting algorithm using a sliding time window.
 * 
 * Algorithm:
 * - Maintains a list of request timestamps for each key
 * - When checking: removes timestamps older than current time - window duration
 * - Counts remaining timestamps
 * - Allows request if count < limit
 * 
 * Pros:
 * - More accurate rate limiting
 * - Prevents burst at window boundaries
 * - Fair enforcement across time
 * 
 * Cons:
 * - Requires storing timestamps (more memory)
 * - O(n) lookup where n = requests in window
 * - Requires periodic cleanup to prevent unbounded growth
 * 
 * Example scenario (limit=5 per 60s):
 * - Timestamps: [0s, 10s, 20s, 30s, 40s]
 * - At 59s: Window is [59-60, 0]. Only [0-60s] are in window: [0s, 10s, 20s, 30s, 40s] = 5 requests
 * - Next request at 59s is denied (window full)
 * - At 70s: Window is [70-130, 0]. [10s-70s] timestamps: [10s, 20s, 30s, 40s] = 4 requests
 * - Timestamp [0s] is now outside window, so it's removed
 * - New request at 70s is allowed
 * - This prevents the burst that would occur in fixed window
 */
public class SlidingWindowCounter implements RateLimitAlgorithm {
    
    // Stores request timestamps (in ms) for each key
    private final Map<String, List<Long>> keyTimestamps = new HashMap<>();
    
    @Override
    public synchronized RateLimitResult isAllowed(String key, RateLimitConfig config) {
        long currentTimeMs = System.currentTimeMillis();
        long windowDurationMs = config.getWindowSeconds() * 1000L;
        long windowStartMs = currentTimeMs - windowDurationMs;
        
        // Get or create timestamp list for this key
        List<Long> timestamps = keyTimestamps.computeIfAbsent(key, k -> new ArrayList<>());
        
        // Remove timestamps outside the current window (cleanup)
        // This keeps memory bounded and makes counting accurate
        timestamps.removeIf(ts -> ts < windowStartMs);
        
        // Check if request is allowed
        boolean allowed = timestamps.size() < config.getLimit();
        
        if (allowed) {
            timestamps.add(currentTimeMs);
        }
        
        // Calculate remaining requests and reset time
        int currentCount = timestamps.size();
        int remaining = Math.max(0, config.getLimit() - currentCount);
        
        // Reset time is the earliest timestamp + window duration
        long resetTimeMs = 0;
        if (!timestamps.isEmpty()) {
            long earliestTimestampMs = timestamps.get(0);
            resetTimeMs = (earliestTimestampMs + windowDurationMs) - currentTimeMs;
            resetTimeMs = Math.max(0, resetTimeMs);
        }
        
        return new RateLimitResult(allowed, remaining, resetTimeMs, config.getLimit());
    }
    
    /**
     * Resets all rate limiting state.
     * Useful for testing.
     */
    public synchronized void reset() {
        keyTimestamps.clear();
    }
    
    /**
     * Gets current count for debugging/monitoring.
     */
    public synchronized int getCurrentCount(String key) {
        List<Long> timestamps = keyTimestamps.get(key);
        if (timestamps == null) {
            return 0;
        }
        
        // Remove old timestamps before counting
        long currentTimeMs = System.currentTimeMillis();
        timestamps.removeIf(ts -> ts < currentTimeMs - (60 * 1000L)); // Arbitrary cleanup
        return timestamps.size();
    }
}
