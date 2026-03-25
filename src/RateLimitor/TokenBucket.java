package RateLimitor;

public class TokenBucket {
    double capacity;
    long lastRefillTime;
    double currentToken;
    double refillRate;

    public TokenBucket(double capacity, long lastRefillTime, double currentToken, long refillRate) {
        this.capacity = capacity;
        this.lastRefillTime = lastRefillTime;
        this.currentToken = currentToken;
        this.refillRate = refillRate;
    }
    public TokenBucket() {
        this.capacity = 10;
        this.lastRefillTime = System.currentTimeMillis();
        this.currentToken = 10;
        this.refillRate = (double) 1 /6;  //10 token per min default
    }

    void refill() {
        long now = System.currentTimeMillis();
        long elapsedTime = (now - lastRefillTime)/1000;
        this.currentToken = Math.min(this.capacity, elapsedTime*refillRate + currentToken);
        this.lastRefillTime = now;
    }

}
