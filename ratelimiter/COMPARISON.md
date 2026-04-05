# Algorithm Comparison & Trade-offs

## Quick Reference Table

| Feature | Fixed Window | Sliding Window |
|---------|-------------|----------------|
| **Implementation** | Counter per window | List of timestamps |
| **Time Complexity** | O(1) | O(n)* |
| **Space Complexity** | O(K)** | O(K×L)** |
| **Accuracy** | Medium | High |
| **Burst at Boundary** | Yes (2x possible) | No |
| **Cleanup Needed** | Automatic | Explicit (removeIf) |
| **Best For** | Development, Testing | Production, Critical Resources |
| **When 99%ile latency matters** | Better | Slightly worse |
| **When accuracy matters** | Worse | Better |

*n = number of requests in current window
**K = number of distinct keys, L = average requests per window

---

## Detailed Comparison

### FixedWindowCounter Deep Dive

#### How it Works
```
Time:    0s        60s       120s      180s
         │         │         │         │
Window:  [◄═════0══►│◄═════1══►│◄═════2══►│
         
Limit: 5 per 60s

Requests:
T=10s: count[key:0] = 1 ✓
T=20s: count[key:0] = 2 ✓
T=30s: count[key:0] = 3 ✓
T=40s: count[key:0] = 4 ✓
T=50s: count[key:0] = 5 ✓
T=55s: count[key:0] = 5 ✗ (denied, window full)
T=60s: count[key:1] = 0 (window resets!)
T=60.1s: count[key:1] = 1 ✓ (NEW WINDOW)
```

#### Burst Pattern
```
Limit: 5 per 60s
Time:   50s  55s  60s  65s  70s
        │    │    │    │    │
Req:    ●●●●●    ●●●●●
        └─ 5 in window 0 ─┘
        
At 59.9s: Window 0 full, request denied
At 60.0s: Window 1 starts, counter resets to 0
At 60.1s: Window 1 fresh, request allowed

Result: 5 requests at 59.9s + 5 requests at 60.1s = 10 in 0.2s
This is a BURST - 2x the normal rate!
```

#### Advantages
✅ O(1) lookup and update  
✅ Minimal memory (just an integer per key per algorithm instance)  
✅ No cleanup needed (old windows naturally discarded)  
✅ Very predictable performance  
✅ Suitable for CPU-constrained systems  

#### Disadvantages
❌ Allows burst at window boundaries  
❌ Can violate "per-minute" limits in practice  
❌ Less fair for continuous workloads  
❌ Not suitable for critical resources  

#### When to Use
- **Development/testing** environments
- **Non-critical** rate limiting (where burst is acceptable)
- **Very high scale** (millions of requests/sec) where O(1) is critical
- **Cost-insensitive** scenarios
- When you need **maximum performance**

#### Real-World Example
```
Charging API with Fixed Window, Limit: 100/min

Hour: 10:00 - 10:01              10:01 - 10:02
      ●●●●●●●●●●●●●...●●●●● (100)  ●●●●●●●●●●●●●...●●●●● (100)
      ▲                      ▲▲▲▲▲▲▲▲▲▲ BURST!
      │                      │
      └─ 10:00:59            └─ 10:01:00
      
Reality:
- First 100 requests: spread over 60 seconds (avg 1.67/sec)
- Burst at boundary: potentially 20+ requests/sec
- Inconsistent experience for users
- Bills might be unexpectedly high due to burst concentration
```

---

### SlidingWindowCounter Deep Dive

#### How it Works
```
Window Duration: 60s going forward from current time

Current Time: T=70s
Window Range: [70-60, 70] = [10s, 70s]

All requests in this window:
T=10s: req1 ✓ (IN window)
T=15s: req2 ✓ (IN window)
T=20s: req3 ✓ (IN window)
T=25s: req4 ✓ (IN window)
T=30s: req5 ✓ (IN window)
T=40s: req6 ✗ (IN window but count=5, denied)

Timestamps stored: [10, 15, 20, 25, 30]
Count: 5 (== limit, next denied)

---

Now at T=71s:
Window Range: [11s, 71s]

Check: is T=10s still in window?
10s < 11s? YES → Remove it
Timestamps: [15, 20, 25, 30]
Count: 4 (< 5, next ALLOWED!)

T=71s: req7 ✓ (now allowed because old one aged out)
Timestamps: [15, 20, 25, 30, 71]
```

#### Accuracy Example
```
Limit: 3 per 10s
Fixed Window Risk: At T=9.9s and T=10.0s boundary
- T=9.9s: 3 requests (count=3) ✗
- T=10.0s: Window resets, 3 more allowed (count=3) ✓
- Result: 6 requests in 0.1s

Sliding Window Protection: At T=9.9s
- Window: [0s, 10s]
- Requests: [0.1s, 5.0s, 9.9s] in window = 3
- T=9.9s: Rejected (window full)

At T=10.0s:
- Window: [0s, 10s] (still the same 10s from 0-10s)
- Requests: [0.1s, 5.0s, 9.9s] all still in window
- T=10.0s: Still rejected

At T=10.1s:
- Window: [0.1s, 10.1s]
- Requests: [0.1s, 5.0s, 9.9s] all still in window
- T=10.1s: Still rejected

At T=10.11s:
- Window: [0.11s, 10.11s]
- [0.1s] is now outside, removed!
- Requests: [5.0s, 9.9s] in window
- T=10.11s: Allowed

Result: No burst! Smooth enforcement.
```

#### Advantages
✅ Accurate enforcement (prevents boundary burst)  
✅ Fair for continuous workloads  
✅ True per-minute/per-hour limits  
✅ Better for charged/critical resources  
✅ Smooth request distribution  

#### Disadvantages
❌ O(n) per check - linear time  
❌ Uses more memory (stores timestamps)  
❌ Requires cleanup (removeIf operation)  
❌ Slightly higher latency per request  

#### When to Use
- **Production systems** (99% of cases)
- **Critical resources** that are charged
- **Expensive external APIs** (accuracy important)
- When **fairness** matters more than microseconds
- Systems where you need **true enforcement**

#### Real-World Example
```
Charging API with Sliding Window, Limit: 100/min

T=59.9s: 100 requests queued (all fit in [0s, 60s])
         Request 101 arrives → DENIED (window full)
         
T=60.0s: All 100 timestamps [0s-59.9s] still in window [0s, 60s]
         Request 101 → DENIED
         
T=60.1s: Window is now [0.1s, 60.1s]
         Timestamp at [0s] falls out (0s < 0.1s)
         Now 99 requests in window
         Request 101 → ALLOWED

Result: Request 101 arrives ~0.2s after window started
        = Gradual, fair request distribution
        = No burst charges
        = Predictable billing
```

---

## Visual Comparison: Request Patterns

### Pattern 1: Bursty Traffic (Worst Case for Fixed Window)

```
Fixed Window (Limit: 5 per 60s):
Requests:  ●●●●●      ●●●●●      ●●●●●
Time range: [0-60s)   [60-120s)  [120-180s)
            ▲ Allowed  ▲ Allowed   ▲ Allowed
            
At boundary: Can get 10 requests in 1 second (burst)

Sliding Window (Limit: 5 per 60s):
Requests:  ●●●●●      ●●●●●      ●●●●●
           [0-60s]────[1-61s]────[2-62s]
                ▼ Smooth enforcement
           
Consistently enforces 5 per 60s window
No burst opportunity
```

### Pattern 2: Continuous Traffic (Both work similarly)

```
Fixed Window & Sliding Window both enforce ~5/60s average
Requests: ●●●●●●●●●●●●●●●●●●●●●●●...
          Consistent enforcement for both
```

### Pattern 3: Low Traffic (Both work identically)

```
Fixed Window & Sliding Window:
Requests: ●   ●    ●
          Both allow all requests
          (well under 5/60s limit)
```

---

## Performance Analysis

### Benchmark Scenario
- 10,000 unique rate limit keys
- 100,000 requests per second
- Limit: 10,000 per minute per key

#### FixedWindowCounter
```
Per-request overhead:
- HashMap lookup: ~50ns
- Window index calculation: ~10ns
- Counter increment: ~10ns
- Total: ~70ns per request

Memory: ~10,000 keys × 2 integers = ~80KB
```

#### SlidingWindowCounter
```
Per-request overhead (best case - many requests):
- HashMap lookup: ~50ns
- removeIf cleanup (amortized): ~500ns*
- ArrayList add: ~20ns
- Total: ~570ns per request (8x slower)

*Depends on how many old timestamps to remove

Memory: ~10,000 keys × 100 timestamps × 8bytes = ~8MB
(100x more memory)
```

#### CPU Trade-off
- For 100k req/sec:
  - Fixed Window: 70ns × 100k = 7ms CPU per second
  - Sliding Window: 570ns × 100k = 57ms CPU per second

**Verdict:** Sliding Window uses ~8-10x more CPU, but:
- Still runs in nanoseconds per request
- 57ms/sec = fully acceptable
- Accuracy worth the cost for critical resources

---

## Decision Matrix

**Choose FixedWindowCounter if:**
- ✅ Development/testing environment
- ✅ Throughput > accuracy (e.g., metrics collection)
- ✅ Cost of burst is negligible
- ✅ Resource is non-critical
- ✅ Extreme scale (millions req/sec)

**Choose SlidingWindowCounter if:**
- ✅ Production system
- ✅ Accuracy > throughput
- ✅ Charged external resource
- ✅ Rate limit MUST be strictly enforced
- ✅ Fairness is important (normal case)

---

## Transition Strategy

### Start with Fixed Window (Development)
```java
// Quick to develop and test
RateLimiter limiter = new RateLimiter(new FixedWindowCounter());
```

### Promote to Sliding Window (Staging)
```java
// Before production, switch for accuracy testing
limiter.setAlgorithm(new SlidingWindowCounter());
```

### Monitor and Optimize
```java
// Collect metrics
- Track burst patterns
- Monitor false rejects
- Measure CPU impact

// If burst is problematic, stay on Sliding Window
// If burst is acceptable, stay on Fixed Window (faster)
```

### No Code Changes Needed!
The beautiful part: switching algorithms requires **zero changes to business logic**.

---

## Future Extensions

### Token Bucket Algorithm
**Why:** Allows controlled burst while maintaining average rate
```
- Tokens refill at steady rate (e.g., 5 per 60s)
- Each request consumes 1 token
- Can consume 0 tokens up to limit if tokens available
- Allows burst up to token bucket capacity
```

### Leaky Bucket Algorithm
**Why:** Mathematical certainty about maximum request rate
```
- Requests leak out at fixed rate
- All requests go into bucket first
- If bucket full, reject
- Smooth output rate guaranteed
```

### Redis-backed Distributed Rate Limiting
**Why:** Share rate limits across multiple servers
```
- Store counters in Redis
- All servers update same state
- Works for horizontally scaled systems
- Can be per-server or per-global
```

---

## Summary Table

| Aspect | Fixed Window | Sliding Window |
|--------|-------------|----------------|
| **Learning Curve** | Easy | Medium |
| **Implementation** | 30 lines | 40 lines |
| **Test Coverage** | 5 test cases | 8 test cases |
| **Recommend For New Developers** | Yes - learn strategy pattern | After understanding Fixed |
| **Recommend For Production** | Only non-critical | Yes, default choice |
| **CPU Per Request** | 70ns | 570ns |
| **Memory Per Key** | 8 bytes | 800 bytes (varies) |
| **Boundary Behavior** | Bursty | Smooth |
| **Accuracy Guarantee** | ±60s | ±millisecond |

---

## Going Deeper: Why Sliding Window is "Correct"

The fundamental issue with Fixed Window is the **window boundary problem**:

```
Real-world limit: "5 requests per minute"

This should mean: "At any point in time, look back 60 seconds.
                  Only 5 requests allowed in that window."

Fixed Window fails:
- Allows 5 in [0-60s) and 5 in [60-120s)
- But this violates the "any point in time" definition
- At 60.0s, you see [0-120s) with 10 requests
- Violation!

Sliding Window succeeds:
- At any time T, look at [T-60, T]
- Always 5 or fewer requests
- Mathematically sound
- True enforcement
```

This is why **SlidingWindow is the academically correct solution** and **FixedWindow is the pragmatic shortcut**.

For production systems managing costs: **use SlidingWindow**.
For development/testing: **use FixedWindow**.
