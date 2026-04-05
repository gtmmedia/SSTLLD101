import java.util.*;

/**
 * CacheNode represents a single node in the distributed cache.
 * Each node has:
 * - Limited capacity
 * - An eviction policy (pluggable)
 * - Its own cache storage
 */
public class CacheNode {
    private final int capacity;
    private final Map<String, Object> storage;
    private final EvictionPolicy evictionPolicy;
    
    public CacheNode(int capacity, EvictionPolicy evictionPolicy) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.evictionPolicy = evictionPolicy;
        this.storage = new HashMap<>();
    }
    
    /**
     * Get value from this node
     * @param key the key to retrieve
     * @return the value, or null if not present
     */
    public Object get(String key) {
        if (storage.containsKey(key)) {
            evictionPolicy.recordAccess(key); // Update access time
            return storage.get(key);
        }
        return null;
    }
    
    /**
     * Store value in this node
     * If node is at capacity, evict least recently used key
     * @param key the key to store
     * @param value the value to store
     */
    public void put(String key, Object value) {
        // If key already exists, just update it
        if (storage.containsKey(key)) {
            storage.put(key, value);
            evictionPolicy.recordAccess(key);
            return;
        }
        
        // If at capacity, evict the least recently used key
        if (storage.size() >= capacity) {
            String keyToEvict = evictionPolicy.getKeyToEvict();
            if (keyToEvict != null) {
                storage.remove(keyToEvict);
                evictionPolicy.removeKey(keyToEvict);
            }
        }
        
        // Store the new key-value pair
        storage.put(key, value);
        evictionPolicy.recordAccess(key);
    }
    
    /**
     * Check if a key exists in this node
     * @param key the key to check
     * @return true if key exists
     */
    public boolean containsKey(String key) {
        return storage.containsKey(key);
    }
    
    /**
     * Get current size of this node
     * @return number of entries in this node
     */
    public int size() {
        return storage.size();
    }
    
    /**
     * Get capacity of this node
     * @return maximum entries this node can hold
     */
    public int getCapacity() {
        return capacity;
    }
}
