# Distributed Cache - Design Notes & Rationale

## Design Decisions & Justifications

### 1. Why Separate CacheNode Class?

**Decision**: Each node is a separate class with its own storage and eviction policy

**Rationale**:
- **Isolation**: Each node operates independently
- **Scaleability**: Easy to extend to true distributed (network) cache
- **Testability**: Can test individual nodes in isolation
- **Flexibility**: Different nodes could have different policies (future enhancement)

```java
// Each node has:
CacheNode node = new CacheNode(capacity, evictionPolicy);
node.put("key", "value");      // Independent storage
node.get("key");                // Independent access tracking
```

### 2. Why Strategy Pattern for Distribution?

**Decision**: DistributionStrategy interface instead of hardcoding

**Benefits**:
- **Pluggable**: Can switch strategies without code change
- **Extensible**: Add consistent hashing later without modifying DistributedCache
- **Testable**: Easy to mock different strategies for testing
- **Maintainable**: Distribution logic isolated in single class

**Example Evolution**:
```
Version 1: ModuloDistributionStrategy (simple, uniform distribution)
    ↓
Version 2: ConsistentHashStrategy (handles node addition/removal)
    ↓
Version 3: WeightedDistributionStrategy (favor faster/larger nodes)
```

### 3. Why Strategy Pattern for Eviction?

**Decision**: EvictionPolicy interface instead of hardcoding LRU

**Benefits**:
- **Flexibility**: Different policies for different use cases
- **Future-proof**: Can add MRU, LFU, TTL without changing cache code
- **Performance**: Right policy for right workload
- **Comparison**: Easy to benchmark strategies

**Different Use Cases**:
- **LRU**: Web page caching, general purpose
- **MRU**: Some scenarios with temporal locality
- **LFU**: Popular items stay longer
- **TTL**: Session caches with time-based validity

### 4. Why LinkedHashMap for LRU?

**Decision**: Use LinkedHashMap with access-order mode instead of manual doubly-linked list

**Rationale**:
- **Simplicity**: Less code, fewer bugs
- **Performance**: O(1) operations for all LRU operations
- **Maintainability**: Standard library component
- **Correctness**: Well-tested implementation

**How it works**:
```
LinkedHashMap tracks access order via doubly-linked list internally
└─ get(): moves element to end (most recent)
└─ put(): adds new element to end
└─ iterator().next(): returns first element (least recent)
```

### 5. Why Validate Keys?

**Decision**: Check for null/empty keys in get() and put()

**Rationale**:
- **Fail-fast**: Catch errors early
- **Predictability**: Clear error messages
- **Consistency**: All null keys are rejected uniform
- **Database safety**: Prevents null from reaching database

### 6. Where Database Updates Happen?

**Design Choice**: Database is updated in put(), not in get()

**Rationale**:
- **Consistency**: All writes go through put()
- **Simpler get()**: Cache miss just reads, doesn't write
- **Assumption clarity**: Clear that database is source of truth
- **Real-world**: In production, this might be async

**Alternative considered**:
```
// Option 1 (current): Update in put() only
cache.put(key, value);        // Updates both cache and DB
Object val = cache.get(key);  // Reads from cache or DB

// Option 2 (not used): Update in get() on miss
// Problem: Multiple accesses would repeatedly update DB
// Not efficient with write-heavy workloads
```

### 7. Why Create Policy Copies for Each Node?

**Decision**: Create new instance of eviction policy for each CacheNode

**Code**:
```java
for (int i = 0; i < nodeCount; i++) {
    EvictionPolicy nodePolicy = createEvictionPolicyCopy(evictionPolicy);
    nodes[i] = new CacheNode(nodeCapacity, nodePolicy);
}
```

**Rationale**:
- **Independence**: Each node tracks its own LRU order
- **Isolation**: Access in node 1 doesn't affect node 2's eviction
- **Correctness**: Each node has separate tracking data

**What would be wrong**:
```java
// WRONG: Sharing same policy across nodes
for (int i = 0; i < nodeCount; i++) {
    nodes[i] = new CacheNode(nodeCapacity, sharedPolicy);
}
// Problem: One node's access affects all nodes' eviction!
```

## Data Flow Diagrams

### GET Operation Flow

```
User: cache.get("user:123")
    │
    ├─ [1] Validate key
    │       ├─ null? → throw exception
    │       └─ empty? → throw exception
    │
    ├─ [2] Route to node
    │       node_id = strategy.getNodeIndex("user:123", 3)
    │       ┌─────────────────────────────┐
    │       │ "user:123".hashCode() = X    │
    │       │ Math.abs(X) % 3 = 0          │
    │       │ Use Node 0 ✓                 │
    │       └─────────────────────────────┘
    │
    ├─ [3] Check node storage
    │       ├─ Found in node[0]?
    │       │   ├─ YES (Cache HIT) ✓
    │       │   │   ├─ Record access: policy.recordAccess("user:123")
    │       │   │   │   └─ Move to end of LinkedHashMap (most recent)
    │       │   │   └─ Return value
    │       │   │
    │       │   └─ NO (Cache MISS) ✗
    │       │       ├─ Call: database.getValue("user:123")
    │       │       ├─ Store in cache
    │       │       │   ├─ Is node full? 
    │       │       │   │   ├─ YES → Evict LRU
    │       │       │   │   │   ├─ keyToEvict = policy.getKeyToEvict()
    │       │       │   │   │   │   └─ Returns first element (least recent)
    │       │       │   │   │   ├─ node[0].remove(keyToEvict)
    │       │       │   │   │   └─ policy.removeKey(keyToEvict)
    │       │       │   │   │
    │       │       │   │   └─ NO → Just add
    │       │       │   │
    │       │       │   └─ node[0].put("user:123", value)
    │       │       │       └─ policy.recordAccess("user:123")
    │       │       │
    │       │       └─ Return value
    │
User receives value
```

### PUT Operation Flow

```
User: cache.put("session:456", "active")
    │
    ├─ [1] Validate
    │       ├─ key null/empty? → exception
    │       └─ value null? → exception
    │
    ├─ [2] Route to node
    │       node_id = strategy.getNodeIndex("session:456", 3)
    │       Result: Node 1
    │
    ├─ [3] Store in node
    │       ├─ Does "session:456" already exist?
    │       │   ├─ YES: Update value
    │       │   │    └─ policy.recordAccess("session:456")
    │       │   │
    │       │   └─ NO: Add new entry
    │       │       ├─ Is node[1] full?
    │       │       │   ├─ YES → Evict LRU
    │       │       │   │
    │       │       │   └─ NO → Just add
    │       │       │
    │       │       └─ node[1].put("session:456", "active")
    │       │           └─ policy.recordAccess("session:456")
    │
    ├─ [4] Update database
    │       database.setValue("session:456", "active")
    │
Done (void return)
```

## Class Responsibilities

### DistributedCache
- **Routes** requests to appropriate cache nodes
- **Orchestrates** interaction between nodes and database
- **Handles** cache miss logic (fetch from database)
- **Manages** statistics and monitoring

### CacheNode
- **Stores** key-value pairs in local HashMap
- **Enforces** capacity constraint
- **Coordinates** with eviction policy
- **Tracks** access for eviction decisions

### DistributionStrategy
- **Maps** keys to node indices deterministically
- **Ensures** scaling: same key → same node
- **Enables** future flexible routing

### EvictionPolicy
- **Tracks** access/usage patterns
- **Selects** victim on eviction
- **Supports** different policies (LRU, MRU, LFU, etc.)

### Database
- **Provides** source of truth
- **Enables** cache refill on miss
- **Persists** data

## Scaling Considerations

### Adding Nodes (Future)

**Problem**: With modulo distribution, adding node changes which keys go where
```
Old: hash % 3 → keys distributed across 3 nodes
New: hash % 4 → 75% of keys now go to different nodes!
```

**Solution**: Use Consistent Hashing (future strategy)
```java
// Future enhancement:
DistributedCache cache = new DistributedCache(
    ...,
    new ConsistentHashStrategy(),  // ← Replace this
    ...
);
// Now: Adding node affects only ~25% of keys
```

### Memory Efficiency

**Current**: Each node stores data in HashMap
```
Total Memory = nodes × capacity × avgValueSize
= 10 nodes × 1000 entries × 1KB = ~10MB
```

**Optimization (future)**: Value compression
```java
// Future enhancement:
public class CompressedCacheNode extends CacheNode {
    // Compress values before storage
    // Decompress on retrieval
}
```

## Thread Safety

**Current**: NOT thread-safe (for LLD simplicity)

**Issues with concurrent access**:
```java
// Thread 1: cache.get("key")
// Thread 2: cache.put("key", value)
// Race condition possible!
```

**Future enhancement**:
```java
public class ThreadSafeCacheNode extends CacheNode {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Override
    public Object get(String key) {
        lock.readLock().lock();
        try {
            return super.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void put(String key, Object value) {
        lock.writeLock().lock();
        try {
            super.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

## Potential Issues & Solutions

### Issue 1: Database Inconsistency
**Problem**: Cache has old value, database updated externally
**Solution**: 
- Cache invalidation strategy
- TTL-based expiration
- Event-based cache update

### Issue 2: Hotspot Keys
**Problem**: Some keys accessed much more than others
**Solution**:
- Monitor access patterns
- Implement LFU instead of LRU
- Replicate hot keys across multiple nodes

### Issue 3: Network Latency (distributed)
**Problem**: Network delay on cache miss
**Solution**:
- Local caching layer
- Prefetching/warming
- Async operations

### Issue 4: Memory Pressure
**Problem**: Cache consumes too much memory
**Solution**:
- Implement TTL/expiration
- Use compression
- Implement spillover to disk
- Estimate value sizes

## Example: Adding Consistent Hash Strategy

```java
public class ConsistentHashDistributionStrategy implements DistributionStrategy {
    private final TreeMap<Integer, Integer> ring = new TreeMap<>();
    private final int virtualNodes = 160;
    
    public ConsistentHashDistributionStrategy(int numberOfNodes) {
        for (int i = 0; i < numberOfNodes; i++) {
            for (int j = 0; j < virtualNodes; j++) {
                int hash = ("node" + i + ":" + j).hashCode();
                ring.put(Math.abs(hash), i);
            }
        }
    }
    
    @Override
    public int getNodeIndex(String key, int numberOfNodes) {
        if (ring.isEmpty()) return 0;
        
        int hash = Math.abs(key.hashCode());
        Map.Entry<Integer, Integer> entry = ring.ceilingEntry(hash);
        return entry == null ? ring.firstEntry().getValue() : entry.getValue();
    }
}

// Usage: Drop-in replacement!
DistributedCache cache = new DistributedCache(
    ...,
    new ConsistentHashDistributionStrategy(nodeCount),
    ...
);
```

## Testing Strategy

```
Unit Tests:
  └─ ModuloDistributionStrategy
       ├─ Same key always maps to same node
       ├─ Keys distributed across all nodes
       └─ Handles edge cases (negative hash, etc.)
  
  └─ LRUEvictionPolicy
       ├─ Recent access moves to end
       ├─ Evict returns least recent
       └─ Removal clears tracking
  
  └─ CacheNode
       ├─ Get existing key
       ├─ Get non-existing key
       ├─ Put updates existing
       ├─ Put new when not full
       ├─ Put new when full (evicts LRU)
       └─ Capacity enforcement

Integration Tests:
  └─ DistributedCache
       ├─ GET: cache hit, returns immediately
       ├─ GET: cache miss, fetches from DB
       ├─ PUT: stores in correct node
       ├─ Data distribution across nodes
       ├─ LRU eviction across multiple nodes
       └─ Stats accuracy
```

## Summary

This design achieves:
✓ **Flexibility**: Pluggable strategies and policies  
✓ **Simplicity**: Clear responsibilities, minimal coupling  
✓ **Efficiency**: O(1) operations for get/put/eviction  
✓ **Extensibility**: Easy to add new strategies/policies  
✓ **Testability**: Isolated components, no hidden dependencies  
✓ **Maintainability**: Clean code, clear documentation  
