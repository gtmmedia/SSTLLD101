# Distributed Cache System - LLD Design

## Overview

A distributed cache system that supports `get(key)` and `put(key, value)` operations across multiple configurable cache nodes. The design emphasizes flexibility and extensibility through pluggable strategies and policies.

## Class Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    DistributedCache                          │
├─────────────────────────────────────────────────────────────┤
│ - nodes: CacheNode[]                                        │
│ - distributionStrategy: DistributionStrategy                │
│ - database: Database                                        │
│ - nodeCount: int                                            │
├─────────────────────────────────────────────────────────────┤
│ + get(key): Object                                          │
│ + put(key, value): void                                     │
│ + getStats(): String                                        │
└─────────────────────────────────────────────────────────────┘
         │
         ├──> [CacheNode] (array)
         │     ├─ storage: Map<String, Object>
         │     ├─ capacity: int
         │     ├─ evictionPolicy: EvictionPolicy
         │     ├─ get(key)
         │     └─ put(key, value)
         │
         ├──> [DistributionStrategy] (interface)
         │     └─ implementations:
         │        └─ ModuloDistributionStrategy
         │
         ├──> [EvictionPolicy] (interface)
         │     └─ implementations:
         │        └─ LRUEvictionPolicy
         │
         └──> [Database] (interface)
               └─ implementations:
                  └─ FakeDatabase
```

## Architecture & Design Patterns

### 1. **Strategy Pattern**

#### Distribution Strategy
- **Interface**: `DistributionStrategy`
- **Purpose**: Determines which cache node should store a given key
- **Current Implementation**: `ModuloDistributionStrategy`
  - Uses `hash(key) % numberOfNodes`
  - Provides uniform distribution
  - O(1) operation
- **Future Extensions**:
  - Consistent Hashing (for better node scalability)
  - Ring-based routing
  - Weighted distribution

#### Eviction Policy
- **Interface**: `EvictionPolicy`
- **Purpose**: Decides which cache entry to remove when capacity is reached
- **Current Implementation**: `LRUEvictionPolicy`
  - Uses LinkedHashMap in access-order mode
  - Tracks most recently/least recently used entries
  - O(1) access and eviction
- **Future Extensions**:
  - MRU (Most Recently Used)
  - LFU (Least Frequently Used)
  - Time-based expiration (TTL)

### 2. **Adapter Pattern**
- `Database` interface abstracts external data source
- `FakeDatabase` provides test implementation
- Easy to swap with real database implementations

## Key Features & Workflows

### Feature 1: Configurable Cache Nodes
```
new DistributedCache(
    nodeCount: 3,              // Number of cache nodes
    nodeCapacity: 100,         // Max entries per node
    strategy: ModuloDistributionStrategy(),
    evictionPolicy: LRUEvictionPolicy(),
    database: realDatabase
);
```

### Feature 2: GET Operation Workflow

```
get(key) called
    ↓
Validate key
    ↓
Determine node using DistributionStrategy (O(1))
    ↓
Check if key exists in node
    ├─ YES (Cache Hit)
    │   ├─ Update access time in eviction policy
    │   └─ Return value
    │
    └─ NO (Cache Miss)
        ├─ Fetch from Database
        ├─ Store in cache node
        │   └─ If node at capacity: evict LRU entry
        └─ Return value
```

### Feature 3: PUT Operation Workflow

```
put(key, value) called
    ↓
Validate key and value
    ↓
Determine node using DistributionStrategy (O(1))
    ↓
Store in node
    ├─ If key exists: update value
    ├─ If at capacity: evict LRU entry
    └─ Add new entry
    ↓
Update Database
```

### Feature 4: LRU Eviction Workflow

```
When putting new key and node is at capacity:
    ↓
Get least recently used key from eviction policy
    ↓
Remove from storage
    ↓
Remove from eviction policy tracking
    ↓
Add new entry
```

## Data Distribution

**Modulo-Based Distribution Strategy**:
- Uses `Math.abs(key.hashCode()) % numberOfNodes`
- Maps each key to a specific node deterministically
- Same key always goes to same node
- Ensures data consistency and locality

**Example with 3 nodes**:
```
key: "user:1"  → hash: 456789 → 456789 % 3 = 0 → Node 0
key: "user:2"  → hash: 456790 → 456790 % 3 = 1 → Node 1
key: "user:3"  → hash: 456791 → 456791 % 3 = 2 → Node 2
key: "cache:1" → hash: 987654 → 987654 % 3 = 0 → Node 0
```

## Cache Miss Handling

**Problem**: Cache doesn't have requested data
**Solution**:
1. Query database: `database.getValue(key)`
2. If found:
   - Store in appropriate cache node
   - If node is full: evict LRU entry
   - Return the value
3. If not found:
   - Throw RuntimeException (key doesn't exist)

**Assumption**: Database is the source of truth

## Eviction (LRU)

**When**: Cache node reaches capacity and new entry needs to be added

**How**:
1. LinkedHashMap in access-order mode tracks all accesses
2. First entry in iteration order = least recently used
3. Remove LRU entry from storage
4. Remove LRU entry from eviction policy tracking
5. Add new entry

**Time Complexity**: 
- Recording access: O(1)
- Getting key to evict: O(1)
- Removing key: O(1)

## Extensibility Points

### Adding New Distribution Strategy

```java
public class ConsistentHashStrategy implements DistributionStrategy {
    @Override
    public int getNodeIndex(String key, int numberOfNodes) {
        // Implement consistent hash algorithm
        // Better behavior when nodes are added/removed
    }
}

// Use it:
DistributedCache cache = new DistributedCache(
    10, 100,
    new ConsistentHashStrategy(),  // Plug in new strategy
    new LRUEvictionPolicy(),
    database
);
```

### Adding New Eviction Policy

```java
public class LFUEvictionPolicy implements EvictionPolicy {
    private Map<String, Integer> frequency = new HashMap<>();
    
    @Override
    public void recordAccess(String key) {
        frequency.put(key, frequency.getOrDefault(key, 0) + 1);
    }
    
    @Override
    public String getKeyToEvict() {
        // Return key with minimum frequency
    }
    
    @Override
    public void removeKey(String key) {
        frequency.remove(key);
    }
}

// Use it:
DistributedCache cache = new DistributedCache(
    10, 100,
    new ModuloDistributionStrategy(),
    new LFUEvictionPolicy(),  // Plug in new policy
    database
);
```

## Time & Space Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| get(key) | O(1) | O(1) | Hash lookup in node |
| put(key, value) | O(1) | O(1) | Hash insert + eviction |
| Distribution routing | O(1) | O(1) | Modulo operation |
| Eviction selection | O(1) | O(1) | LinkedHashMap tracking |
| Overall storage | O(n) | O(n*c) | n = nodes, c = capacity per node |

## Assumptions

1. **Keys are unique** within the entire cache system
2. **Database interface** is already available
3. **No network communication** between nodes (in-memory LLD)
4. **Thread-safety** not implemented (for basic LLD)
5. **Database is source of truth** - always consistent with cache
6. **Values are non-null** - null represents "not found"

## Testing

Run the Main class to see demonstrations of:
1. Basic GET and PUT operations
2. Cache miss handling (database fetch)
3. Data distribution across nodes
4. LRU eviction in action
5. Cache statistics

```bash
javac *.java
java Main
```

## Future Enhancements

1. **Thread-safe implementation** using ReentrantReadWriteLock
2. **TTL (Time-To-Live)** for automatic expiration
3. **Consistent Hashing** for better scalability
4. **Cache warming** and preloading
5. **Persistence** to disk
6. **Network communication** between distributed cache servers
7. **Cache statistics & metrics** (hit rate, eviction count, etc.)
8. **Compression** for large values
9. **Cache invalidation** strategies
10. **Monitoring and logging** infrastructure
