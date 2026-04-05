import java.util.*;

/**
 * LRUEvictionPolicy implements Least Recently Used eviction.
 * Uses a combination of Doubly Linked List and HashMap for O(1) operations.
 * 
 * When a key is accessed, it's moved to the end (most recently used).
 * When eviction is needed, the first element (least recently used) is returned.
 */
public class LRUEvictionPolicy implements EvictionPolicy {
    private final LinkedHashMap<String, Long> accessOrder;
    
    /**
     * LinkedHashMap in access-order mode: maintains insertion order of accesses
     */
    public LRUEvictionPolicy() {
        // LinkedHashMap with access-order mode (last parameter = true)
        this.accessOrder = new LinkedHashMap<String, Long>(16, 0.75f, true) {
            // Return true to allow access ordering
            protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                return false; // We'll handle removal manually
            }
        };
    }
    
    @Override
    public void recordAccess(String key) {
        accessOrder.put(key, System.currentTimeMillis());
    }
    
    @Override
    public String getKeyToEvict() {
        if (accessOrder.isEmpty()) {
            return null;
        }
        // The first entry is the least recently used (oldest access)
        return accessOrder.keySet().iterator().next();
    }
    
    @Override
    public void removeKey(String key) {
        accessOrder.remove(key);
    }
}
