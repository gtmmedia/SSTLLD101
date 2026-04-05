/**
 * DistributedCache is the main class that orchestrates a distributed cache system.
 * 
 * Architecture:
 * - Multiple cache nodes distributed across the system
 * - Uses a pluggable distribution strategy to map keys to nodes
 * - Each node has its own eviction policy
 * - Integrates with a database for cache misses
 * 
 * Key responsibilities:
 * 1. Route get/put requests to appropriate nodes using distribution strategy
 * 2. Handle cache misses by fetching from database
 * 3. Support pluggable strategies for extensibility
 */
public class DistributedCache {
    private final CacheNode[] nodes;
    private final DistributionStrategy distributionStrategy;
    private final Database database;
    private final int nodeCount;
    
    /**
     * Constructor for DistributedCache
     * @param nodeCount number of cache nodes to create
     * @param nodeCapacity capacity of each node (max entries)
     * @param distributionStrategy strategy used to map keys to nodes
     * @param evictionPolicy eviction policy used by each node
     * @param database the database to fetch values on cache miss
     */
    public DistributedCache(int nodeCount, int nodeCapacity, 
                           DistributionStrategy distributionStrategy,
                           EvictionPolicy evictionPolicy, 
                           Database database) {
        if (nodeCount <= 0) {
            throw new IllegalArgumentException("Node count must be greater than 0");
        }
        
        this.nodeCount = nodeCount;
        this.distributionStrategy = distributionStrategy;
        this.database = database;
        this.nodes = new CacheNode[nodeCount];
        
        // Initialize all cache nodes with the same eviction policy class
        for (int i = 0; i < nodeCount; i++) {
            // Create new instance of eviction policy for each node
            EvictionPolicy nodePolicy = createEvictionPolicyCopy(evictionPolicy);
            this.nodes[i] = new CacheNode(nodeCapacity, nodePolicy);
        }
    }
    
    /**
     * Get value from cache. If not present, fetch from database and cache it.
     * 
     * Flow:
     * 1. Determine which node should have this key (using distribution strategy)
     * 2. Check if key exists in that node
     * 3. If found, return it
     * 4. If not found (cache miss):
     *    a. Fetch from database
     *    b. Store in cache
     *    c. Return the value
     * 
     * @param key the key to retrieve
     * @return the value associated with the key
     * @throws IllegalArgumentException if key is null or empty
     * @throws RuntimeException if value not found in database (cache miss)
     */
    public Object get(String key) {
        validateKey(key);
        
        int nodeIndex = distributionStrategy.getNodeIndex(key, nodeCount);
        CacheNode node = nodes[nodeIndex];
        
        // Check if key exists in the designated node
        Object value = node.get(key);
        if (value != null) {
            System.out.println("Cache HIT for key: " + key + " from node: " + nodeIndex);
            return value;
        }
        
        // Cache MISS - fetch from database
        System.out.println("Cache MISS for key: " + key + ". Fetching from database...");
        value = database.getValue(key);
        
        if (value == null) {
            throw new RuntimeException("Key not found in database: " + key);
        }
        
        // Store in cache for future access
        node.put(key, value);
        return value;
    }
    
    /**
     * Put value in cache
     * 
     * Flow:
     * 1. Determine which node should store this key
     * 2. Store the value in that node
     * 3. Update the database (assumption: this is done here)
     * 
     * @param key the key to store
     * @param value the value to store
     * @throws IllegalArgumentException if key is null or empty
     */
    public void put(String key, Object value) {
        validateKey(key);
        
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        int nodeIndex = distributionStrategy.getNodeIndex(key, nodeCount);
        CacheNode node = nodes[nodeIndex];
        
        // Store in cache
        node.put(key, value);
        System.out.println("Stored key: " + key + " in node: " + nodeIndex);
        
        // Update database (assumption: done here)
        database.setValue(key, value);
    }
    
    /**
     * Get statistics about all cache nodes
     * @return string representation of cache stats
     */
    public String getStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Distributed Cache Statistics ===\n");
        stats.append("Total Nodes: ").append(nodeCount).append("\n");
        stats.append("Distribution Strategy: ").append(distributionStrategy.getClass().getSimpleName()).append("\n\n");
        
        for (int i = 0; i < nodeCount; i++) {
            stats.append("Node ").append(i).append(": ");
            stats.append(nodes[i].size()).append("/").append(nodes[i].getCapacity()).append(" entries\n");
        }
        
        return stats.toString();
    }
    
    // Private helper methods
    
    private void validateKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
    }
    
    /**
     * Create a new instance of the eviction policy
     * This ensures each node gets its own independent policy instance
     */
    private EvictionPolicy createEvictionPolicyCopy(EvictionPolicy policy) {
        if (policy instanceof LRUEvictionPolicy) {
            return new LRUEvictionPolicy();
        }
        // Add more policy types as they're implemented
        throw new IllegalArgumentException("Unknown eviction policy type");
    }
}
