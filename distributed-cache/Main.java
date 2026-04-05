/**
 * Main class demonstrates the Distributed Cache system with examples
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    DISTRIBUTED CACHE SYSTEM DEMO");
        System.out.println("========================================\n");
        
        // Initialize database with test data
        FakeDatabase db = new FakeDatabase();
        db.init("user:1", "John Doe");
        db.init("user:2", "Jane Smith");
        db.init("user:3", "Bob Johnson");
        db.init("config:timeout", "30000");
        db.init("config:retry", "3");
        
        // Create distributed cache with:
        // - 3 cache nodes
        // - Capacity of 2 entries per node
        // - Modulo-based distribution strategy
        // - LRU eviction policy
        DistributedCache cache = new DistributedCache(
            3,                                      // Number of nodes
            2,                                      // Capacity per node
            new ModuloDistributionStrategy(),       // Distribution strategy
            new LRUEvictionPolicy(),                // Eviction policy
            db                                      // Database
        );
        
        System.out.println(">>> TEST 1: Basic GET and PUT Operations\n");
        testBasicOperations(cache);
        
        System.out.println("\n>>> TEST 2: Cache Miss Handling\n");
        testCacheMissHandling(cache);
        
        System.out.println("\n>>> TEST 3: Distribution Across Nodes\n");
        testDistribution(cache);
        
        System.out.println("\n>>> TEST 4: LRU Eviction Policy\n");
        testLRUEviction(cache);
        
        System.out.println("\n>>> TEST 5: Cache Statistics\n");
        System.out.println(cache.getStats());
    }
    
    private static void testBasicOperations(DistributedCache cache) {
        System.out.println("1. Get user:1 (cache miss, will fetch from DB)");
        Object user1 = cache.get("user:1");
        System.out.println("   Retrieved: " + user1 + "\n");
        
        System.out.println("2. Get user:1 again (cache hit)");
        Object user1Again = cache.get("user:1");
        System.out.println("   Retrieved: " + user1Again + "\n");
        
        System.out.println("3. Put new key-value pair");
        cache.put("session:123", "active_session_data");
        System.out.println("   Stored session:123\n");
    }
    
    private static void testCacheMissHandling(DistributedCache cache) {
        System.out.println("1. Get user:2 from database (first access)");
        Object user2 = cache.get("user:2");
        System.out.println("   Value: " + user2 + "\n");
        
        System.out.println("2. Get config:timeout (first access)");
        Object timeout = cache.get("config:timeout");
        System.out.println("   Value: " + timeout + "\n");
        
        System.out.println("3. Get user:2 again (now from cache)");
        Object user2Again = cache.get("user:2");
        System.out.println("   Value: " + user2Again);
    }
    
    private static void testDistribution(DistributedCache cache) {
        System.out.println("Distribution strategy: Modulo (hash(key) % numberOfNodes)\n");
        
        String[] keys = {"user:1", "user:2", "user:3", "config:timeout", "config:retry"};
        int nodeCount = 3;
        
        ModuloDistributionStrategy strategy = new ModuloDistributionStrategy();
        
        for (String key : keys) {
            int nodeIndex = strategy.getNodeIndex(key, nodeCount);
            System.out.println("Key: " + key + " -> Node " + nodeIndex + 
                             " (hash: " + Math.abs(key.hashCode()) + ")");
        }
    }
    
    private static void testLRUEviction() {
        System.out.println("Demonstrating LRU Eviction with small cache:\n");
        
        // Create a small cache with only 2 nodes, 2 capacity each
        FakeDatabase db = new FakeDatabase();
        db.init("key1", "value1");
        db.init("key2", "value2");
        db.init("key3", "value3");
        db.init("key4", "value4");
        db.init("key5", "value5");
        
        DistributedCache cache = new DistributedCache(
            2,
            2,
            new ModuloDistributionStrategy(),
            new LRUEvictionPolicy(),
            db
        );
        
        System.out.println("Step 1: Put key1");
        cache.put("key1", "value1");
        System.out.println(cache.getStats());
        
        System.out.println("Step 2: Put key2");
        cache.put("key2", "value2");
        System.out.println(cache.getStats());
        
        System.out.println("Step 3: Put key3");
        cache.put("key3", "value3");
        System.out.println(cache.getStats());
        
        System.out.println("Step 4: Put key4");
        cache.put("key4", "value4");
        System.out.println(cache.getStats());
        
        System.out.println("Step 5: Put key5");
        cache.put("key5", "value5");
        System.out.println(cache.getStats());
        
        System.out.println("NOTE: Nodes evict least recently used entries when capacity is reached");
    }
    
    private static void testLRUEviction(DistributedCache cache) {
        System.out.println("LRU (Least Recently Used) Eviction Policy:\n");
        System.out.println("Principle: When a node reaches capacity, the least recently");
        System.out.println("accessed entry is evicted to make room for new entries.\n");
        
        System.out.println("Example with 2 nodes, 2 capacity each:");
        
        FakeDatabase db2 = new FakeDatabase();
        db2.init("a", "value_a");
        db2.init("b", "value_b");
        db2.init("c", "value_c");
        
        DistributedCache smallCache = new DistributedCache(2, 2, 
            new ModuloDistributionStrategy(),
            new LRUEvictionPolicy(),
            db2);
        
        System.out.println("\n1. Put a, b (fill one node partially)");
        smallCache.put("a", "value_a");
        smallCache.put("b", "value_b");
        System.out.println(smallCache.getStats());
        
        System.out.println("2. Put c (might trigger eviction based on node assignment)");
        smallCache.put("c", "value_c");
        System.out.println(smallCache.getStats());
        
        System.out.println("3. Access a (makes 'a' most recently used)");
        Object val = smallCache.get("a");
        System.out.println("   Retrieved: " + val);
        System.out.println(smallCache.getStats());
    }
}
