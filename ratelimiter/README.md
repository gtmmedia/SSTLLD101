# Pluggable Rate Limiting System

A production-grade, extensible rate limiting system designed for controlling external resource usage in backend services.

## Features

- **Pluggable Algorithms**: Easily swap between different rate limiting algorithms
- **Multiple Implementations**: Fixed Window Counter and Sliding Window Counter included
- **Flexible Rate Limit Keys**: Rate limit by customer, tenant, API key, provider, etc.
- **Thread-Safe**: Synchronized access ensures safe concurrent usage
- **Extensible Design**: Follow SOLID principles to plug in new algorithms (Token Bucket, Leaky Bucket, etc.)
- **Configurable Limits**: Support multiple limits at different granularities (per minute, per hour, etc.)

## Architecture

### Core Components

1. **RateLimitAlgorithm** - Interface for plugging in different algorithms
2. **RateLimiter** - Main facade that uses configured algorithm
3. **FixedWindowCounter** - Simple algorithm counting requests in fixed time windows
4. **SlidingWindowCounter** - More accurate algorithm tracking exact request times
5. **RateLimitConfig** - Configuration for limits and time windows
6. **RateLimitResult** - Response indicating allow/deny decision

## System Design

```
Client API Request
    ↓
Business Logic (determines if external call needed)
    ↓
If external call needed:
    ↓
    ┌─────────────────────────────────────┐
    │   RateLimiter.isAllowed()           │
    │   (Pluggable Algorithm)             │
    └─────────────────────────────────────┘
         /          |          \
    Fixed         Sliding      Token Bucket
    Window        Window       (extensible)
    ↓
Call External Resource OR Reject/Handle Gracefully
```

## Usage Example

```java
// Configure rate limiter
RateLimitConfig config = new RateLimitConfig(5, 60); // 5 requests per 60 seconds

// Create rate limiter with algorithm of choice
RateLimitAlgorithm algorithm = new SlidingWindowCounter();
RateLimiter rateLimiter = new RateLimiter(algorithm);

// Check before external call
String key = "customer_" + customerId; // Rate limit by customer
RateLimitResult result = rateLimiter.isAllowed(key, config);

if (result.isAllowed()) {
    callExternalAPI();
} else {
    handleRateLimitExceeded(result);
}

// Switch algorithm without changing business logic
RateLimitAlgorithm newAlgorithm = new FixedWindowCounter();
rateLimiter.setAlgorithm(newAlgorithm);
```

## Algorithm Comparison

| Aspect | Fixed Window | Sliding Window |
|--------|-------------|----------------|
| **Accuracy** | Lower - burst at window boundaries | Higher - smooth enforcement |
| **Memory** | Low - simple counter | Medium - stores timestamps |
| **Computation** | O(1) | O(n) - n = window size |
| **Use Case** | Simple, lenient limits | Strict, precise limits |
| **Burst Protection** | Allows 2x burst at boundaries | Prevents burst |

## Key Design Decisions

1. **Algorithm Pattern**: Strategy pattern allows runtime switching without code changes
2. **Thread Safety**: Synchronized blocks ensure thread-safe counter updates
3. **Key Abstraction**: String keys provide flexibility for any dimension (customer, tenant, etc.)
4. **Simple Configuration**: RateLimitConfig encapsulates limit and time window
5. **Result Object**: Returns both decision and metadata for logging/monitoring

## Trade-offs

- **Fixed Window vs Sliding Window**: Fast simple counting vs more accurate but slightly more CPU
- **Memory**: Sliding Window uses more memory to store request timestamps
- **Time Synchronization**: System assumes clock consistency; distributed systems may need careful consideration

## Extension Points

To add a new algorithm (e.g., Token Bucket):

1. Implement `RateLimitAlgorithm` interface
2. Implement `isAllowed(String key, RateLimitConfig config)` method
3. Pass instance to RateLimiter constructor
4. No business logic changes needed!

```java
public class TokenBucketAlgorithm implements RateLimitAlgorithm {
    @Override
    public RateLimitResult isAllowed(String key, RateLimitConfig config) {
        // Implementation
    }
}
```

## Files in this Solution

- `RateLimitAlgorithm.java` - Interface for algorithms
- `RateLimiter.java` - Main facade
- `RateLimitConfig.java` - Configuration class
- `RateLimitResult.java` - Result object
- `FixedWindowCounter.java` - Fixed window implementation
- `SlidingWindowCounter.java` - Sliding window implementation
- `ExternalServiceExample.java` - Usage example
- `RateLimiterTest.java` - Comprehensive tests
- `DESIGN_NOTES.md` - Detailed design documentation
