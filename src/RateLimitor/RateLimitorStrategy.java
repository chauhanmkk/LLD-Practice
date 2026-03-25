package RateLimitor;

public interface RateLimitorStrategy {
    boolean allowRequest(Client client);
}
