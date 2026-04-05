# Distributed Cache System - Complete Reference

## Project Summary

A production-grade **Distributed Cache System** implementation demonstrating clean object-oriented design, SOLID principles, and extensibility. The system manages distributed caching across multiple configurable nodes with pluggable distribution strategies and eviction policies.

## What Was Implemented

### Core Components ✓

1. **DistributedCache** - Main orchestrator class
   - Routes requests to appropriate cache nodes
   - Handles cache misses by fetching from database
   - Provides system statistics

2. **CacheNode** - Individual cache node
   - Stores key-value pairs with capacity limits
   - Enforces eviction when full
   - Coordinates with eviction policy

3. **DistributionStrategy Interface** + ModuloDistributionStrategy
   - Maps keys to nodes using hash(key) % numberOfNodes
   - Ensures deterministic routing
   - Enables future extensibility (consistent hashing, etc.)

4. **EvictionPolicy Interface** + LRUEvictionPolicy
   - Least Recently Used eviction
   - Uses LinkedHashMap for O(1) operations
   - Tracks access patterns

5. **Database Interface** + FakeDatabase
   - Abstracts external data source
   - Mock implementation for testing
   - Easy to replace with real database

6. **Main.java** - Comprehensive demonstrations
   - Basic operations (get/put)
   - Cache miss handling
   - Distribution verification
   - LRU eviction in action
   - Cache statistics

### Documentation ✓

1. **README.md** - Architecture overview with:
   - Class diagram
   - Design patterns used
   - Feature workflows
   - Extensibility points
   - Time/space complexity analysis

2. **DESIGN_NOTES.md** - Deep design decisions with:
   - Rationale for each design choice
   - Data flow diagrams
   - Class responsibilities
   - Scaling considerations
   - Thread safety analysis
   - Potential issues & solutions

3. **IMPLEMENTATION_GUIDE.md** - Practical guide with:
   - Quick start examples
   - Complete API reference
   - Common usage patterns
   - Extension examples
   - Performance tips
   - Testing guide
   - Troubleshooting

## Key Features

### Feature 1: Flexible Distribution Strategy
```java
// Current: Simple modulo-based
cache = new DistributedCache(..., new ModuloDistributionStrategy(), ...)

// Future: Just plug in new strategy
cache = new DistributedCache(..., new ConsistentHashStrategy(), ...)

// Same code, different behavior!
```

### Feature 2: Pluggable Eviction Policy
```java
// Current: LRU
DistributedCache cache = new DistributedCache(..., new LRUEvictionPolicy(), ...)

// Future: Just implement new policy
DistributedCache cache = new DistributedCache(..., new LFUEvictionPolicy(), ...)
```

### Feature 3: Cache Miss Handling
```
Cache miss → Fetch from database → Store in cache → Return value
All seamlessly integrated in get() operation
```

### Feature 4: Node-Based Distribution
```
Key "user:123" → Node routing → Stored in Node 2
Same key always goes to same node
Data locality & consistency guaranteed
```

### Feature 5: LRU Eviction
```
Node full + New entry added → Evict least recently used entry
LinkedHashMap provides O(1) operations
Efficient memory management
```

## Architecture Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                  Client Application                          │
└────────────────┬─────────────────────────────────────────────┘
                 │
                 │ cache.get(key)
                 │ cache.put(key, value)
                 │
         ┌───────▼────────────────────────────────────┐
         │     DistributedCache                       │
         │ ─────────────────────────────────────────  │
         │ • Validates requests                       │
         │ • Routes using distribution strategy       │
         │ • Handles cache misses                     │
         │ • Provides statistics                      │
         └────┬────────────────────────────────┬──────┘
              │                                │
         ┌────▼─────┐  ┌────────┐  ┌─────────▼────┐
         │ Node[0]  │  │ Node[1]│  │   Node[N]    │
         ├──────────┤  ├────────┤  ├──────────────┤
         │Storage   │  │Storage │  │ Storage      │
         │───────── │  │─────── │  │ ──────────   │
         │k1: v1    │  │k2: v2  │  │ k3: v3       │
         │k4: v4    │  │        │  │ k5: v5       │
         │          │  │        │  │ k6: v6       │
         ├──────────┤  ├────────┤  ├──────────────┤
         │LRU Policy│  │LRU     │  │ LRU Policy   │
         └──────────┘  └────────┘  └──────────────┘
              │            │              │
              └────────────┼──────────────┘
                          │
              ┌───────────▼───────────┐
              │ DistributionStrategy  │
              │ hash(key) % numNodes  │
              └───────────────────────┘
                          │
              ┌───────────▼───────────┐
              │    Database (DB)      │
              │ (Source of truth)     │
              └───────────────────────┘
```

## Implementation Statistics

| Component | Type | Purpose |
|-----------|------|---------|
| DistributedCache | Class | Main orchestrator |
| CacheNode | Class | Individual node |
| DistributionStrategy | Interface | Pluggable routing |
| ModuloDistributionStrategy | Class | Hash-based distribution |
| EvictionPolicy | Interface | Pluggable eviction |
| LRUEvictionPolicy | Class | LRU implementation |
| Database | Interface | Data source abstraction |
| FakeDatabase | Class | Mock database |
| Main | Class | Examples & tests |
| README.md | Doc | Architecture |
| DESIGN_NOTES.md | Doc | Design decisions |
| IMPLEMENTATION_GUIDE.md | Doc | Usage guide |

## Time Complexity

| Operation | Complexity | Notes |
|-----------|-----------|-------|
| get(key) | O(1) | Hash lookup + access update |
| put(key, value) | O(1) | Hash insert + potential eviction |
| getNodeIndex() | O(1) | Modulo operation |
| recordAccess() | O(1) | LinkedHashMap.put() |
| getKeyToEvict() | O(1) | LinkedHashMap.iterator().next() |

## Space Complexity

```
Total Space = Number_of_Nodes × Capacity_per_Node × Average_Value_Size

Example:
  10 nodes × 100 capacity × 1KB average = 1MB cache
  10 nodes × 1000 capacity × 1KB average = 10MB cache
```

## Design Patterns Used

1. **Strategy Pattern** - DistributionStrategy, EvictionPolicy
2. **Adapter Pattern** - Database interface for different backends
3. **Composition Pattern** - DistributedCache contains CacheNodes
4. **Factory Pattern** - createEvictionPolicyCopy() method

## SOLID Principles Applied

- **S**ingle Responsibility: Each class has one reason to change
- **O**pen/Closed: Open for extension (new strategies), closed for modification
- **L**iskov Substitution: Strategies are interchangeable
- **I**nterface Segregation: Focused interfaces (EvictionPolicy, DistributionStrategy)
- **D**ependency Inversion: Depend on abstractions, not concrete implementations

## Testing Performed

```
✓ Code compiles without errors
✓ All 5 test scenarios in Main.java pass:
  ✓ Basic GET and PUT operations
  ✓ Cache miss handling (database fetch)
  ✓ Distribution across nodes (verified routing)
  ✓ LRU eviction (demonstrated capacity management)
  ✓ Cache statistics (accurate node occupancy reporting)
```

## How It Works - Sequence Diagram

### GET Operation
```
User: cache.get("user:123")
  │
  └─> DistributedCache.get()
      ├─> Validate key
      ├─> Route: strategy.getNodeIndex("user:123", 3) → Node 2
      ├─> Check: node[2].get("user:123")
      ├─> If found:
      │   ├─> policy.recordAccess("user:123")
      │   └─> Return value (CACHE HIT) ✓
      └─> If not found:
          ├─> database.getValue("user:123")
          ├─> node[2].put("user:123", value)
          ├─> policy.recordAccess("user:123")
          └─> Return value (CACHE MISS, then HIT) ✓
```

### PUT Operation
```
User: cache.put("session:456", "active")
  │
  └─> DistributedCache.put()
      ├─> Validate key and value
      ├─> Route: strategy.getNodeIndex("session:456", 3) → Node 1
      ├─> Check: node[1].size() < capacity?
      ├─> Yes: node[1].put("session:456", "active")
      ├─> No: Eviction needed
      │   ├─> keyToEvict = policy.getKeyToEvict()
      │   ├─> node[1].remove(keyToEvict)
      │   ├─> policy.removeKey(keyToEvict)
      │   └─> node[1].put("session:456", "active")
      ├─> policy.recordAccess("session:456")
      └─> database.setValue("session:456", "active")
```

## File Listing

```
distributed-cache/
├── Database.java                    (interface, 12 lines)
├── DistributionStrategy.java        (interface, 14 lines)
├── ModuloDistributionStrategy.java  (implementation, 14 lines)
├── EvictionPolicy.java              (interface, 23 lines)
├── LRUEvictionPolicy.java           (implementation, 35 lines)
├── CacheNode.java                   (class, 84 lines)
├── DistributedCache.java            (main orchestrator, 160 lines)
├── FakeDatabase.java                (mock database, 30 lines)
├── Main.java                        (examples, 150+ lines)
├── README.md                        (architecture, ~250 lines)
├── DESIGN_NOTES.md                  (design decisions, ~400 lines)
├── IMPLEMENTATION_GUIDE.md          (usage guide, ~450 lines)
└── SUMMARY.md                       (this file)

Total: ~1850 lines of code + documentation
```

## Quick Usage Examples

### Example 1: Basic Caching
```java
FakeDatabase db = new FakeDatabase();
db.init("api:key", "api:secret");

DistributedCache cache = new DistributedCache(
    5, 100,
    new ModuloDistributionStrategy(),
    new LRUEvictionPolicy(),
    db
);

// First access: fetches from database
String secret = (String) cache.get("api:key");  // Cache MISS

// Second access: returns from cache
String secret2 = (String) cache.get("api:key"); // Cache HIT
```

### Example 2: Writing Data
```java
cache.put("user:1001", new User("John", "john@example.com"));
cache.put("user:1002", new User("Jane", "jane@example.com"));

// Both data and cache updated
```

### Example 3: Switching Strategies (Future)
```java
// Just implement the interface
public class MyCustomStrategy implements DistributionStrategy {
    @Override
    public int getNodeIndex(String key, int numberOfNodes) {
        // Custom routing logic
    }
}

// Drop-in replacement
DistributedCache cache = new DistributedCache(
    5, 100,
    new MyCustomStrategy(),  // ← Current strategy
    new LRUEvictionPolicy(),
    db
);
```

## Real-World Use Cases

1. **Web Session Cache**: Store active user sessions
2. **API Response Cache**: Cache third-party API responses
3. **Database Query Cache**: Cache frequently accessed database records
4. **Computation Cache**: Store expensive calculation results
5. **Static Asset Cache**: Cache static content like images, CSS
6. **Rate Limit Storage**: Track API rate limits per user
7. **User Preference Cache**: Store user preferences and settings
8. **Configuration Cache**: Store application configuration

## How to Extend

### Add Consistent Hashing
1. Create `ConsistentHashStrategy implements DistributionStrategy`
2. Implement hash ring logic
3. Pass to DistributedCache constructor
Done! ✓

### Add LFU Eviction
1. Create `LFUEvictionPolicy implements EvictionPolicy`
2. Track access frequency
3. Return lowest frequency key in getKeyToEvict()
4. Pass to DistributedCache constructor
Done! ✓

### Add Real Database
1. Create `RealDatabase implements Database`
2. Implement JDBC/SQL queries
3. Pass to DistributedCache constructor
Done! ✓

## Known Limitations (By Design)

1. **Not thread-safe** - Single-threaded LLD
2. **No TTL** - Items stay until evicted by LRU
3. **No persistence** - In-memory only
4. **No network** - Not distributed over network
5. **Modulo distribution** - Not optimal for dynamic node addition

All limitations can be addressed with future enhancements that extend the current design.

## Conclusion

This implementation demonstrates **professional-grade systems design** with:
- ✓ Clear separation of concerns
- ✓ Extensible architecture
- ✓ Pluggable strategies
- ✓ Clean code with good documentation
- ✓ Efficient O(1) operations
- ✓ Practical, working examples
- ✓ Future-proof design

The code is production-ready for learning purposes and can serve as a foundation for building real distributed caching systems.

---

**Status**: ✅ Complete and tested
**Files**: 12 files (8 Java + 4 Documentation)
**Total Lines**: ~1850 (code + docs)
**Compilation**: ✅ Passes
**Execution**: ✅ All tests pass
