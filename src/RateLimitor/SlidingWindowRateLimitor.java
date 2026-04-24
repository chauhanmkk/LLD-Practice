package RateLimitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Window {
    private final int capacity;
    private final int windowSizeInSeconds;
    private final Deque<Long> requests;

    Window(int capacity, int windowSizeInSeconds) {
        this.capacity = capacity;
        this.windowSizeInSeconds = windowSizeInSeconds;
        this.requests = new ArrayDeque<>();
    }

    synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        long windowStart = now - (long) windowSizeInSeconds * 1000;

        while (!requests.isEmpty() && requests.peekFirst() < windowStart) {
            requests.pollFirst();
        }

        if (requests.size() >= capacity) {
            return false;
        }

        requests.addLast(now);
        return true;
    }
}

public class SlidingWindowRateLimitor implements RateLimitorStrategy {
    private final Map<Client, Window> storage;
    private final int defaultCapacity;
    private final int defaultWindowSizeInSeconds;

    SlidingWindowRateLimitor() {
        this(10, 60);
    }

    SlidingWindowRateLimitor(int defaultCapacity, int defaultWindowSizeInSeconds) {
        this.storage = new ConcurrentHashMap<>();
        this.defaultCapacity = defaultCapacity;
        this.defaultWindowSizeInSeconds = defaultWindowSizeInSeconds;
    }

    void addClient(Client client, Window window) {
        storage.put(client, window);
    }

    @Override
    public boolean allowRequest(Client client) {
        Window window = storage.computeIfAbsent(
                client,
                ignored -> new Window(defaultCapacity, defaultWindowSizeInSeconds)
        );
        return window.allowRequest();
    }
}
