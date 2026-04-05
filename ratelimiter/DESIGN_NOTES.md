# Pluggable Rate Limiting System - Design Notes

## Overview

This is a production-grade rate limiting system designed specifically for controlling external resource usage in backend systems. Unlike traditional API rate limiters that protect individual endpoints, this system applies rate limits only at the point where external calls are made.

## Problem Statement

### Context
- Client calls API endpoint
- API runs business logic
- Business logic may or may not call external resource (paid)
- System is charged per external call
- Need to prevent excessive external calls while allowing legitimate requests

### Key Insight
**Not every API request should consume external resource quota.** Rate limiting is applied only when external calls are actually needed, making this different from standard API rate limiters.

## Architecture & Design Patterns

### 1. Strategy Pattern (Algorithm Pluggability)

```
┌─────────────────────────────────────┐
│   RateLimitAlgorithm (interface)   │
│  - isAllowed(key, config)          │
└──────────────┬──────────────────────┘
               │
       ┌───────┴──────────┬──────────────┬─────────────┐
       │                  │              │             │
   ┌───▼────┐    ┌────────▼───┐   ┌────▼──────┐   ┌──▼──────────┐
   │ Fixed  │    │  Sliding   │   │  Token    │   │  Leaky     │
   │ Window │    │  Window    │   │  Bucket   │   │  Bucket    │
   │        │    │            │   │           │   │            │
   └────────┘    └────────────┘   (future)    (future)
       ▲
       │ (injected into)
   ┌───────────────────────────┐
   │    RateLimiter (facade)   │
   │  - isAllowed()            │
   │  - setAlgorithm()         │
   └───────────────────────────┘
```

**Benefits:**
- Change algorithm dynamically without code changes
- Easy to test by injecting mock algorithms
- New algorithms added without modifying existing code
- Open/Closed Principle satisfied

### 2. Facade Pattern (RateLimiter)

The `RateLimiter` class simplifies client interaction:
- Clients don't need to know about different algorithms
- Simple `isAllowed()` method hides complexity
- Encapsulates algorithm management
- Single point of rate limit decision

### 3. Configuration Object Pattern (RateLimitConfig)

Instead of scattered parameters:
```java
// ❌ Hard to extend, easy to mistake parameter order
isAllowed(key, 5, 60);

// ✅ Clear, extensible, type-safe
isAllowed(key, new RateLimitConfig(5, 60));
```

### 4. Result Object Pattern (RateLimitResult)

Returns more than just boolean:
```java
RateLimitResult result = rateLimiter.isAllowed(key, config);

result.isAllowed();           // true/false
result.getRemainingRequests(); // 2 (for client-side throttling)
result.getResetTimeMs();       // 4500 (for retry-after header)
result.getTotalLimit();        // 5 (for debugging)
```

## Core Components

### 1. RateLimitAlgorithm Interface
**Responsibility:** Define contract for rate limiting strategies

```java
public interface RateLimitAlgorithm {
    RateLimitResult isAllowed(String key, RateLimitConfig config);
}
```

**Why interface?**
- Allows multiple implementations
- Strategy pattern enablement
- Easy testing with mocks

---

### 2. RateLimiter Facade
**Responsibility:** Main entry point for rate limiting

```java
public class RateLimiter {
    private RateLimitAlgorithm algorithm;
    
    public synchronized RateLimitResult isAllowed(String key, RateLimitConfig config) {
        return algorithm.isAllowed(key, config);
    }
    
    public synchronized void setAlgorithm(RateLimitAlgorithm newAlgorithm) {
        this.algorithm = newAlgorithm;
    }
}
```

**Key Design Decisions:**
- Synchronized methods ensure thread-safety
- Simple delegation to plugged algorithm
- Allows runtime algorithm switching
- Single responsibility: route to algorithm

**Thread Safety Consideration:**
- External callers see synchronized wrapper
- Individual algorithm implementations also use synchronized blocks
- Provides safety at both levels

---

### 3. FixedWindowCounter Algorithm
**Responsibility:** Count requests in fixed time windows

#### How It Works

1. **Divide time into fixed windows**
   - Window size = config.windowSeconds
   - Current window = currentTimeMs / (windowSeconds * 1000)

2. **Track counter per window**
   - Store separate counter for each (key, window) pair
   - When window changes, reset counter

3. **Decision:**
   - If counter < limit: allow & increment
   - If counter >= limit: deny

#### Visual Example
```
Time: ────────────────────────────────────────────────────────────
      Limit: 3 per 60s
      
Window 0 (0-60s):
  T=10s: Request 1 (count=1) ✓
  T=20s: Request 2 (count=2) ✓
  T=30s: Request 3 (count=3) ✓
  T=40s: Request 4 (count=3) ✗
  
Window 1 (60-120s):
  T=60s: Request 5 (count=0→1) ✓ [NEW WINDOW]
  T=70s: Request 6 (count=1→2) ✓
```

#### Characteristics

| Aspect | Value |
|--------|-------|
| **Accuracy** | Medium - allows burst at boundaries |
| **Time Complexity** | O(1) |
| **Space Complexity** | O(K) where K = number of keys |
| **Implementation** | `windowIndex = currentTimeMs / (windowSeconds * 1000)` |
| **Cleanup** | Automatic (old windows discarded) |

#### Burst Problem at Boundaries

```
Window 0: 0-60s limits to 3 requests
Window 1: 60-120s limits to 3 requests

Scenario:
- T=59.9s: 3 requests made, count=3 (next denied)
- T=60.0s: Window resets, count=0
- T=60.1s: 3 more requests squeezed in quickly

Result: 6 requests in 0.2 seconds (2x burst rate)
This is the WEAKNESS of fixed window approach.
```

**When to Use:**
- Simple systems where burst is acceptable
- Cost of over-provisioning is low
- Want maximum performance (O(1))

---

### 4. SlidingWindowCounter Algorithm
**Responsibility:** Track exact request timestamps within window

#### How It Works

1. **Store all request timestamps**
   - List<Long> per key containing request times
   - Timestamps in milliseconds since epoch

2. **Sliding window filtering**
   - Current window = [now - duration, now]
   - Remove all timestamps older than (now - duration)
   - Count remaining timestamps

3. **Decision:**
   - If remaining count < limit: allow & add timestamp
   - If remaining count >= limit: deny

#### Visual Example
```
Time: ────────────────────────────────────────────────────────────
      Limit: 3 per 60s window
      
T=0s:    [req1]      count=1 ✓
T=10s:   [req1, req2] count=2 ✓
T=20s:   [req1, req2, req3] count=3 ✓
T=30s:   [req1, req2, req3] count=3 ✗ (denied)

T=60.1s: Window is [0.1s, 60.1s]
         [req1@0s is removed, outside window]
         [req2@10s, req3@20s] count=2 ✓ (allowed!)
         
This prevents the burst! Only 2 new requests in 30 seconds
instead of 3 requests in 0.2 seconds with fixed window.
```

#### Characteristics

| Aspect | Value |
|--------|-------|
| **Accuracy** | High - exact enforcement |
| **Time Complexity** | O(n) where n = requests in window |
| **Space Complexity** | O(K×L) where K = keys, L = avg requests in window |
| **Cleanup** | Explicit on each check (removeIf) |
| **Burst Protection** | Yes - prevents boundary bursts |

**Example:**
- Limit: 1000 per minute
- With SlidingWindow: requests spread ~3.6 per second average
- With FixedWindow: could burst to ~33 per second at boundaries

**When to Use:**
- Important external resources with cost
- Want strict enforcement
- Can afford slightly higher CPU (still very fast)
- 99% of production cases

---

### 5. RateLimitConfig
**Responsibility:** Encapsulate rate limit parameters

```java
public class RateLimitConfig {
    private final int limit;           // Max requests
    private final int windowSeconds;   // Time window
}
```

**Design Benefit:**
- Avoids primitive parameter explosion
- Self-documenting
- Easy to extend (e.g., add priority, multiplier)
- Type-safe configuration

**Examples:**
```java
new RateLimitConfig(5, 60)      // 5 per minute
new RateLimitConfig(1000, 3600) // 1000 per hour
new RateLimitConfig(1, 1)       // 1 per second
```

---

### 6. RateLimitResult
**Responsibility:** Encapsulate rate limit decision + metadata

```java
public class RateLimitResult {
    boolean allowed;           // Allow or deny?
    int remainingRequests;     // For client throttling
    long resetTimeMs;          // For retry-after header
    int totalLimit;            // For logging
}
```

**Usage:**
```java
RateLimitResult result = rateLimiter.isAllowed(key, config);

if (!result.isAllowed()) {
    response.setHeader("Retry-After", result.getResetTimeMs() / 1000);
    return error(429, "Rate limited");
}
```

---

## Thread Safety Analysis

### Thread-Safety Strategy

1. **RateLimiter class**: `synchronized isAllowed()` wrapper
   - Ensures only one thread can check rate limit at a time
   - Prevents race conditions in algorithm selection

2. **FixedWindowCounter**: `synchronized` on `isAllowed()`
   - Synchronizes map access (windowCounts, keyWindows)
   - Ensures counter increment is atomic

3. **SlidingWindowCounter**: `synchronized` on `isAllowed()`
   - Synchronizes list access (keyTimestamps)
   - Ensures add/remove operations are atomic

### Concurrency Guarantees

```java
// Thread 1
RateLimitResult r1 = rateLimiter.isAllowed("customer_1", config);

// Thread 2
RateLimitResult r2 = rateLimiter.isAllowed("customer_1", config);

// Both threads will be serialized through synchronized blocks
// Counter will be updated atomically
// No race conditions on state
```

### Performance Impact

- Synchronized blocks = potential contention under high concurrency
- Trade-off: Safety vs performance
- In practice: very fast (microseconds), lock contention rare
- For extreme scale: consider lock-free (AtomicInteger) or distributed approaches

---

## SOLID Principles Compliance

### Single Responsibility Principle (SRP)
- `RateLimiter`: Manages algorithm choice and provides facade
- `FixedWindowCounter`: Implements fixed window algorithm
- `SlidingWindowCounter`: Implements sliding window algorithm
- `RateLimitConfig`: Holds configuration data
- `RateLimitResult`: Holds result data

Each class has ONE reason to change.

### Open/Closed Principle (OCP)
- **Open for extension:** New algorithms via `RateLimitAlgorithm` interface
- **Closed for modification:** Existing code doesn't change

Adding TokenBucketAlgorithm:
```java
// No changes to existing classes!
class TokenBucketAlgorithm implements RateLimitAlgorithm { ... }
rateLimiter.setAlgorithm(new TokenBucketAlgorithm());
```

### Liskov Substitution Principle (LSP)
- Any `RateLimitAlgorithm` implementation can replace another
- Contract: `isAllowed(key, config) -> RateLimitResult`
- All implementations honor this contract

```java
RateLimitAlgorithm algo1 = new FixedWindowCounter();
RateLimitAlgorithm algo2 = new SlidingWindowCounter();
RateLimiter limiter = new RateLimiter(algo1);
limiter.setAlgorithm(algo2); // Works seamlessly
```

### Interface Segregation Principle (ISP)
- Clients don't depend on algorithm-specific details
- Depend only on `RateLimitAlgorithm` interface
- No unnecessary methods exposed

### Dependency Inversion Principle (DIP)
- High-level `RateLimiter` depends on abstract `RateLimitAlgorithm`
- Not on concrete implementations
- Dependencies flow inward toward abstractions

```
Business Logic
    ↓
RateLimiter (facade)
    ↓ (depends on)
RateLimitAlgorithm (interface)
    ↑ (implemented by)
FixedWindowCounter, SlidingWindowCounter (implementations)
```

---

## Algorithm Comparison in Detail

### FixedWindowCounter

**Pros:**
- ✅ Simplest implementation
- ✅ O(1) time complexity
- ✅ Minimal memory per key
- ✅ No timestamp tracking needed
- ✅ Predictable performance

**Cons:**
- ❌ Allows burst at window boundaries
- ❌ Not precise edge cases
- ❌ Can violate "actual" per-minute limits at boundaries

**Real Example:**
```
Limit: 100 per minute
Customer makes:
- 100 requests at 59.9 seconds
- 100 requests at 60.1 seconds (new window)
Result: 200 requests in 0.2 seconds (10x burst)
```

**Best For:**
- Non-critical rate limiting
- Systems where bursts are acceptable
- Very high scale systems needing O(1) performance
- Development/testing environments

---

### SlidingWindowCounter

**Pros:**
- ✅ Accurate enforcement
- ✅ Prevents burst at window boundaries
- ✅ Fair rate limiting across time
- ✅ Smooth request distribution
- ✅ More predictable for clients

**Cons:**
- ❌ More complex implementation
- ❌ O(n) per check where n = requests in window
- ❌ More memory (stores timestamps)
- ❌ Requires cleanup to prevent growth
- ❌ Slightly higher CPU per request

**Real Example:**
```
Same limit: 100 per minute
Customer makes:
- 100 requests at 59.9 seconds
- At 60.1 seconds, 100 at 0.0 seconds expires
- Now only ~99 requests in window
- Next request allowed at 60.1 seconds
Result: Smooth enforcement, no burst
```

**Best For:**
- Critical rate limiting (billing, service protection)
- Paid external resources
- Cases where accuracy matters more than speed
- Most production scenarios

---

## Trade-offs and Decisions

### Trade-off 1: Accuracy vs Performance

| Scenario | Recommendation |
|----------|-----------------|
| Free API | FixedWindow (simpler) |
| Paid external API | SlidingWindow (accuracy) |
| High-volume (10k+/sec) | FixedWindow (performance) |
| Medium volume | SlidingWindow (best overall) |

### Trade-off 2: Memory vs Precision

**FixedWindowCounter:**
- Memory: O(K) per algorithm
- Precision: ±1 window duration

**SlidingWindowCounter:**
- Memory: O(K × L) where L = average requests per window
- Precision: ±millisecond (exact)

### Trade-off 3: Synchronized vs Lock-Free

Current approach:
```java
public synchronized RateLimitResult isAllowed(String key, ...) {
    // thread-safe
}
```

Alternative for extreme scale:
```java
AtomicInteger.incrementAndGet();  // Lock-free
```

**Current choice rationale:**
- Simple and correct
- Sufficient for 99% of workloads
- Synchronized blocks are fast
- Safer than lock-free for this use case

### Trade-off 4: Per-Window Config vs Per-Request Config

Current approach: Config passed per request
```java
rateLimiter.isAllowed(key, config1); // config per check
```

Alternative: Config registered per key
```java
rateLimiter.registerKey(key, config1);
rateLimiter.isAllowed(key);
```

**Current choice rationale:**
- Maximum flexibility
- Can have different limits for same key
- No pre-registration needed
- Simpler mental model

---

## Extension Scenarios

### Scenario 1: Adding Token Bucket Algorithm

```java
public class TokenBucketAlgorithm implements RateLimitAlgorithm {
    private Map<String, TokenBucket> buckets = new HashMap<>();
    
    @Override
    public synchronized RateLimitResult isAllowed(String key, RateLimitConfig config) {
        TokenBucket bucket = buckets.computeIfAbsent(key, 
            k -> new TokenBucket(config.getLimit(), config.getWindowSeconds()));
        
        if (bucket.tryConsume(1)) {
            return new RateLimitResult(true, bucket.getAvailable(), bucket.getRefillTimeMs(), config.getLimit());
        }
        return new RateLimitResult(false, bucket.getAvailable(), bucket.getRefillTimeMs(), config.getLimit());
    }
}

// Usage - ZERO changes to business logic!
rateLimiter.setAlgorithm(new TokenBucketAlgorithm());
```

### Scenario 2: Adding Distributed Rate Limiting

```java
public class RedisDistributedRateLimiter implements RateLimitAlgorithm {
    private RedisClient redis;
    
    @Override
    public RateLimitResult isAllowed(String key, RateLimitConfig config) {
        String redisKey = "ratelimit:" + key;
        long count = redis.incr(redisKey);
        
        if (count == 1) {
            redis.expire(redisKey, config.getWindowSeconds());
        }
        
        boolean allowed = count <= config.getLimit();
        return new RateLimitResult(allowed, Math.max(0, config.getLimit() - (int)count), ...);
    }
}
```

### Scenario 3: Adding Metrics/Monitoring

```java
public class MonitoredRateLimiter extends RateLimiter {
    private RateLimitMetrics metrics;
    
    @Override
    public RateLimitResult isAllowed(String key, RateLimitConfig config) {
        RateLimitResult result = super.isAllowed(key, config);
        
        if (result.isAllowed()) {
            metrics.incrementAllowed(key);
        } else {
            metrics.incrementDenied(key);
        }
        
        return result;
    }
}
```

---

## Testing Strategy

### Unit Tests Include:
1. ✅ Basic allow/deny functionality
2. ✅ Counter increment on allow
3. ✅ Boundary conditions (limit=1, very high limit)
4. ✅ Multiple keys independence
5. ✅ Algorithm switching
6. ✅ Window reset/slide behavior
7. ✅ Result object accuracy
8. ✅ Thread safety (concurrent requests)
9. ✅ Configuration validation
10. ✅ External service integration

### How to Test:
```bash
javac *.java
java RateLimiterTest
```

---

## Summary: Design Wins

1. **Pluggability:** Add new algorithms without touching existing code
2. **Simplicity:** Clean interface and facade hide complexity
3. **Safety:** Thread-safe by design
4. **Flexibility:** Support any rate limit key dimension
5. **Observability:** Result object provides metadata for monitoring
6. **Testability:** Interface allows mocking algorithms
7. **Performance:** O(1) option available when needed
8. **Accuracy:** O(n) option for critical resources
9. **SOLID:** Follows all five SOLID principles
10. **Production-Ready:** Handles edge cases, validation, errors

---

## Conclusion

This rate limiting system is designed for **real-world backend scenarios** where:
- External resources are paid and must be carefully controlled
- Different algorithms suit different needs
- Accuracy for some resources, performance for others
- Easy to modify without breaking business logic
- Thread-safe and production-ready

The **strategy pattern** provides extensibility, **proper SOLID design** ensures maintainability, and **two proven algorithms** cover most use cases.
