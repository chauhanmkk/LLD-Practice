package RateLimitor;

public class Main {
    static void main() {
        RateLimitorStrategy strategy = new TokenBucketRateLimitor();
        Client client = new Client(1, "Mohit");
        for(int i=0;i<20;i++) {
            strategy.allowRequest(client);
        }
    }
}
