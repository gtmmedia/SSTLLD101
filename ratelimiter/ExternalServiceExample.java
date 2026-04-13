/**
 * ExternalServiceExample
 * 
 * Demonstrates how to use the rate limiter in a real backend system
 * where external API calls are made.
 * 
 * Scenario: A customer analysis service that calls an external data provider (charged per call)
 */
public class ExternalServiceExample {
    
    private final RateLimiter rateLimiter;
    private final ExternalDataProvider externalProvider;
    
    // Rate limit configuration: 5 calls per minute per customer
    private final RateLimitConfig customerLimitPerMinute = new RateLimitConfig(5, 60);
    /**
     * Initialize with a specific algorithm (SlidingWindow is more accurate)
     */
    ExternalServiceExample(ExternalDataProvider provider) {
        this.externalProvider = provider;
        this.rateLimiter = new RateLimiter(new SlidingWindowCounter());
    }
    
    /**
     * Process a customer analysis request.
     * 
     * Flow:
     * 1. Run business logic
     * 2. Determine if external API call is needed
     * 3. If yes, check rate limit
     * 4. If allowed, call external API; otherwise reject gracefully
     */
    public AnalysisResponse processAnalysisRequest(String customerId, String analysisType) {
        // Step 1: Business logic (simplified)
        boolean needsExternalData = analysisType.equals("DETAILED") || analysisType.equals("COMPREHENSIVE");
        
        if (!needsExternalData) {
            // No external call needed, return cached/basic response
            return new AnalysisResponse(AnalysisResponse.Status.SUCCESS, "Cached analysis");
        }
        
        // Step 2: External call is needed, check rate limit
        String rateLimitKey = "customer_" + customerId;
        RateLimitResult result = rateLimiter.isAllowed(rateLimitKey, customerLimitPerMinute);
        
        if (!result.isAllowed()) {
            // Rate limit exceeded
            String message = String.format(
                "Rate limit exceeded for customer %s. Limit: %d per minute. Reset in %dms",
                customerId, result.getTotalLimit(), result.getResetTimeMs()
            );
            return new AnalysisResponse(AnalysisResponse.Status.RATE_LIMITED, message);
        }
        
        // Step 3: Rate limit check passed, call external API
        try {
            String data = externalProvider.fetchData(customerId, analysisType);
            String analysis = analyzeData(data);
            
            return new AnalysisResponse(
                AnalysisResponse.Status.SUCCESS,
                analysis,
                result.getRemainingRequests()
            );
        } catch (Exception e) {
            return new AnalysisResponse(AnalysisResponse.Status.ERROR, e.getMessage());
        }
    }
    
    /**
     * Demonstrates switching algorithms at runtime without affecting business logic.
     * 
     * Example: After observing burst issues, switch from Fixed Window to Sliding Window
     */
    public void switchToFixedWindowCounterAlgorithm() {
        System.out.println("Switching rate limiter algorithm to FixedWindowCounter");
        rateLimiter.setAlgorithm(new FixedWindowCounter());
    }
    
    public void switchToSlidingWindowCounterAlgorithm() {
        System.out.println("Switching rate limiter algorithm to SlidingWindowCounter");
        rateLimiter.setAlgorithm(new SlidingWindowCounter());
    }
    
    /**
     * When ready to extend with Token Bucket algorithm:
     * 1. Create TokenBucketAlgorithm class implementing RateLimitAlgorithm
     * 2. Call: rateLimiter.setAlgorithm(new TokenBucketAlgorithm());
     * 3. Zero changes to business logic needed!
     */
    
    private String analyzeData(String data) {
        return "Analysis result: " + data.substring(0, Math.min(20, data.length())) + "...";
    }
    
    /**
     * Response object for analysis requests
     */
    public static class AnalysisResponse {
        public enum Status { SUCCESS, RATE_LIMITED, ERROR }
        
        private final Status status;
        private final String message;
        private final int remainingRequests;
        
        public AnalysisResponse(Status status, String message) {
            this(status, message, -1);
        }
        
        public AnalysisResponse(Status status, String message, int remainingRequests) {
            this.status = status;
            this.message = message;
            this.remainingRequests = remainingRequests;
        }
        
        public Status getStatus() { return status; }
        public String getMessage() { return message; }
        public int getRemainingRequests() { return remainingRequests; }
        
        @Override
        public String toString() {
            String remaining = remainingRequests >= 0 
                ? ", remaining=" + remainingRequests 
                : "";
            return String.format("AnalysisResponse{status=%s, message='%s'%s}", 
                status, message, remaining);
        }
    }
}

/**
 * Mock external data provider for demonstration
 */
interface ExternalDataProvider {
    String fetchData(String customerId, String analysisType) throws Exception;
    
    static ExternalDataProvider mock() {
        return (customerId, type) -> 
            "Sample data for customer " + customerId + " (" + type + ")";
    }
}
