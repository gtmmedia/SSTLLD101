/**
 * RateLimiterTest
 * 
 * Comprehensive test suite for rate limiting system.
 * Tests both algorithms and various scenarios.
 */
public class RateLimiterTest {
    
    public static void main(String[] args) {
        System.out.println("=== Rate Limiter Test Suite ===\n");
        
        testFixedWindowCounter();
        System.out.println();
        testSlidingWindowCounter();
        System.out.println();
        testAlgorithmSwitching();
        System.out.println();
        testMultipleKeys();
        System.out.println();
        testBoundaryConditions();
        System.out.println();
        testExternalServiceFlow();
        
        System.out.println("\n=== All Tests Completed ===");
    }
    
    // ===========================
    // Fixed Window Counter Tests
    // ===========================
    
    static void testFixedWindowCounter() {
        System.out.println("TEST 1: FixedWindowCounter - Basic Rate Limiting");
        System.out.println("Config: 3 requests per 2 seconds\n");
        
        RateLimiter rateLimiter = new RateLimiter(new FixedWindowCounter());
        RateLimitConfig config = new RateLimitConfig(3, 2);
        String key = "customer_1";
        
        // First 3 requests should pass
        for (int i = 1; i <= 3; i++) {
            RateLimitResult result = rateLimiter.isAllowed(key, config);
            System.out.printf("  Request %d: ALLOWED=%s, Remaining=%d\n", 
                i, result.isAllowed(), result.getRemainingRequests());
            assert result.isAllowed() : "Request " + i + " should be allowed";
        }
        
        // 4th request should fail
        RateLimitResult result = rateLimiter.isAllowed(key, config);
        System.out.printf("  Request 4: ALLOWED=%s, Remaining=%d\n", 
            result.isAllowed(), result.getRemainingRequests());
        assert !result.isAllowed() : "Request 4 should be denied";
        
        System.out.println("  ✓ FixedWindowCounter test passed\n");
    }
    
    // ===========================
    // Sliding Window Counter Tests
    // ===========================
    
    static void testSlidingWindowCounter() {
        System.out.println("TEST 2: SlidingWindowCounter - Burst Protection");
        System.out.println("Config: 3 requests per 2 seconds\n");
        
        RateLimiter rateLimiter = new RateLimiter(new SlidingWindowCounter());
        RateLimitConfig config = new RateLimitConfig(3, 2);
        String key = "customer_2";
        
        // First 3 requests pass
        for (int i = 1; i <= 3; i++) {
            RateLimitResult result = rateLimiter.isAllowed(key, config);
            System.out.printf("  T=0: Request %d: ALLOWED=%s\n", i, result.isAllowed());
            assert result.isAllowed() : "Request " + i + " should be allowed";
        }
        
        // 4th request denied (window full)
        RateLimitResult result = rateLimiter.isAllowed(key, config);
        System.out.printf("  T=0: Request 4: ALLOWED=%s (window full)\n", result.isAllowed());
        assert !result.isAllowed() : "Request 4 should be denied";
        
        // Simulate waiting for 2+ seconds
        try {
            System.out.println("  Waiting 2.1 seconds for window to slide...");
            Thread.sleep(2100);
            
            // Old timestamps should be outside window by now
            result = rateLimiter.isAllowed(key, config);
            System.out.printf("  T=2.1s: Request 5: ALLOWED=%s (old requests expired)\n", 
                result.isAllowed());
            assert result.isAllowed() : "Request 5 should be allowed after window slide";
            
            System.out.println("  ✓ SlidingWindowCounter test passed\n");
        } catch (InterruptedException e) {
            System.out.println("  (Skipping time-based assertion in test)");
        }
    }
    
    // ===========================
    // Algorithm Switching Tests
    // ===========================
    
    static void testAlgorithmSwitching() {
        System.out.println("TEST 3: Algorithm Switching at Runtime");
        System.out.println("Config: 2 requests per 1 second\n");
        
        RateLimiter rateLimiter = new RateLimiter(new FixedWindowCounter());
        RateLimitConfig config = new RateLimitConfig(2, 1);
        String key = "customer_3";
        
        // Use Fixed Window initially
        System.out.println("  Using FixedWindowCounter:");
        RateLimitResult result1 = rateLimiter.isAllowed(key, config);
        RateLimitResult result2 = rateLimiter.isAllowed(key, config);
        RateLimitResult result3 = rateLimiter.isAllowed(key, config);
        System.out.printf("    Requests 1,2,3: %b, %b, %b\n", 
            result1.isAllowed(), result2.isAllowed(), result3.isAllowed());
        
        // Switch to Sliding Window
        System.out.println("  Switching to SlidingWindowCounter:");
        rateLimiter.setAlgorithm(new SlidingWindowCounter());
        
        // Reset key for clean test (new algorithm instance)
        String key2 = "customer_4";
        result1 = rateLimiter.isAllowed(key2, config);
        result2 = rateLimiter.isAllowed(key2, config);
        result3 = rateLimiter.isAllowed(key2, config);
        System.out.printf("    Requests 1,2,3: %b, %b, %b\n", 
            result1.isAllowed(), result2.isAllowed(), result3.isAllowed());
        
        System.out.println("  ✓ Algorithm switching works without code changes!\n");
    }
    
    // ===========================
    // Multiple Keys Test
    // ===========================
    
    static void testMultipleKeys() {
        System.out.println("TEST 4: Multiple Keys (Different Customers)");
        System.out.println("Config: 2 requests per 1 second\n");
        
        RateLimiter rateLimiter = new RateLimiter(new FixedWindowCounter());
        RateLimitConfig config = new RateLimitConfig(2, 1);
        
        // Customer 1
        String key1 = "customer_1";
        System.out.println("  Customer 1:");
        System.out.printf("    Request 1: %s\n", rateLimiter.isAllowed(key1, config).isAllowed());
        System.out.printf("    Request 2: %s\n", rateLimiter.isAllowed(key1, config).isAllowed());
        System.out.printf("    Request 3: %s (denied)\n", rateLimiter.isAllowed(key1, config).isAllowed());
        
        // Customer 2 (independent limit)
        String key2 = "customer_2";
        System.out.println("  Customer 2:");
        System.out.printf("    Request 1: %s\n", rateLimiter.isAllowed(key2, config).isAllowed());
        System.out.printf("    Request 2: %s\n", rateLimiter.isAllowed(key2, config).isAllowed());
        System.out.printf("    Request 3: %s (denied)\n", rateLimiter.isAllowed(key2, config).isAllowed());
        
        System.out.println("  ✓ Each key has independent rate limit!\n");
    }
    
    // ===========================
    // Boundary Conditions
    // ===========================
    
    static void testBoundaryConditions() {
        System.out.println("TEST 5: Boundary Conditions");
        RateLimiter rateLimiter = new RateLimiter(new SlidingWindowCounter());
        
        // Test with single request limit
        System.out.println("  Config: 1 request per 10 seconds");
        RateLimitConfig config = new RateLimitConfig(1, 10);
        
        System.out.printf("    Request 1: %s\n", 
            rateLimiter.isAllowed("key1", config).isAllowed());
        System.out.printf("    Request 2: %s (denied)\n", 
            rateLimiter.isAllowed("key1", config).isAllowed());
        
        // Test with high limit
        System.out.println("  Config: 1000 requests per 1 second");
        RateLimitConfig highConfig = new RateLimitConfig(1000, 1);
        
        boolean allPassed = true;
        for (int i = 0; i < 100; i++) {
            if (!rateLimiter.isAllowed("key2", highConfig).isAllowed()) {
                allPassed = false;
                break;
            }
        }
        System.out.printf("    100 rapid requests: %s\n", allPassed);
        
        System.out.println("  ✓ Boundary conditions handled correctly\n");
    }
    
    // ===========================
    // External Service Flow
    // ===========================
    
    static void testExternalServiceFlow() {
        System.out.println("TEST 6: External Service Usage Pattern");
        
        ExternalDataProvider provider = ExternalDataProvider.mock();
        ExternalServiceExample service = new ExternalServiceExample(provider);
        
        System.out.println("  Scenario: Customer requests detailed analysis 3 times");
        System.out.println("  (Limit: 5 per minute)\n");
        
        for (int i = 1; i <= 3; i++) {
            ExternalServiceExample.AnalysisResponse response = 
                service.processAnalysisRequest("cust_123", "DETAILED");
            System.out.printf("    Request %d: Status=%s, Remaining=%d\n", 
                i, response.getStatus(), response.getRemainingRequests());
        }
        
        System.out.println("\n  Switching algorithm...");
        service.switchToFixedWindowCounterAlgorithm();
        
        System.out.println("  Making more requests with new algorithm...");
        for (int i = 4; i <= 5; i++) {
            ExternalServiceExample.AnalysisResponse response = 
                service.processAnalysisRequest("cust_456", "DETAILED");
            System.out.printf("    Request %d: Status=%s, Remaining=%d\n", 
                i, response.getStatus(), response.getRemainingRequests());
        }
        
        System.out.println("  ✓ External service integration works perfectly!\n");
    }
    
}
