# Implementation Guide & Advanced Usage

## Quick Start

### Basic Setup

```java
// Step 1: Create a database implementation
Database database = new FakeDatabase();
((FakeDatabase) database).init("user:1", "John Doe");
((FakeDatabase) database).init("user:2", "Jane Smith");

// Step 2: Create distributed cache
DistributedCache cache = new DistributedCache(
    3,                                      // Number of nodes
    10,                                     // Entries per node (max)
    new ModuloDistributionStrategy(),       // Distribution strategy
    new LRUEvictionPolicy(),                // Eviction policy
    database                                // Database for misses
);

// Step 3: Use it!
cache.put("key1", "value1");
Object value = cache.get("key1");
```

## File Structure

```
distributed-cache/
├── Database.java                 # Interface for external data source
├── DistributionStrategy.java     # Interface for key-to-node routing
├── ModuloDistributionStrategy.java
├── EvictionPolicy.java           # Interface for eviction strategy
├── LRUEvictionPolicy.java        # Least Recently Used implementation
├── CacheNode.java                # Single cache node
├── DistributedCache.java         # Main orchestrator
├── FakeDatabase.java             # Mock database for testing
├── Main.java                     # Examples and demonstrations
├── README.md                     # Architecture & design overview
├── DESIGN_NOTES.md              # Detailed design decisions
└── IMPLEMENTATION_GUIDE.md      # This file
```

## Class Relationships

### Inheritance Hierarchy
```
Interface DistributionStrategy
    └── ModuloDistributionStrategy (concrete)

Interface EvictionPolicy
    └── LRUEvictionPolicy (concrete)

Interface Database
    └── FakeDatabase (concrete, for testing)
```

### Composition Hierarchy
```
DistributedCache
    ├── CacheNode[] (array)
    ├── DistributionStrategy (dependency)
    ├── Database (dependency)
    │
    └── CacheNode (composition)
        ├── EvictionPolicy (dependency)
        └── Map<String, Object> (storage)
```

## Detailed API Reference

### DistributedCache

#### Constructor
```java
public DistributedCache(
    int nodeCount,                          // Number of cache nodes (> 0)
    int nodeCapacity,                       // Max entries per node (> 0)
    DistributionStrategy distributionStrategy,  // Strategy for routing
    EvictionPolicy evictionPolicy,          // Policy for eviction
    Database database                       // Data source for misses
)
```

#### Methods

**get(String key)**
```java
Object value = cache.get("user:123");

// Behavior:
// 1. Validates key (not null, not empty)
// 2. Routes to appropriate node using strategy
// 3. Returns value if in cache (cache hit)
// 4. If not in cache (cache miss):
//    a. Fetches from database
//    b. Stores in cache (may evict LRU)
//    c. Returns value
// 5. Throws exception if key not in database

// Time Complexity: O(1)
```

**put(String key, Object value)**
```java
cache.put("user:123", "John Doe");

// Behavior:
// 1. Validates key (not null, not empty)
// 2. Validates value (not null)
// 3. Routes to appropriate node using strategy
// 4. If key exists: updates value
// 5. If key doesn't exist and node full: evicts LRU
// 6. Stores key-value pair
// 7. Updates database

// Time Complexity: O(1)
```

**getStats()**
```java
String stats = cache.getStats();
System.out.println(stats);

// Output:
// === Distributed Cache Statistics ===
// Total Nodes: 3
// Distribution Strategy: ModuloDistributionStrategy
//
// Node 0: 5/10 entries
// Node 1: 8/10 entries
// Node 2: 7/10 entries
```

### CacheNode

**get(String key)**
```java
Object value = node.get("key");
// Returns value if exists, null otherwise
// Side effect: Updates access time in eviction policy for LRU
```

**put(String key, Object value)**
```java
node.put("key", "value");
// Stores key-value
// If node full: evicts LRU entry first
```

**containsKey(String key)**
```java
boolean exists = node.containsKey("key");
```

**size()**
```java
int count = node.size();  // Current number of entries
```

**getCapacity()**
```java
int max = node.getCapacity();  // Max capacity of node
```

### DistributionStrategy

**getNodeIndex(String key, int numberOfNodes)**
```java
DistributionStrategy strategy = new ModuloDistributionStrategy();
int nodeIndex = strategy.getNodeIndex("user:123", 3);
// Returns: 0, 1, or 2 (for 3 nodes)
// Always returns same index for same key
```

### EvictionPolicy

**recordAccess(String key)**
```java
policy.recordAccess("key");
// Called whenever a key is accessed (get or put)
// Updates access tracking for eviction decision
```

**getKeyToEvict()**
```java
String keyToEvict = policy.getKeyToEvict();
// Returns the key that should be evicted
// For LRU: returns least recently used key
// For LFU: would return least frequently used key
```

**removeKey(String key)**
```java
policy.removeKey("key");
// Removes key from eviction tracking
// Called when key is actually deleted from cache
```

## Common Usage Patterns

### Pattern 1: Basic Caching

```java
Database db = new FakeDatabase();
db.init("config:timeout", "30000");

DistributedCache cache = new DistributedCache(
    5, 100,
    new ModuloDistributionStrategy(),
    new LRUEvictionPolicy(),
    db
);

// Read-through cache
Object timeout = cache.get("config:timeout");  // Fetches from DB
Object timeout2 = cache.get("config:timeout"); // Gets from cache
```

### Pattern 2: Cache-Aside

```java
Database db = new FakeDatabase();
DistributedCache cache = new DistributedCache(
    5, 100,
    new ModuloDistributionStrategy(),
    new LRUEvictionPolicy(),
    db
);

// Manually manage cache
try {
    Object value = cache.get("key");
} catch (RuntimeException e) {
    // Key not in database, handle accordingly
}

// Update cache explicitly
cache.put("key", "new_value");
```

### Pattern 3: Batch Loading

```java
Database db = new FakeDatabase();
// Initialize database with multiple values
for (int i = 0; i < 1000; i++) {
    db.init("item:" + i, "value" + i);
}

DistributedCache cache = new DistributedCache(
    10, 50,  // 10 nodes, 50 per node = 500 total capacity
    new ModuloDistributionStrategy(),
    new LRUEvictionPolicy(),
    db
);

// Cache is empty, but can hold 500 items
// As items are accessed, they're cached
for (int i = 0; i < 100; i++) {
    Object val = cache.get("item:" + i);
}
```

## Extension Examples

### Creating Custom Distribution Strategy

```java
public class RingDistributionStrategy implements DistributionStrategy {
    @Override
    public int getNodeIndex(String key, int numberOfNodes) {
        // Consistent hashing implementation
        int hash = Math.abs(key.hashCode());
        return hash % numberOfNodes;  // Different formula = different routing
    }
}

// Usage
DistributedCache cache = new DistributedCache(
    5, 100,
    new RingDistributionStrategy(),  // New strategy
    new LRUEvictionPolicy(),
    db
);
```

### Creating Custom Eviction Policy

```java
public class MRUEvictionPolicy implements EvictionPolicy {
    private LinkedHashMap<String, Long> accessOrder;
    
    public MRUEvictionPolicy() {
        accessOrder = new LinkedHashMap<String, Long>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return false;
            }
        };
    }
    
    @Override
    public void recordAccess(String key) {
        accessOrder.put(key, System.currentTimeMillis());
    }
    
    @Override
    public String getKeyToEvict() {
        if (accessOrder.isEmpty()) return null;
        // Return LAST entry (most recently used) instead of first
        String lastKey = null;
        for (String key : accessOrder.keySet()) {
            lastKey = key;
        }
        return lastKey;
    }
    
    @Override
    public void removeKey(String key) {
        accessOrder.remove(key);
    }
}

// Usage
DistributedCache cache = new DistributedCache(
    5, 100,
    new ModuloDistributionStrategy(),
    new MRUEvictionPolicy(),  // Most Recently Used instead of LRU
    db
);
```

### Creating Different Database Implementation

```java
public class RealDatabase implements Database {
    private Connection connection;
    
    public RealDatabase(String dbUrl) {
        // Connect to real database
    }
    
    @Override
    public Object getValue(String key) {
        // Execute SQL query
        String sql = "SELECT value FROM cache_table WHERE key = ?";
        // ... execute and return
    }
    
    @Override
    public void setValue(String key, Object value) {
        // Execute SQL insert/update
        String sql = "INSERT INTO cache_table VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE value = ?";
        // ... execute
    }
}

// Usage
Database realDb = new RealDatabase("jdbc:mysql://localhost/cache");
DistributedCache cache = new DistributedCache(
    5, 100,
    new ModuloDistributionStrategy(),
    new LRUEvictionPolicy(),
    realDb  // Real database instead of fake
);
```

## Performance Considerations

### Cache Size Planning

```java
// Config: 5 nodes, 100 entries per node
int totalCapacity = 5 * 100;  // 500 entries

// Memory estimation
int avgValueSize = 1024;      // 1KB per value
long totalMemory = totalCapacity * avgValueSize;  // 500KB

// For larger needs:
DistributedCache largeCache = new DistributedCache(
    100,    // More nodes
    1000,   // More per node
    ...
);
// Total: 100,000 entries ~ 100MB
```

### Hit Rate Optimization

```java
// Working set size (keys accessed regularly): ~1000
// Cache size should be >= working set size
DistributedCache cache = new DistributedCache(
    10,     // 10 nodes
    200,    // 200 per node = 2000 total (2x working set)
    new ModuloDistributionStrategy(),
    new LRUEvictionPolicy(),
    db
);
// Expected hit rate: 90-95%
```

### Hotspot Keys

```java
// If certain keys accessed much more frequently
// Could use LFU instead of LRU
DistributedCache cache = new DistributedCache(
    10, 100,
    new ModuloDistributionStrategy(),
    new LRUEvictionPolicy(),  // Could be: new LFUEvictionPolicy()
    db
);
// LRU: evicts least recently accessed
// LFU: evicts least frequently accessed (better for hotspots)
```

## Testing Guide

### Unit Tests for Each Component

```java
public class ModuloDistributionStrategyTest {
    @Test
    public void testSameKeyAlwaysMapSameNode() {
        DistributionStrategy strategy = new ModuloDistributionStrategy();
        int node1 = strategy.getNodeIndex("key", 5);
        int node2 = strategy.getNodeIndex("key", 5);
        assertEquals(node1, node2);  // Should be same
    }
    
    @Test
    public void testKeysDistributedAcrossNodes() {
        // ... test that different keys go to different nodes
    }
}

public class LRUEvictionPolicyTest {
    @Test
    public void testEvictsLeastRecentlyUsed() {
        EvictionPolicy policy = new LRUEvictionPolicy();
        policy.recordAccess("a");
        policy.recordAccess("b");
        policy.recordAccess("c");
        assertEquals("a", policy.getKeyToEvict());  // First one added
    }
}

public class CacheNodeTest {
    @Test
    public void testEvictionWhenFull() {
        CacheNode node = new CacheNode(2, new LRUEvictionPolicy());
        node.put("k1", "v1");
        node.put("k2", "v2");
        node.put("k3", "v3");  // Should evict k1
        assertNull(node.get("k1"));
        assertEquals("v2", node.get("k2"));
    }
}

public class DistributedCacheTest {
    @Test
    public void testCacheHit() {
        // First get: cache miss, fetches from DB
        // Second get: cache hit, from cache
    }
    
    @Test
    public void testDistributionAcrossNodes() {
        // Verify keys routed to correct nodes
    }
}
```

## Troubleshooting

### Issue: Cache always misses
```java
// Check 1: Is database initialized?
FakeDatabase db = new FakeDatabase();
db.init("key", "value");  // Must initialize before use

// Check 2: Is key format correct?
cache.get("key");         // Should work
cache.get(null);          // Will throw exception
cache.get("");            // Will throw exception
```

### Issue: Memory usage keeps growing
```java
// Check 1: Is node capacity too large?
// Current:
new DistributedCache(5, 10000, ...)  // 50,000 entries = lots of memory

// Better:
new DistributedCache(5, 100, ...)    // 500 entries = reasonable

// Check 2: Add TTL (future enhancement)
// Items should expire after some time, not live forever
```

### Issue: Uneven distribution
```java
// Some nodes have more entries than others
// This is normal with modulo distribution!

// If critically unbalanced:
// Use ConsistentHashStrategy (future)
// Or implement weighted distribution

// Check distribution:
System.out.println(cache.getStats());
// Node 0: 60/100  <-- Higher
// Node 1: 40/100  <-- Lower
// Normal variance with random keys
```

## Summary

**Key Takeaways**:
1. DistributedCache routes keys to appropriate nodes
2. Each node has independent LRU eviction
3. Cache miss triggers database fetch
4. pluggable strategies for distribution and eviction
5. Clean OOP design enables easy extensions
6. O(1) operations for get/put/eviction

**Next Steps**:
1. Implement ConsistentHashStrategy for better scalability
2. Add TTL/expiration support
3. Make thread-safe with locks
4. Add metrics and monitoring
5. Implement persistence to disk
