package RateLimitor;

public class TokenBucket {
    private final double capacity;
    private long lastRefillTime; //in milliseconds
    private double currentToken;
    private final double refillRate; // in seconds

    public TokenBucket(double capacity, double refillRate) {
        this.capacity = capacity;
        this.lastRefillTime = System.currentTimeMillis();
        this.currentToken = capacity;
        this.refillRate = refillRate;
    }

    public TokenBucket() {
        this(10, 10.0 / 60);
    }

    synchronized boolean allowRequest() {
        refill();
        if (currentToken >= 1) {
            currentToken--;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        double elapsedSeconds = (now - lastRefillTime) / 1000.0;
        currentToken = Math.min(capacity, currentToken + elapsedSeconds * refillRate);
        lastRefillTime = now;
    }
}
