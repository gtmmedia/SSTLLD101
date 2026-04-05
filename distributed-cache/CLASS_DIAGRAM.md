# Distributed Cache - Class Diagram (Text Format)

## Complete System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DISTRIBUTED CACHE SYSTEM                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │ DistributedCache                                                      │   │
│  ├──────────────────────────────────────────────────────────────────────┤   │
│  │ FIELDS:                                                              │   │
│  │  - nodes: CacheNode[]                                               │   │
│  │  - distributionStrategy: DistributionStrategy                       │   │
│  │  - database: Database                                               │   │
│  │  - nodeCount: int                                                   │   │
│  ├──────────────────────────────────────────────────────────────────────┤   │
│  │ METHODS:                                                             │   │
│  │  + DistributedCache(nodeCount, nodeCapacity,                        │   │
│  │                     strategy, policy, database)                     │   │
│  │  + get(key): Object              // Cache hit or fetch from DB      │   │
│  │  + put(key, value): void         // Store in cache + update DB      │   │
│  │  + getStats(): String            // Display cache statistics        │   │
│  │  - validateKey(key): void        // Helper: validate input          │   │
│  │  - createEvictionPolicyCopy(...) // Helper: create policy instance  │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│         │                │                    │                             │
│         │ uses (array)   │ uses               │ uses                        │
│         │                │                    │                             │
│         │                │                    │                             │
│  ┌──────▼────────┐   ┌───▼──────────────────────┐   ┌────────────────────┐ │
│  │  CacheNode    │   │ DistributionStrategy(I)  │   │ EvictionPolicy(I)  │ │
│  ├───────────────┤   ├──────────────────────────┤   ├────────────────────┤ │
│  │ FIELDS:       │   │ METHODS:                 │   │ METHODS:           │ │
│  │ - capacity    │   │ + getNodeIndex(          │   │ + recordAccess()   │ │
│  │ - storage     │   │     key, numNodes)       │   │ + getKeyToEvict()  │ │
│  │ - eviction... │   │   → int (0 to numNodes)  │   │ + removeKey()      │ │
│  │   Policy      │   └──────────────────────────┘   └────────────────────┘ │
│  ├───────────────┤          ▲                              ▲                │
│  │ METHODS:      │          │                              │                │
│  │ + get(key)    │          │ implements            implements             │
│  │ + put(key,    │          │                              │                │
│  │   value)      │          │                              │                │
│  │ + containsKey │   ┌──────┴──────────────────┐  ┌────────┴──────────────┐ │
│  │ + size()      │   │ModuloDistribution      │  │ LRUEvictionPolicy     │ │
│  │ + getCapacity │   │Strategy                │  ├───────────────────────┤ │
│  └───────────────┘   ├────────────────────────┤  │ FIELDS:               │ │
│         │            │ + getNodeIndex(...)    │  │ - accessOrder:        │ │
│         │ uses       │   return Math.abs      │  │   LinkedHashMap       │ │
│         │            │   (key.hashCode()) %   │  ├───────────────────────┤ │
│         │            │   numberOfNodes        │  │ + recordAccess(key)   │ │
│         │            └────────────────────────┘  │ + getKeyToEvict()     │ │
│         │                                       │ + removeKey()         │ │
│  ┌──────▼────────────────────────────────────┐ └───────────────────────┘ │
│  │ EVICTION POLICY (dependency)               │                            │
│  │                                            │                            │
│  │ When node.put() called + node full:       │                            │
│  │ 1. Get LRU key: policy.getKeyToEvict()    │                            │
│  │ 2. Remove from storage                     │                            │
│  │ 3. Remove from policy: policy.removeKey()  │                            │
│  │ 4. Add new entry                           │                            │
│  └────────────────────────────────────────────┘                            │
│                                                                               │
│         │ uses                                                              │
│         │                                                                   │
│  ┌──────▼──────────────────────────┐                                       │
│  │ Database (Interface)             │                                       │
│  ├──────────────────────────────────┤                                       │
│  │ + getValue(key): Object          │                                       │
│  │ + setValue(key, value): void     │                                       │
│  └──────────────────────────────────┘                                       │
│         ▲                                                                    │
│         │ implements                                                         │
│         │                                                                    │
│  ┌──────┴──────────────────────────┐                                       │
│  │ FakeDatabase                     │                                       │
│  ├──────────────────────────────────┤                                       │
│  │ FIELDS:                          │                                       │
│  │  - data: Map<String, Object>     │                                       │
│  ├──────────────────────────────────┤                                       │
│  │ + getValue(key): Object          │                                       │
│  │ + setValue(key, value): void     │                                       │
│  │ + init(key, value): void         │                                       │
│  │ + getAllData(): Map              │                                       │
│  └──────────────────────────────────┘                                       │
│                                                                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Component Interaction Sequence

### GET Operation
```
┌─────────────┐         ┌──────────────────┐      ┌───────────┐      ┌─────────┐
│   Client    │         │DistributedCache  │      │ CacheNode │      │Database │
└──────┬──────┘         └────────┬─────────┘      └─────┬─────┘      └────┬────┘
       │ get("user:123")         │                       │                │
       ├───────────────────────>│                       │                │
       │                        │ validate key          │                │
       │                        │ strategy.getNode... →│                │
       │                        │ check storage         │                │
       │                        ├──────────────────────>│ (cache hit)    │
       │                        │                       │                │
       │                        │           value ◄─────┤                │
       │                        │ update access time    │                │
       │                        │                       │                │
       │           value ◄──────┤                       │                │
       │◄───────────────────────┤                       │                │
       │                        │                       │                │
       │                        │ (cache miss scenario) │                │
       │                        ├──────────────────────>│ (not found)    │
       │                        │                       │                │
       │                        │ fetch from DB         │                │
       │                        ├───────────────────────────────────────>│
       │                        │                       │    value ◄────┤
       │                        │◄───────────────────────────────────────┤
       │                        │ store in cache        │                │
       │                        ├──────────────────────>│                │
       │                        │ record access         │                │
       │                        │                       │                │
       │           value ◄──────┤                       │                │
       │◄───────────────────────┤                       │                │
       │                        │                       │                │
```

### PUT Operation
```
┌─────────────┐         ┌──────────────────┐      ┌───────────┐      ┌─────────┐
│   Client    │         │DistributedCache  │      │ CacheNode │      │Database │
└──────┬──────┘         └────────┬─────────┘      └─────┬─────┘      └────┬────┘
       │ put("user:999",         │                       │                │
       │     "New Value")        │                       │                │
       ├───────────────────────>│                       │                │
       │                        │ validate inputs       │                │
       │                        │ strategy.getNode... →│                │
       │                        │ check if key exists   │                │
       │                        │ check if full         │                │
       │                        ├──────────────────────>│                │
       │                        │              (space available or       │
       │                        │              eviction needed)          │
       │                        │ (if full) evict LRU   │                │
       │                        │ store new entry       │                │
       │                        ├──────────────────────>│                │
       │                        │ record access         │                │
       │                        │                       │                │
       │                        │ update database       │                │
       │                        ├──────────────────────────────────────>│
       │                        │                       │          done  │
       │                        │◄────────────────────────────────────┤
       │           done ◄───────┤                       │                │
       │◄───────────────────────┤                       │                │
       │                        │                       │                │
```

## Data Storage Structure

```
DistributedCache
├─ nodes[0]
│  └─ storage: HashMap<String, Object>
│     ├─ "user:1" → User{name: "John"}
│     ├─ "user:4" → User{name: "Alice"}
│     └─ "config:1" → "value1"
│
├─ nodes[1]
│  └─ storage: HashMap<String, Object>
│     ├─ "user:2" → User{name: "Jane"}
│     ├─ "user:5" → User{name: "Bob"}
│     └─ "session:456" → "active"
│
├─ nodes[2]
│  └─ storage: HashMap<String, Object>
│     ├─ "user:3" → User{name: "Charlie"}
│     └─ "cache:99" → "data99"
│
...more nodes...
```

## LRU Tracking Structure (LinkedHashMap)

```
For Node[0]:

LRUEvictionPolicy.accessOrder (LinkedHashMap in access-order mode)
┌──────────────────────────────────────────────────┐
│ [1] "user:1"  ← Oldest access (will evict first) │
│ [2] "user:4"                                     │
│ [3] "config:1" ← Most recent access              │
└──────────────────────────────────────────────────┘

If new put() called on full node:
  1. getKeyToEvict() → returns "user:1" (first in order)
  2. Remove from storage
  3. removeKey("user:1") from tracking
  4. Add new entry at end (most recent)
```

## Module Dependencies

```
Main.java
  ├── uses DistributedCache
  ├── uses FakeDatabase
  ├── uses ModuloDistributionStrategy
  ├── uses LRUEvictionPolicy
  └── imports Database, DistributionStrategy, EvictionPolicy

DistributedCache.java
  ├── uses CacheNode
  ├── uses DistributionStrategy (interface)
  ├── uses EvictionPolicy (interface)
  ├── uses Database (interface)
  └── creates LRUEvictionPolicy in constructor

CacheNode.java
  ├── uses EvictionPolicy (interface)
  └── uses HashMap for storage

ModuloDistributionStrategy.java
  └── implements DistributionStrategy

LRUEvictionPolicy.java
  ├── implements EvictionPolicy
  ├── uses LinkedHashMap
  └── tracks access patterns

FakeDatabase.java
  ├── implements Database
  └── uses HashMap for test data

Database.java
  └── interface definition

DistributionStrategy.java
  └── interface definition

EvictionPolicy.java
  └── interface definition
```

## Key Design Points

### 1. Modulo Distribution
```
Key → String.hashCode() → Math.abs() → % numberOfNodes → node index

Examples:
  "user:1".hashCode() = 836031422
  Math.abs(836031422) = 836031422
  836031422 % 3 = 2 → Node 2

  "user:2".hashCode() = 836031421
  Math.abs(836031421) = 836031421
  836031421 % 3 = 1 → Node 1
```

### 2. LRU Tracking
```
Access sequence:
  recordAccess("user:1")   →  [user:1]
  recordAccess("user:2")   →  [user:1, user:2]
  recordAccess("config:1") →  [user:1, user:2, config:1]
  recordAccess("user:1")   →  [user:2, config:1, user:1]  (moved to end)

When eviction needed:
  getKeyToEvict() → "user:2" (first in LinkedHashMap)
```

### 3. Node Isolation
```
Each node has:
  ✓ Independent storage (separate HashMap)
  ✓ Independent eviction policy (separate LRUEvictionPolicy instance)
  ✓ Independent capacity tracking

Benefits:
  ✓ Prevents cross-node interference
  ✓ Allows different node configurations (future)
  ✓ Scales well to distributed scenario
```

### 4. Interface Separation
```
Interfaces: Database, DistributionStrategy, EvictionPolicy
Benefits:
  ✓ Easy to mock for testing
  ✓ Easy to extend with new implementations
  ✓ No tight coupling
  ✓ Follow Dependency Inversion Principle
```

## Extensibility Hooks

```
┌─────────────────────────────────────────────────┐
│ Easy to extend:                                 │
├─────────────────────────────────────────────────┤
│ 1. New DistributionStrategy                    │
│    └─ Implement getNodeIndex()                 │
│       ├─ ConsistentHashStrategy                │
│       ├─ WeightedDistributionStrategy          │
│       └─ RingBasedStrategy                     │
│                                                 │
│ 2. New EvictionPolicy                          │
│    └─ Implement all three methods              │
│       ├─ LFUEvictionPolicy                     │
│       ├─ MRUEvictionPolicy                     │
│       ├─ TTLEvictionPolicy                     │
│       └─ RandomEvictionPolicy                  │
│                                                 │
│ 3. New Database Implementation                 │
│    └─ Implement getValue/setValue              │
│       ├─ MySQLDatabase                         │
│       ├─ MongoDatabase                         │
│       ├─ RedisDatabase                         │
│       └─ FileSystemDatabase                    │
│                                                 │
│ 4. New CacheNode variants                      │
│    └─ Extend CacheNode                         │
│       ├─ CompressedCacheNode (compress values)│
│       ├─ ThreadSafeCacheNode (add locks)       │
│       └─ MonitoredCacheNode (track metrics)    │
└─────────────────────────────────────────────────┘
```

---

**Class diagram generated**: ✅  
**Component interactions**: ✅  
**Data structures visualized**: ✅  
**Design patterns clear**: ✅  
