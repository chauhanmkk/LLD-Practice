package RateLimitor;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        RateLimitorStrategy tokenBucketLimiter = new TokenBucketRateLimitor(10, 10.0 / 60);
        Client client = new Client(1, "Mohit");

        System.out.println("Token bucket demo:");
        for (int i = 1; i <= 12; i++) {
            boolean allowed = tokenBucketLimiter.allowRequest(client);
            System.out.println("Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED"));
        }

        Thread.sleep(2000);
        System.out.println("After waiting 2 seconds: "
                + (tokenBucketLimiter.allowRequest(client) ? "ALLOWED" : "REJECTED"));

        RateLimitorStrategy slidingWindowLimiter = new SlidingWindowRateLimitor(3, 5);
        System.out.println("\nSliding window demo:");
        for (int i = 1; i <= 4; i++) {
            boolean allowed = slidingWindowLimiter.allowRequest(client);
            System.out.println("Window request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED"));
        }
    }
}
