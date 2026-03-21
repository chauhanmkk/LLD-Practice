# Ride Sharing (Uber/Ola) — LLD Interview Ready Sheet
> Target: Tier-1 (Uber, Amazon, Flipkart, Google) | Pattern: Strategy + State + Observer | Difficulty: Hard

---

## 1. Clarifying Questions (Ask These First)

- One driver per ride or can rides be pooled (UberPool)?
- Can driver reject a ride? How many rejections before next driver is tried?
- How is fare calculated — flat, distance-based, surge pricing?
- Do we need real-time driver location tracking?
- Should we support ride cancellation by user/driver?
- Do we need ratings for both driver and user post-trip?
- Multiple ride types? (Auto, Mini, Sedan, XL)

---

## 2. Core Entities

| Class | Key Fields | Responsibility |
|-------|-----------|---------------|
| `User` | userId, name | Person requesting ride |
| `Driver` | driverId, name, Location, DriverState | Available/busy driver |
| `Ride` | rideId, userId, driverId, pickup, drop, RideStatus, cost, rejectedDriverIds | Full ride lifecycle |
| `Location` | latitude, longitude | Reused across Driver + Ride |
| `Rating` | ratingId, userId, driverId, rating, rideId | Post-trip rating |
| `RideController` | DriverSearchStrategy, FareCalculationStrategy | Orchestrator |

### Enums
```java
enum DriverState  { IDLE, ON_TRIP }
enum RideStatus   { NEW, DRIVER_ASSIGNED, PICKUP, ONGOING, COMPLETED, CANCELLED }
```

---

## 3. Key Design Insight — State Synchronization
> This is the #1 thing interviewers check for Ride Sharing.

**DriverState and RideStatus must stay in sync — atomically.**

If two users request a ride simultaneously and both get assigned the same IDLE driver:
- Driver ends up on two rides
- Classic race condition

```java
// WRONG — two separate updates, not atomic
driver.setState(ON_TRIP);
ride.setStatus(DRIVER_ASSIGNED);

// RIGHT — synchronized block ensures atomicity
synchronized (driver) {
    if (driver.getState() != DriverState.IDLE)
        throw new RuntimeException("Driver no longer available");
    driver.setState(DriverState.ON_TRIP);
    ride.setDriverId(driver.getDriverId());
    ride.setStatus(RideStatus.DRIVER_ASSIGNED);
}
```

---

## 4. Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `DriverSearchStrategy` | Swap Nearest/Rating-based/Surge-aware assignment |
| **Strategy** | `FareCalculationStrategy` | Swap Normal/Surge pricing without changing Ride logic |
| **State** | `DriverState`, `RideStatus` | Both entities have well-defined lifecycle transitions |
| **Observer** | Driver notification on ride request | Notify driver asynchronously — decouple assignment from acceptance |

---

## 5. Strategy Interfaces

```java
// Driver Search Strategy
public interface DriverSearchStrategy {
    List<Driver> findDrivers(Location pickup, List<Driver> availableDrivers, int topN);
}

public class NearestDriverStrategy implements DriverSearchStrategy {
    @Override
    public List<Driver> findDrivers(Location pickup, List<Driver> availableDrivers, int topN) {
        return availableDrivers.stream()
            .filter(d -> d.getState() == DriverState.IDLE)
            .sorted((a, b) -> Double.compare(
                distance(a.getLocation(), pickup),
                distance(b.getLocation(), pickup)))
            .limit(topN)
            .collect(Collectors.toList());
    }

    private double distance(Location a, Location b) {
        // Haversine or simple Euclidean for interview
        return Math.sqrt(Math.pow(a.lat - b.lat, 2) + Math.pow(a.lng - b.lng, 2));
    }
}

// Fare Calculation Strategy
public interface FareCalculationStrategy {
    double calculateFare(Location pickup, Location drop, RideType rideType);
}

public class NormalFareStrategy implements FareCalculationStrategy {
    private static final double BASE_FARE   = 30.0;
    private static final double RATE_PER_KM = 12.0;

    @Override
    public double calculateFare(Location pickup, Location drop, RideType rideType) {
        double distance = computeDistance(pickup, drop);
        return BASE_FARE + (distance * RATE_PER_KM);
    }
}

public class SurgePricingStrategy implements FareCalculationStrategy {
    private final double surgeMultiplier;

    public SurgePricingStrategy(double surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }

    @Override
    public double calculateFare(Location pickup, Location drop, RideType rideType) {
        double baseFare = new NormalFareStrategy().calculateFare(pickup, drop, rideType);
        return baseFare * surgeMultiplier;
    }
}
```

---

## 6. Core Algorithm — requestRide()

```java
public class RideController {
    private final List<Driver> drivers;
    private final DriverSearchStrategy driverSearchStrategy;
    private final FareCalculationStrategy fareStrategy;

    public Ride requestRide(String userId, Location pickup, Location drop) {
        // 1. Create ride in NEW state
        Ride ride = new Ride(UUID.randomUUID().toString(), userId, pickup, drop);
        ride.setStatus(RideStatus.NEW);

        // 2. Find top N candidate drivers
        List<Driver> candidates = driverSearchStrategy.findDrivers(pickup, drivers, 5);

        // 3. Try assigning — skip rejected drivers
        for (Driver driver : candidates) {
            if (ride.getRejectedDriverIds().contains(driver.getDriverId())) continue;

            boolean assigned = tryAssign(driver, ride);
            if (assigned) {
                double fare = fareStrategy.calculateFare(pickup, drop, RideType.MINI);
                ride.setCost(fare);
                return ride;
            }
        }

        throw new RuntimeException("No drivers available");
    }

    // Atomic assignment — prevents two rides mapping to same driver
    private boolean tryAssign(Driver driver, Ride ride) {
        synchronized (driver) {
            if (driver.getState() != DriverState.IDLE) return false;
            driver.setState(DriverState.ON_TRIP);
            ride.setDriverId(driver.getDriverId());
            ride.setStatus(RideStatus.DRIVER_ASSIGNED);
            return true;
        }
    }

    public void driverRejectsRide(Driver driver, Ride ride) {
        synchronized (driver) {
            driver.setState(DriverState.IDLE);              // free driver back up
            ride.getRejectedDriverIds().add(driver.getDriverId());
            ride.setStatus(RideStatus.NEW);                 // back to searching
        }
        // Retry assignment with remaining candidates
        requestRide(ride.getUserId(), ride.getPickup(), ride.getDrop());
    }

    public void completeRide(Driver driver, Ride ride) {
        synchronized (driver) {
            driver.setState(DriverState.IDLE);              // driver free again
            ride.setStatus(RideStatus.COMPLETED);
        }
    }

    public void cancelRide(Driver driver, Ride ride) {
        synchronized (driver) {
            driver.setState(DriverState.IDLE);
            ride.setStatus(RideStatus.CANCELLED);
        }
    }
}
```

---

## 7. Ride + Driver Classes

```java
public class Location {
    double lat, lng;
    Location(double lat, double lng) { this.lat = lat; this.lng = lng; }
}

public class Driver {
    String driverId;
    String name;
    Location location;
    DriverState state;

    Driver(String driverId, String name, Location location) {
        this.driverId = driverId;
        this.name     = name;
        this.location = location;
        this.state    = DriverState.IDLE;
    }

    public synchronized void setState(DriverState state) { this.state = state; }
    public synchronized DriverState getState()           { return state; }
}

public class Ride {
    String rideId;
    String userId;
    String driverId;
    Location pickup, drop;
    RideStatus status;
    double cost;
    List<String> rejectedDriverIds = new ArrayList<>();

    public Ride(String rideId, String userId, Location pickup, Location drop) {
        this.rideId  = rideId;
        this.userId  = userId;
        this.pickup  = pickup;
        this.drop    = drop;
        this.status  = RideStatus.NEW;
    }
}

public class Rating {
    String ratingId;
    String userId;
    String driverId;
    String rideId;
    int rating;       // 1-5
}
```

---

## 8. RideStatus Transition Diagram

```
NEW
 │
 │ driver assigned
 ▼
DRIVER_ASSIGNED
 │                 ──> CANCELLED (user cancels before pickup)
 │ driver arrives
 ▼
PICKUP
 │
 │ trip starts
 ▼
ONGOING
 │
 │ trip ends
 ▼
COMPLETED
```

---

## 9. Curveballs + Answers

| Curveball | Answer |
|-----------|--------|
| Driver rejects — find next | `rejectedDriverIds` on Ride — skip in next search iteration |
| Two users get same driver | `synchronized(driver)` in `tryAssign()` — only one succeeds |
| Surge pricing at peak hours | Inject `SurgePricingStrategy` instead of `NormalFareStrategy` — no other changes |
| UberPool (shared rides) | `Ride` gets `List<String> userIds`, driver picked up multiple users |
| Driver goes offline mid-search | Filter `DriverState.IDLE` in strategy — offline drivers excluded |
| Rating after trip | `RatingService.addRating(rideId, userId, driverId, rating)` — triggered post COMPLETED |
| Driver location updates in real time | Observer pattern — drivers publish location events, system subscribes |

---

## 10. Mistakes to Avoid in Interview

| Mistake | Why It's Bad |
|---------|-------------|
| No `Location` class — raw lat/lng in Driver | Not reusable — pickup/drop/driver location all need same type |
| DriverState and RideStatus updated separately | Race condition — one driver assigned to two rides |
| No `rejectedDriverIds` on Ride | Can't skip already-rejected drivers in retry |
| Fare hardcoded in Ride | Can't swap surge/normal pricing — OCP violation |
| `synchronized` on entire `requestRide()` | Too coarse — serializes all ride requests globally |
| Missing CANCELLED state | Real systems always need cancellation |

---

## 11. One-Line Pattern Justifications
> Say these out loud in the interview.

- **Strategy for driver search** — *"Assignment algorithm can change — nearest today, highest-rated tomorrow — Strategy keeps RideController unchanged."*
- **Strategy for fare** — *"Surge pricing is a runtime decision — injecting FareCalculationStrategy means zero changes to ride logic."*
- **synchronized on Driver object** — *"Contention is per driver, not global — locking the driver object prevents double-assignment without serializing all requests."*
- **rejectedDriverIds on Ride** — *"Ride owns its own retry context — controller doesn't need to track which drivers were tried."*
- **State enums on both Driver and Ride** — *"Both entities have independent lifecycles that must stay in sync — explicit states make transitions auditable."*