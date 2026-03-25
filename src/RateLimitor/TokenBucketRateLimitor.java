package RateLimitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimitor implements RateLimitorStrategy {

    //client id -> TokenBucket
    Map<Integer, TokenBucket> map;

    TokenBucketRateLimitor() {
        this.map = new ConcurrentHashMap<>();
    }

    // we can use reentrant lock as well at bucket level
    // introduce lock at bucket entity and acquire lock there and then refill and consume atomically
    @Override
    public boolean allowRequest(Client client) {
        map.putIfAbsent(client.clientId, new TokenBucket());
        TokenBucket bucket = map.get(client.clientId);
        synchronized (bucket) {
            bucket.refill();
            if(bucket.currentToken >= 1) {
                bucket.currentToken--;
                System.out.println("Ok");
                return true;
            }
            System.out.println("Too many requests");
            return false;
        }
    }

    void addClient(Client client, TokenBucket bucket) {
        // for custom client vs token mapping
    }
}
