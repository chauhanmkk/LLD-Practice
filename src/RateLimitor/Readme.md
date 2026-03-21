# Rate Limiter — LLD Interview Ready Sheet
> Target: Tier-1 (Google, Microsoft, Amazon) | Pattern: Strategy + Decorator | Difficulty: Medium-Hard

---

## 1. Clarifying Questions (Ask These First)

- Per user or global rate limiting?
- What are the limits? (e.g., 100 requests/min per user)
- Single server or distributed deployment?
- What happens on breach — reject instantly or queue request?
- Multiple rules? (e.g., 100/min AND 1000/hour for same user)
- Should limits differ per API endpoint?

---

## 2. Core Entities

| Class | Key Fields | Responsibility |
|-------|-----------|---------------|
| `TokenBucket` | capacity, currentTokens, refillRate, lastRefillTime | Holds token state per user |
| `RateLimiter` | interface | Contract for all algorithms |
| `TokenBucketRateLimiter` | Map\<String, TokenBucket\> | Token Bucket implementation |
| `RateLimiterMiddleware` | RateLimiter | Intercepts requests, delegates to limiter |
| `RateLimitConfig` | capacity, refillRate | Config per user/endpoint |

### Enums
```java
enum RateLimitResult { ALLOWED, REJECTED }
```

---

## 3. Key Design Insight — Why Strategy Pattern?
> This is the #1 thing interviewers check.

Rate limiting algorithms are **swappable** — Token Bucket today,
Sliding Window tomorrow, without changing middleware or calling code.

```
RateLimiterMiddleware ──> RateLimiter (interface)
                               ├── TokenBucketRateLimiter
                               ├── SlidingWindowRateLimiter
                               └── LeakyBucketRateLimiter
```

---

## 4. Algorithm Comparison — Why Token Bucket?
> Interviewer WILL ask this. Have a crisp answer.

| Algorithm | How It Works | Pro | Con |
|-----------|-------------|-----|-----|
| **Token Bucket** | Tokens added at fixed rate. Request consumes 1 token. | Handles bursts gracefully | Slight complexity |
| **Leaky Bucket** | Requests queue and process at fixed rate | Smooth output rate | No burst tolerance |
| **Fixed Window** | Count requests per fixed window (e.g., per minute) | Simple | Boundary burst problem* |
| **Sliding Window** | Rolling time window per request | Most accurate | Higher memory cost |

**Boundary burst problem:** With fixed window, a user can fire 100 requests at 12:00:59 and 100 more at 12:01:01 — 200 requests in 2 seconds, both within "limits."

**Why Token Bucket for APIs:**
- Tolerates legitimate bursts (e.g., user opens app, fires 5 requests simultaneously)
- Smooth average rate enforcement via refill
- O(1) time and O(1) space per check
- Industry standard (AWS, Stripe, GitHub all use it)

---

## 5. Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `RateLimiter` interface | Swap algorithms without changing middleware |
| **Decorator** | `RateLimiterMiddleware` | Wraps API handler, adds rate limiting transparently |

---

## 6. Core Algorithm — Token Bucket

```java
public class TokenBucket {
    private final double capacity;
    private double currentTokens;
    private final double refillRate;       // tokens per second
    private long lastRefillTime;           // epoch millis

    public TokenBucket(double capacity, double refillRate) {
        this.capacity = capacity;
        this.currentTokens = capacity;     // start full
        this.refillRate = refillRate;
        this.lastRefillTime = System.currentTimeMillis();
    }

    // CORE ALGORITHM
    public synchronized boolean allowRequest() {
        refill();
        if (currentTokens >= 1) {
            currentTokens--;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        double elapsedSeconds = (now - lastRefillTime) / 1000.0;
        double tokensToAdd = elapsedSeconds * refillRate;
        currentTokens = Math.min(capacity, currentTokens + tokensToAdd);
        lastRefillTime = now;
    }
}
```

**Formula:**
```
currentTokens = min(capacity, currentTokens + (elapsedTime * refillRate))
```

---

## 7. RateLimiter Strategy + Middleware

```java
// Strategy interface
public interface RateLimiter {
    boolean isAllowed(String userId);
}

// Token Bucket implementation
public class TokenBucketRateLimiter implements RateLimiter {
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final RateLimitConfig config;

    public TokenBucketRateLimiter(RateLimitConfig config) {
        this.config = config;
    }

    @Override
    public boolean isAllowed(String userId) {
        TokenBucket bucket = buckets.computeIfAbsent(userId,
            k -> new TokenBucket(config.getCapacity(), config.getRefillRate()));
        return bucket.allowRequest();
    }
}

// Decorator — wraps any API handler
public class RateLimiterMiddleware {
    private final RateLimiter rateLimiter;

    public RateLimiterMiddleware(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public void handleRequest(String userId, Runnable apiHandler) {
        if (!rateLimiter.isAllowed(userId)) {
            throw new RuntimeException("429 Too Many Requests");
        }
        apiHandler.run();
    }
}
```

---

## 8. Thread Safety

| What | Solution |
|------|----------|
| `TokenBucket.allowRequest()` | `synchronized` — refill + check must be atomic |
| `Map<String, TokenBucket>` | `ConcurrentHashMap` — thread-safe bucket creation |
| Why not lock the whole map? | `computeIfAbsent` is atomic in ConcurrentHashMap — no extra lock needed |

**Critical:** `refill()` and token consumption must be **one atomic operation**.
If you separate them, two threads can both see sufficient tokens and both proceed.

---

## 9. Distributed Rate Limiting (If Asked in LLD)
> "We solved Redis in HLD" won't fly if interviewer probes LLD level.

In distributed deployment, each server has its own in-memory `TokenBucket` — a user hitting 3 servers gets 3× the limit.

**LLD-level answer:**
```java
// Replace in-memory TokenBucket with Redis atomic ops
public boolean isAllowed(String userId) {
    String key = "rate_limit:" + userId;
    // MULTI/EXEC or Lua script ensures atomicity
    Long tokens = redisClient.eval(luaScript, key, capacity, refillRate);
    return tokens != null && tokens >= 1;
}
```
- Use **Redis + Lua script** for atomic refill + consume
- Lua script runs atomically on Redis — no race condition across servers
- Key expires automatically — no manual cleanup

---

## 10. Curveballs + Answers

| Curveball | Answer |
|-----------|--------|
| Different limits per endpoint | `Map<String, RateLimitConfig>` keyed by endpoint — different bucket per (userId, endpoint) |
| 100/min AND 1000/hour same user | Chain two `TokenBucket`s — request allowed only if BOTH pass |
| Whitelist certain users | Check whitelist before hitting RateLimiter in middleware |
| Graceful degradation vs hard reject | Return `Retry-After` header with wait time = (1 - currentTokens) / refillRate |
| Memory bloat with millions of users | TTL-based eviction on inactive buckets using `ScheduledExecutorService` |

---

## 11. Mistakes to Avoid in Interview

| Mistake | Why It's Bad |
|---------|-------------|
| Jumping to algorithm without clarifying questions | Misses distributed vs single-server scope entirely |
| Refill and consume not atomic | Race condition — two threads both get allowed on last token |
| `HashMap` instead of `ConcurrentHashMap` | Not thread-safe for concurrent bucket creation |
| Hard-coded capacity/refillRate | Config should be injectable — not testable otherwise |
| No Strategy pattern | Changing algorithm requires rewriting middleware — OCP violation |

---

## 12. One-Line Pattern Justifications
> Say these out loud in the interview.

- **Strategy for RateLimiter** — *"Rate limiting algorithm can change independently of the middleware — Token Bucket today, Sliding Window tomorrow."*
- **Decorator for Middleware** — *"Rate limiting is a cross-cutting concern — it wraps the handler transparently without modifying it."*
- **ConcurrentHashMap** — *"Bucket creation must be thread-safe — computeIfAbsent is atomic, no extra locking needed."*
- **synchronized on allowRequest** — *"Refill and consume must be one atomic operation — splitting them creates a race condition on the last token."*
  - **Token Bucket over Fixed Window** — *"Fixed window has boundary burst problem — Token Bucket handles legitimate bursts while enforcing average rate."*