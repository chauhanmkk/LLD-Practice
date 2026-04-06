# LLD: Amazon Locker Service

## Problem Statement
Design a low-level system for Amazon Locker — a self-service kiosk system where customers can pick up packages from a nearby locker instead of home delivery.

---

## Functional Requirements
- Delivery agent can assign a locker of appropriate size to a package
- System generates a unique OTP and maps it to the assigned locker
- Customer enters OTP at kiosk to retrieve their package
- If package not picked up within 3 days, locker is auto-freed by a background job
- Support multiple LockerHubs in a city
- Support Small / Medium / Large locker sizes

## Non-Functional Requirements
- Thread-safe locker assignment (concurrent delivery agents)
- OTP brute-force protection (rate limiting + lockout after 3 wrong attempts)
- O(1) locker lookup by ID

---

## Core Entities

```
AmazonLockerSystem
    └── List<LockerHub>

LockerHub
    ├── int hubId
    ├── Location location (lat, lon)
    ├── Map<LockerSize, List<Locker>> lockersBySize
    └── Map<Integer, Locker> lockerById

Locker
    ├── int lockerId
    ├── LockerSize size (enum: SMALL, MEDIUM, LARGE)
    ├── LockerStatus status (enum: VACANT, OCCUPIED)
    ├── long maxExpiryTime
    ├── Package currentPackage
    └── ReentrantLock lock

Package
    ├── int packageId
    ├── int orderId
    ├── LockerSize size
    ├── String agentId
    └── String customerId

LockerService
    ├── Map<String, Integer> otpToLockerIdMap   // ConcurrentHashMap
    ├── Map<Integer, LockerHub> hubMap
    └── LockerAssignStrategy strategy
```

---

## Key Design Decisions

### 1. Strategy Pattern for Locker Assignment
Different hubs or business rules may need different assignment logic (nearest, least-used, size-optimized). Strategy pattern keeps `LockerService` open for extension.

```java
public interface LockerAssignStrategy {
    Locker assignLocker(LockerSize size, List<Locker> candidates);
}
```

### 2. Per-Locker ReentrantLock with tryLock()
- Lock granularity at individual Locker level (not hub, not size-group)
- `tryLock()` — non-blocking; if a locker is taken, move to next
- Zero contention when different lockers are targeted simultaneously
- Always release in `finally` block

### 3. OTP Brute Force Protection
- Track wrong attempt count per session/IP
- Lock kiosk terminal after 3 wrong consecutive attempts
- Reset counter on successful unlock

### 4. Background Expiry Job
- Runs every N minutes
- Scans all OCCUPIED lockers
- If `System.currentTimeMillis() > maxExpiryTime` → free locker, initiate return flow

---

## Concurrency — Critical Rules

| Operation | Lock Needed | Why |
|---|---|---|
| assignLocker | `locker.lock.tryLock()` | Prevent double-assignment |
| removeFromLocker (OTP pickup) | `locker.lock.lock()` | Prevent race on status update |
| Background expiry job | `locker.lock.tryLock()` | Skip if locker being accessed |

**Always release lock in finally:**
```java
locker.getLock().lock();
try {
    // mutate locker state
} finally {
    locker.getLock().unlock();
}
```

---

## TOCTOU Warning
Pre-filtering VACANT lockers before acquiring the lock is a **Time-Of-Check-Time-Of-Use** race condition. Status may change between filter and lock acquisition. Always **double-check status inside the lock**.

```java
if (locker.lock.tryLock()) {
    try {
        if (locker.status == LockerStatus.VACANT) { // double check inside lock
            // safe to assign
        }
    } finally {
        locker.lock.unlock();
    }
}
```

---

## Tier-1 Level Code

```java
// Enums
public enum LockerSize { SMALL, MEDIUM, LARGE }
public enum LockerStatus { VACANT, OCCUPIED }

// Package
public class Package {
    private final int packageId;
    private final int orderId;
    private final LockerSize size;
    private final String agentId;
    private final String customerId;

    public Package(int packageId, int orderId, LockerSize size, String agentId, String customerId) {
        this.packageId = packageId;
        this.orderId = orderId;
        this.size = size;
        this.agentId = agentId;
        this.customerId = customerId;
    }

    public LockerSize getSize() { return size; }
    public int getPackageId() { return packageId; }
}

// Locker
public class Locker {
    private final int lockerId;
    private final LockerSize size;
    private volatile LockerStatus status;
    private long maxExpiryTime;
    private Package currentPackage;
    private final ReentrantLock lock = new ReentrantLock();

    public Locker(int lockerId, LockerSize size) {
        this.lockerId = lockerId;
        this.size = size;
        this.status = LockerStatus.VACANT;
    }

    public ReentrantLock getLock() { return lock; }
    public int getLockerId() { return lockerId; }
    public LockerSize getSize() { return size; }
    public LockerStatus getStatus() { return status; }
    public void setStatus(LockerStatus status) { this.status = status; }
    public void setCurrentPackage(Package p) { this.currentPackage = p; }
    public void setMaxExpiryTime(long t) { this.maxExpiryTime = t; }
    public long getMaxExpiryTime() { return maxExpiryTime; }
    public Package getCurrentPackage() { return currentPackage; }
}

// LockerHub
public class LockerHub {
    private final int hubId;
    private final Map<LockerSize, List<Locker>> lockersBySize = new HashMap<>();
    private final Map<Integer, Locker> lockerById = new HashMap<>();

    public LockerHub(int hubId) {
        this.hubId = hubId;
        for (LockerSize size : LockerSize.values()) {
            lockersBySize.put(size, new ArrayList<>());
        }
    }

    public void addLocker(Locker locker) {
        lockersBySize.get(locker.getSize()).add(locker);
        lockerById.put(locker.getLockerId(), locker);
    }

    public List<Locker> getLockersBySize(LockerSize size) {
        return lockersBySize.getOrDefault(size, Collections.emptyList());
    }

    public Locker getLockerById(int lockerId) {
        return lockerById.get(lockerId);
    }

    public int getHubId() { return hubId; }
}

// Strategy Interface
public interface LockerAssignStrategy {
    Locker assignLocker(LockerSize size, List<Locker> candidates);
}

// Strategy Implementation
public class FirstAvailableLockerStrategy implements LockerAssignStrategy {

    private static final long THREE_DAYS_MS = 1000L * 60 * 60 * 24 * 3;

    @Override
    public Locker assignLocker(LockerSize size, List<Locker> candidates) {
        for (Locker locker : candidates) {
            if (locker.getStatus() == LockerStatus.VACANT) { // pre-filter (optimistic)
                if (locker.getLock().tryLock()) {
                    try {
                        if (locker.getStatus() == LockerStatus.VACANT) { // double-check inside lock
                            locker.setStatus(LockerStatus.OCCUPIED);
                            locker.setMaxExpiryTime(System.currentTimeMillis() + THREE_DAYS_MS);
                            return locker;
                        }
                    } finally {
                        locker.getLock().unlock();
                    }
                }
            }
        }
        return null; // no locker available
    }
}

// LockerService
public class LockerService {

    private final Map<String, Integer> otpToLockerIdMap = new ConcurrentHashMap<>();
    private final Map<Integer, LockerHub> hubMap = new ConcurrentHashMap<>();
    private final LockerAssignStrategy strategy;
    private final Map<String, Integer> otpAttemptCount = new ConcurrentHashMap<>();
    private static final int MAX_OTP_ATTEMPTS = 3;

    public LockerService(LockerAssignStrategy strategy) {
        this.strategy = strategy;
    }

    public void registerHub(LockerHub hub) {
        hubMap.put(hub.getHubId(), hub);
    }

    // Called by delivery orchestration after order placement
    public String assignLocker(Package pack, int hubId) {
        LockerHub hub = hubMap.get(hubId);
        if (hub == null) throw new IllegalArgumentException("Hub not found: " + hubId);

        List<Locker> candidates = hub.getLockersBySize(pack.getSize());
        Locker locker = strategy.assignLocker(pack.getSize(), candidates);

        if (locker == null) throw new RuntimeException("No locker available for size: " + pack.getSize());

        locker.setCurrentPackage(pack);

        String otp = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        otpToLockerIdMap.put(otp, locker.getLockerId());
        return otp;
    }

    // Called by customer at kiosk
    public boolean openLocker(String otp, int hubId) {
        int attempts = otpAttemptCount.getOrDefault(otp + "_attempts", 0);
        if (attempts >= MAX_OTP_ATTEMPTS) {
            throw new RuntimeException("Too many wrong attempts. Kiosk locked.");
        }

        Integer lockerId = otpToLockerIdMap.get(otp);
        if (lockerId == null) {
            otpAttemptCount.merge(otp + "_attempts", 1, Integer::sum);
            return false;
        }

        LockerHub hub = hubMap.get(hubId);
        Locker locker = hub.getLockerById(lockerId);

        locker.getLock().lock();
        try {
            locker.setStatus(LockerStatus.VACANT);
            locker.setCurrentPackage(null);
            locker.setMaxExpiryTime(0);
            otpToLockerIdMap.remove(otp);
            otpAttemptCount.remove(otp + "_attempts");
            return true;
        } finally {
            locker.getLock().unlock();
        }
    }

    // Background expiry job — call via ScheduledExecutorService every 30 min
    public void runExpiryJob() {
        long now = System.currentTimeMillis();
        for (LockerHub hub : hubMap.values()) {
            for (LockerSize size : LockerSize.values()) {
                for (Locker locker : hub.getLockersBySize(size)) {
                    if (locker.getStatus() == LockerStatus.OCCUPIED
                            && locker.getMaxExpiryTime() > 0
                            && now > locker.getMaxExpiryTime()) {
                        if (locker.getLock().tryLock()) {
                            try {
                                if (now > locker.getMaxExpiryTime()) {
                                    // initiate return flow here
                                    locker.setStatus(LockerStatus.VACANT);
                                    locker.setCurrentPackage(null);
                                    locker.setMaxExpiryTime(0);
                                }
                            } finally {
                                locker.getLock().unlock();
                            }
                        }
                    }
                }
            }
        }
    }
}
```

---

## Gaps You Had — Remember These

| Gap | Correct Approach |
|---|---|
| No `unlock()` after `tryLock()` | Always `try { ... } finally { unlock() }` |
| `removeFromLocker` had no lock | Acquire `locker.lock.lock()` before mutating status |
| `assignLocker` returned void | Must return OTP to caller |
| Lock at hub/size level | Lock at individual Locker level with `tryLock()` |
| No brute force protection on OTP | Track attempt count, lockout after 3 failures |
| O(n) locker lookup by id | Maintain `Map<Integer, Locker>` for O(1) lookup |
| Package entity missing initially | Package needed for traceability (orderId, agentId, customerId) |

---

## Patterns Used
- **Strategy Pattern** — LockerAssignStrategy (pluggable assignment logic)
- **Background Job / Reaper Pattern** — expiry cleanup via ScheduledExecutorService

---

## What a Tier-1 Interviewer Expects
1. Enums for status and size — never raw Strings
2. `tryLock()` with `try/finally` — non-negotiable
3. Double-check status inside lock (TOCTOU awareness)
4. O(1) locker lookup — `Map<Integer, Locker>` in LockerHub
5. OTP returned from `assignLocker`, not swallowed
6. Brute force protection mentioned proactively
7. Background expiry job with its own `tryLock()` to avoid conflicts
8. Strategy pattern for assignment — shows extensibility thinking
