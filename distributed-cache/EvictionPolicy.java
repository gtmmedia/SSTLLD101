/**
 * EvictionPolicy defines how entries should be evicted from a cache node
 * when it reaches capacity.
 * Current implementations: LRU (Least Recently Used)
 * Future implementations: MRU (Most Recently Used), LFU (Least Frequently Used), etc.
 */
public interface EvictionPolicy {
    /**
     * Record that a key has been accessed (get or put)
     * @param key the key that was accessed
     */
    void recordAccess(String key);
    
    /**
     * Get the key that should be evicted (least recently used)
     * @return the key to evict
     */
    String getKeyToEvict();
    
    /**
     * Remove a key's access record when it's deleted from cache
     * @param key the key to remove
     */
    void removeKey(String key);
}
