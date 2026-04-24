package RateLimitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimitor implements RateLimitorStrategy {
    private final Map<Client, TokenBucket> buckets;
    private final double capacity;
    private final double refillRate;

    TokenBucketRateLimitor() {
        this(10, 10.0 / 60);
    }

    TokenBucketRateLimitor(double capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.buckets = new ConcurrentHashMap<>();
    }

    @Override
    public boolean allowRequest(Client client) {
        TokenBucket bucket = buckets.computeIfAbsent(client, ignored -> new TokenBucket(capacity, refillRate));
        return bucket.allowRequest();
    }

    void addClient(Client client, TokenBucket bucket) {
        buckets.put(client, bucket);
    }
}
