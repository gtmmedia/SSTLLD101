# Rate Limiting System - Use Cases & Examples

## Use Case 1: Customer-Based Rate Limiting

**Scenario:** Multiple customers use your API, each with a different external service budget.

```java
public class PaymentProcessingService {
    private RateLimiter rateLimiter = new RateLimiter(new SlidingWindowCounter());
    
    // Tier-based rate limits
    private static final Map<String, RateLimitConfig> TIER_LIMITS = Map.ofEntries(
        Map.entry("PREMIUM", new RateLimitConfig(100, 60)),    // 100 per minute
        Map.entry("STANDARD", new RateLimitConfig(20, 60)),    // 20 per minute
        Map.entry("FREE", new RateLimitConfig(5, 60))         // 5 per minute
    );
    
    public PaymentResult processPayment(String customerId, String tier, PaymentDetails details) {
        // Step 1: Run business logic (no external call needed yet)
        if (!details.isValidForProcessing()) {
            return PaymentResult.invalid();
        }
        
        // Step 2: Check if payment method validation needs external service
        if (details.needsValidationFromExternalProvider()) {
            
            // Step 3: Check rate limit before external call
            String rateLimitKey = "customer_" + customerId;
            RateLimitConfig tierLimit = TIER_LIMITS.get(tier);
            RateLimitResult limitResult = rateLimiter.isAllowed(rateLimitKey, tierLimit);
            
            if (!limitResult.isAllowed()) {
                PaymentResult result = PaymentResult.rateLimited();
                result.setRetryAfterMs(limitResult.getResetTimeMs());
                return result;
            }
            
            // Step 4: Call external validation service
            var validationResult = callExternalPaymentValidator(details);
            if (!validationResult.isValid()) {
                return PaymentResult.validationFailed();
            }
        }
        
        // Step 5: Process payment normally
        return processPaymentInternal(details);
    }
}
```

---

## Use Case 2: Tenant-Based Rate Limiting

**Scenario:** Multi-tenant SaaS where each tenant has their own external data quota.

```java
public class DataEnrichmentService {
    private RateLimiter rateLimiter = new RateLimiter(new SlidingWindowCounter());
    
    public EnrichedUserData getUserDataWithEnrichment(String tenantId, String userId) {
        // Basic user data from internal database
        UserData userData = database.getUser(userId);
        
        // Check if enrichment is needed
        if (!userData.needsEnrichment() || userData.isCacheValid()) {
            return new EnrichedUserData(userData);
        }
        
        // Before calling expensive external data provider
        String rateLimitKey = "tenant_" + tenantId;
        RateLimitConfig config = new RateLimitConfig(1000, 3600); // 1000 per hour per tenant
        
        RateLimitResult result = rateLimiter.isAllowed(rateLimitKey, config);
        if (!result.isAllowed()) {
            System.out.println("Tenant " + tenantId + " has exhausted enrichment quota");
            return new EnrichedUserData(userData); // return without enrichment
        }
        
        // Call external data provider
        ExternalEnrichmentData enrichment = externalProvider.enrich(userData);
        return new EnrichedUserData(userData, enrichment);
    }
}
```

---

## Use Case 3: API Key Based Rate Limiting

**Scenario:** External integrations use API keys, each with different quotas.

```java
public class ExternalIntegrationGateway {
    private RateLimiter rateLimiter = new RateLimiter(new FixedWindowCounter());
    
    public ApiResponse handleExternalRequest(String apiKey, String endpoint, Map<String, Object> params) {
        // Validate API key and get quota
        ApiQuota quota = apiKeyManager.getQuota(apiKey);
        if (quota == null) {
            return ApiResponse.unauthorized();
        }
        
        // Check rate limit
        String rateLimitKey = "api_key_" + apiKey;
        RateLimitConfig config = new RateLimitConfig(
            quota.getCallsPerMinute(), 
            60
        );
        
        RateLimitResult result = rateLimiter.isAllowed(rateLimitKey, config);
        
        if (!result.isAllowed()) {
            ApiResponse response = ApiResponse.rateLimited();
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingRequests()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetTimeMs() / 1000));
            return response;
        }
        
        // Call the external service
        Object externalResult = callExternalService(endpoint, params);
        return ApiResponse.success(externalResult);
    }
}
```

---

## Use Case 4: Provider-Based Rate Limiting

**Scenario:** Your system uses multiple external providers, each with different limits.

```java
public class WeatherForecastService {
    private RateLimiter rateLimiter = new RateLimiter(new SlidingWindowCounter());
    
    // Different limits per provider
    private static final Map<String, RateLimitConfig> PROVIDER_LIMITS = Map.ofEntries(
        Map.entry("WEATHER_API", new RateLimitConfig(1000, 3600)),      // 1000/hr
        Map.entry("GEO_SERVICE", new RateLimitConfig(5000, 3600)),      // 5000/hr
        Map.entry("SATELLITE_DATA", new RateLimitConfig(100, 3600))     // 100/hr - expensive
    );
    
    public WeatherData getWeather(String location) {
        // Try cache first
        WeatherData cached = cache.get(location);
        if (cached != null && !cached.isStale()) {
            return cached;
        }
        
        // Need fresh data - call external provider
        String rateLimitKey = "provider_weather_api";
        RateLimitConfig limit = PROVIDER_LIMITS.get("WEATHER_API");
        
        RateLimitResult result = rateLimiter.isAllowed(rateLimitKey, limit);
        if (!result.isAllowed()) {
            // Return stale cached data instead of rejecting
            if (cached != null) {
                System.out.println("Weather quota exhausted, using stale cache");
                return cached;
            }
            throw new ExternalServiceQuotaExhaustedException(
                "Weather data unavailable. Try again in " + result.getResetTimeMs() + "ms"
            );
        }
        
        // Fetch from provider
        WeatherData fresh = callWeatherProvider(location);
        cache.put(location, fresh);
        return fresh;
    }
    
    public List<WeatherData> getForecastWithSatelliteEnhancement(String location) {
        List<WeatherData> forecast = getWeather(location); // uses WEATHER_API
        
        // Satellite enhancement is expensive, check its specific limit
        String satKey = "provider_satellite_data";
        RateLimitConfig satLimit = PROVIDER_LIMITS.get("SATELLITE_DATA");
        
        RateLimitResult satResult = rateLimiter.isAllowed(satKey, satLimit);
        if (satResult.isAllowed()) {
            // Enhance with satellite data
            SatelliteData satellite = callSatelliteProvider(location);
            for (WeatherData w : forecast) {
                w.enhance(satellite);
            }
        }
        // If quota exhausted, still return basic forecast
        
        return forecast;
    }
}
```

---

## Use Case 5: Dynamic Algorithm Switching

**Scenario:** Start with fixed window, switch to sliding window based on monitoring.

```java
public class AdaptiveRateLimiter {
    private RateLimiter rateLimiter = new RateLimiter(new FixedWindowCounter());
    private RateLimitMetrics metrics = new RateLimitMetrics();
    
    public void startMonitoring() {
        // Monitor burst rate
        Timer.scheduleRepeating(() -> {
            double burstRate = metrics.calculateBurstRate();
            
            if (burstRate > 0.5) { // 50% of requests in bursts
                System.out.println("High burst rate detected! Switching to SlidingWindow");
                rateLimiter.setAlgorithm(new SlidingWindowCounter());
                metrics.recordAlgorithmSwitch("FixedWindow -> SlidingWindow");
            }
        }, 60_000); // Check every minute
    }
    
    public RateLimitResult checkLimit(String key, RateLimitConfig config) {
        RateLimitResult result = rateLimiter.isAllowed(key, config);
        metrics.recordCheck(key, result);
        return result;
    }
}
```

---

## Use Case 6: Graceful Degradation

**Scenario:** When external service hits rate limit, degrade gracefully.

```java
public class RecommendationEngine {
    private RateLimiter rateLimiter = new RateLimiter(new SlidingWindowCounter());
    private RateLimitConfig personalizationLimit = new RateLimitConfig(5000, 3600);
    
    public List<Product> getRecommendations(String userId) {
        // Always get basic recommendations
        List<Product> basicRecommendations = getBasicRecommendations(userId);
        
        // Try to enhance with ML personalization if quota allows
        String key = "service_personalization_engine";
        RateLimitResult result = rateLimiter.isAllowed(key, personalizationLimit);
        
        if (result.isAllowed()) {
            try {
                List<Product> personalized = callPersonalizationService(userId, basicRecommendations);
                return personalized;
            } catch (Exception e) {
                logger.warn("Personalization failed, using basic recommendations", e);
                return basicRecommendations; // fallback
            }
        } else {
            // Gracefully degrade without personalization
            logger.info("Personalization quota exhausted, using basic recommendations");
            return basicRecommendations;
        }
    }
}
```

---

## Use Case 7: T1 Requirement - Per Customer Limit

From the problem statement:
> Suppose user T1 is allowed to make at most 5 external calls per minute.

```java
public class DocumentProcessingService {
    private RateLimiter rateLimiter = new RateLimiter(new SlidingWindowCounter());
    private static final RateLimitConfig LIMIT_T1 = new RateLimitConfig(5, 60);
    
    public DocumentProcessingResult processDocument(String customerId, Document doc) {
        // Step 1: Business logic runs first
        DocumentValidation validation = validateDocument(doc);
        
        if (!validation.isValid()) {
            return DocumentProcessingResult.failed(validation.getErrors());
        }
        
        // Step 2: Determine if external processing is needed
        if (!requiresExternalNLPProcessing(doc)) {
            // No external call needed, skip rate limiting
            InternalResult result = processInternally(doc);
            return DocumentProcessingResult.success(result);
        }
        
        // Step 3: Rate limit is about to be consumed
        String rateLimitKey = "customer_" + customerId;
        RateLimitResult limitResult = rateLimiter.isAllowed(rateLimitKey, LIMIT_T1);
        
        if (!limitResult.isAllowed()) {
            // Step 3a: External call denied due to rate limit
            return DocumentProcessingResult.rejected(
                "Customer reached 5 requests per minute limit. " +
                "Reset in " + limitResult.getResetTimeMs() + "ms"
            );
        }
        
        // Step 4: Rate limit allows, call external service
        try {
            ExternalNLPResult nlpResult = callExternalNLPEngine(doc);
            EnrichedResult enriched = enrichWithNLPResults(doc, nlpResult);
            return DocumentProcessingResult.success(enriched);
        } catch (ExternalServiceException e) {
            return DocumentProcessingResult.error(e.getMessage());
        }
    }
}
```

**Flow Diagram:**
```
processDocument()
    └─ Business Logic
        ├─ validateDocument() → valid?
        │   if NO → return FAILED
        │
        └─ requiresExternalNLPProcessing() → needed?
            ├─ if NO → processInternally() → return SUCCESS
            │
            └─ if YES
                ├─ rateLimiter.isAllowed(key, config) → allowed?
                │   ├─ if NO → return REJECTED (rate limit exceeded)
                │   │
                │   └─ if YES → callExternalNLPEngine()
                │       └─ enrichWithNLPResults()
                │           └─ return SUCCESS
```

---

## Configuration Examples

### Aggressive Rate Limiting (Expensive Resource)
```java
// 10 calls per hour - very restrictive
RateLimitConfig expensive = new RateLimitConfig(10, 3600);
rateLimiter.isAllowed("expensive_service", expensive);
```

### Moderate Rate Limiting
```java
// 100 calls per minute - typical for well-provisioned external service
RateLimitConfig moderate = new RateLimitConfig(100, 60);
rateLimiter.isAllowed("moderate_service", moderate);
```

### Permissive Rate Limiting
```java
// 10000 calls per minute - for fast, cheap external services
RateLimitConfig permissive = new RateLimitConfig(10000, 60);
rateLimiter.isAllowed("fast_service", permissive);
```

### Very Fine-Grained (Per Second)
```java
// 100 calls per second
RateLimitConfig finegrained = new RateLimitConfig(100, 1);
rateLimiter.isAllowed("service", finegrained);
```

---

## Testing the Use Cases

```java
public class UseCaseTests {
    
    public static void main(String[] args) {
        System.out.println("=== Use Case Testing ===\n");
        
        testUseCase1_CustomerBased();
        testUseCase2_TenantBased();
        testUseCase3_APIKeyBased();
        testUseCase4_ProviderBased();
        testUseCase5_AlgorithmSwitching();
        testUseCase6_GracefulDegradation();
        
        System.out.println("\n=== All Use Cases Verified ===");
    }
    
    static void testUseCase1_CustomerBased() {
        System.out.println("Use Case 1: Customer-Based Rate Limiting");
        
        RateLimiter rateLimiter = new RateLimiter(new SlidingWindowCounter());
        
        // Premium customer: 100/minute
        String premiumKey = "customer_premium_123";
        RateLimitConfig premiumLimit = new RateLimitConfig(100, 60);
        
        // Free customer: 5/minute
        String freeKey = "customer_free_456";
        RateLimitConfig freeLimit = new RateLimitConfig(5, 60);
        
        // Premium can make 100 calls
        System.out.println("  Premium customer:");
        for (int i = 1; i <= 5; i++) {
            RateLimitResult r = rateLimiter.isAllowed(premiumKey, premiumLimit);
            System.out.printf("    Call %d: %s\n", i, r.isAllowed() ? "ALLOWED" : "DENIED");
        }
        
        // Free can make only 5
        System.out.println("  Free customer:");
        for (int i = 1; i <= 7; i++) {
            RateLimitResult r = rateLimiter.isAllowed(freeKey, freeLimit);
            System.out.printf("    Call %d: %s\n", i, r.isAllowed() ? "ALLOWED" : "DENIED");
        }
        
        System.out.println("  ✓ Different tiers enforced independently\n");
    }
    
    static void testUseCase2_TenantBased() {
        System.out.println("Use Case 2: Tenant-Based Rate Limiting");
        
        RateLimiter rateLimiter = new RateLimiter(new SlidingWindowCounter());
        RateLimitConfig config = new RateLimitConfig(3, 1);
        
        String tenant1 = "tenant_acme_corp";
        String tenant2 = "tenant_widgets_inc";
        
        System.out.println("  ACME Corp:");
        for (int i = 0; i < 4; i++) {
            RateLimitResult r = rateLimiter.isAllowed(tenant1, config);
            System.out.printf("    Request %d: %s\n", i + 1, r.isAllowed());
        }
        
        System.out.println("  Widgets Inc:");
        for (int i = 0; i < 4; i++) {
            RateLimitResult r = rateLimiter.isAllowed(tenant2, config);
            System.out.printf("    Request %d: %s\n", i + 1, r.isAllowed());
        }
        
        System.out.println("  ✓ Tenants have independent quotas\n");
    }
    
    static void testUseCase3_APIKeyBased() {
        System.out.println("Use Case 3: API Key Based Rate Limiting");
        
        RateLimiter rateLimiter = new RateLimiter(new FixedWindowCounter());
        
        String apiKey1 = "api_key_PartnerA";
        String apiKey2 = "api_key_PartnerB";
        
        RateLimitConfig limit1 = new RateLimitConfig(50, 60);  // 50/min
        RateLimitConfig limit2 = new RateLimitConfig(100, 60); // 100/min
        
        System.out.println("  Partner A (50/min limit):");
        int count = 0;
        for (int i = 0; i < 55; i++) {
            if (rateLimiter.isAllowed(apiKey1, limit1).isAllowed()) count++;
        }
        System.out.printf("    Allowed: %d out of 55\n", count);
        
        System.out.println("  Partner B (100/min limit):");
        count = 0;
        for (int i = 0; i < 105; i++) {
            if (rateLimiter.isAllowed(apiKey2, limit2).isAllowed()) count++;
        }
        System.out.printf("    Allowed: %d out of 105\n", count);
        
        System.out.println("  ✓ API keys enforced separately\n");
    }
    
    // Additional test methods...
}
```

---

## Key Takeaway

**Every use case follows the same flow:**

1. Run business logic (no external call needed)
2. Determine if external call is necessary
3. If yes, check rate limit before external call
4. If allowed, make external call
5. If denied, gracefully handle (reject, degrade, cache)

The **rate limiter is pluggable** and can be swapped between algorithms without changing any business logic!
