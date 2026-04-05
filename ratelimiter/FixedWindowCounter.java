import java.util.HashMap;
import java.util.Map;

/**
 * FixedWindowCounter
 * 
 * Rate limiting algorithm using fixed time windows.
 * 
 * Algorithm:
 * - Divides time into fixed windows (e.g., 0-60s, 60-120s, 120-180s)
 * - Counts requests in current window
 * - Allows request if count < limit
 * - Resets counter when window changes
 * 
 * Pros:
 * - Simple implementation
 * - O(1) time complexity
 * - Low memory usage per key
 * 
 * Cons:
 * - Not always precise at window boundaries
 * - Can allow burst when transitioning between windows
 * 
 * Example scenario (limit=5 per 60s):
 * - At 59.9s: 5 requests made (at limit, next denied)
 * - At 00.0s: New window starts, counter resets
 * - New requests now allowed from 00.0s
 * - This can create a "burst" where 5 more requests squeezed in quickly
 */
public class FixedWindowCounter implements RateLimitAlgorithm {
    
    // Stores request count per window per key
    // Format: "key:windowIndex" -> count
    private final Map<String, Integer> windowCounts = new HashMap<>();
    
    // Stores the current window index per key
    // Format: "key" -> windowIndex
    private final Map<String, Long> keyWindows = new HashMap<>();
    
    @Override
    public synchronized RateLimitResult isAllowed(String key, RateLimitConfig config) {
        long currentTimeMs = System.currentTimeMillis();
        long windowIndexDurationMs = config.getWindowSeconds() * 1000L;
        long currentWindowIndex = currentTimeMs / windowIndexDurationMs;
        
        // Get the previous window index for this key
        Long previousWindowIndex = keyWindows.get(key);
        
        // If this is a new window, reset the counter
        if (previousWindowIndex == null || previousWindowIndex != currentWindowIndex) {
            keyWindows.put(key, currentWindowIndex);
            windowCounts.put(key, 0);
        }
        
        // Get current count for this key in current window
        int currentCount = windowCounts.getOrDefault(key, 0);
        
        // Check if request is allowed
        boolean allowed = currentCount < config.getLimit();
        
        if (allowed) {
            windowCounts.put(key, currentCount + 1);
        }
        
        // Calculate remaining requests and reset time
        int remaining = Math.max(0, config.getLimit() - (allowed ? currentCount + 1 : currentCount));
        long currentWindowStartMs = currentWindowIndex * windowIndexDurationMs;
        long nextWindowStartMs = currentWindowStartMs + windowIndexDurationMs;
        long resetTimeMs = nextWindowStartMs - currentTimeMs;
        
        return new RateLimitResult(allowed, remaining, resetTimeMs, config.getLimit());
    }
    
    /**
     * Resets all rate limiting state.
     * Useful for testing.
     */
    public synchronized void reset() {
        windowCounts.clear();
        keyWindows.clear();
    }
    
    /**
     * Gets current count for debugging/monitoring.
     */
    public synchronized int getCurrentCount(String key) {
        return windowCounts.getOrDefault(key, 0);
    }
}
