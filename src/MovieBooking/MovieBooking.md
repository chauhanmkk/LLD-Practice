# BookMyShow — LLD Interview Ready Sheet
> Target: Tier-1 (Google, Amazon, Flipkart, Uber) | Pattern: Strategy + Concurrency | Difficulty: Hard

---

## 1. Clarifying Questions (Ask These First)
> Never skip this. Interviewers give silent credit for good scoping.

- Single city or multi-city support?
- Can a user book multiple seats in one transaction?
- How long is a seat locked before it auto-expires? (e.g., 10 min)
- Do we need waitlist / notification when seats free up?
- Any seat categories? (VIP, Regular, Recliner)
- Payment — just initiate or full retry/refund logic?

---

## 2. Core Entities

| Class | Key Fields | Responsibility |
|-------|-----------|---------------|
| `Movie` | movieId, title, duration | Represents a film |
| `Theater` | theaterId, city, List\<Show\> | Physical theater in a city |
| `Show` | showId, Movie, List\<ShowSeat\>, startTime, endTime | One screening of a movie |
| `Seat` | seatId, rowNumber, SeatType | Physical seat in theater |
| `ShowSeat` | showId, seatId, Status | Seat status **per show** ← key insight |
| `Booking` | bookingId, userId, List\<ShowSeat\>, timestamp | Confirmed reservation |
| `User` | userId, name, email | Person making booking |

### Enums
```java
enum Status   { AVAILABLE, LOCKED, BOOKED }
enum SeatType { REGULAR, VIP, RECLINER }
```

---

## 3. Key Design Insight — Why ShowSeat?
> This is the #1 thing interviewers check.

Seat availability is **per show**, not global.
- Same physical seat can be AVAILABLE for 6PM show but BOOKED for 9PM show.
- Putting `status` directly on `Seat` breaks this — it can only hold one state.
- `ShowSeat` decouples physical seat from its per-show availability.

```
Seat (physical) ──< ShowSeat (status per show) >── Show
```

---

## 4. Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `PaymentStrategy` | Swap UPI/Card/Wallet without changing booking logic |
| **Observer** | Waitlist notification | Notify users when seat becomes AVAILABLE |

```java
interface PaymentStrategy {
    boolean pay(int userId, double amount);
}
class UPIPayment  implements PaymentStrategy { ... }
class CardPayment implements PaymentStrategy { ... }
```

---

## 5. SRP-Compliant Service Layer
> Single Responsibility — each class does ONE thing.

```java
class SeatAvailabilityService {
    void validateAndLock(List<ShowSeat> seats) {
        for (ShowSeat seat : seats) {
            if (seat.getStatus() != Status.AVAILABLE)
                throw new RuntimeException("Seat " + seat.getSeatId() + " not available");
        }
        seats.forEach(s -> s.setStatus(Status.LOCKED));
    }
    void confirm(List<ShowSeat> seats) { seats.forEach(s -> s.setStatus(Status.BOOKED)); }
    void release(List<ShowSeat> seats) { seats.forEach(s -> s.setStatus(Status.AVAILABLE)); }
}

class PaymentService {
    boolean processPayment(int userId, double amount, PaymentStrategy strategy) {
        return strategy.pay(userId, amount);
    }
}

class BookingService {
    Booking createBooking(int userId, List<ShowSeat> seats) {
        return new Booking(UUID.randomUUID().toString(), userId, seats, LocalDateTime.now());
    }
}
```

---

## 6. Core Algorithm — bookSeat()
> Thread safety is the heart of this problem.

```java
public class BookingController {
    // Per-show lock — NOT one global lock
    Map<String, ReentrantLock> showLocks = new ConcurrentHashMap<>();

    SeatAvailabilityService seatService;
    PaymentService paymentService;
    BookingService bookingService;
    PaymentStrategy paymentStrategy;

    Booking bookSeat(List<ShowSeat> seats, int userId) {
        String showId = seats.get(0).getShowId();
        ReentrantLock lock = showLocks.computeIfAbsent(showId, k -> new ReentrantLock());

        // tryLock with timeout — user shouldn't wait forever
        if (!lock.tryLock(5, TimeUnit.SECONDS))
            throw new RuntimeException("System busy, please try again");

        try {
            seatService.validateAndLock(seats);

            boolean paid = paymentService.processPayment(userId,
                              calculateAmount(seats), paymentStrategy);
            if (paid) {
                seatService.confirm(seats);
                return bookingService.createBooking(userId, seats);
            } else {
                seatService.release(seats);
                throw new RuntimeException("Payment failed — seats released");
            }
        } finally {
            lock.unlock(); // always unlocks even if exception thrown
        }
    }
}
```

---

## 7. Thread Safety — The Crucial Distinction

| Approach | Granularity | Verdict |
|----------|------------|---------|
| `synchronized` on method | Entire controller — all shows blocked | ❌ Wrong |
| Single `ReentrantLock` field | Same as above | ❌ Wrong |
| `synchronized(showLockObj)` per show | Per show | ✅ Works |
| `ReentrantLock` per show via `ConcurrentHashMap` | Per show | ✅ Best |

**Golden Rule:** Lock granularity must match contention scope.
Two users booking **different shows** should never block each other.

### ReentrantLock vs synchronized
| Feature | `synchronized` | `ReentrantLock` |
|---------|---------------|-----------------|
| Per-object locking | ✅ (with object ref) | ✅ |
| `tryLock` with timeout | ❌ | ✅ ← use this |
| Fairness control | ❌ | ✅ `new ReentrantLock(true)` |
| Interruptible wait | ❌ | ✅ |

---

## 8. Curveballs + Answers
> Interviewers WILL throw these. Have them ready.

| Curveball | Answer |
|-----------|--------|
| Seat lock expires in 10 min | `ScheduledExecutorService` — scheduled task releases LOCKED → AVAILABLE after timeout |
| Notify waitlisted users when seat frees | Observer pattern — `SeatAvailabilityObserver` notified on status change |
| Add seat categories pricing | `SeatType` enum on `Seat`, `PricingStrategy` per type |
| Booking cancellation | Reverse flow — BOOKED → AVAILABLE, trigger refund via `PaymentStrategy` |
| Scale to millions of users | Per-show lock already helps; add Redis distributed lock for multi-instance deployment |
| Two users book last seat simultaneously | Per-show lock ensures only one proceeds; second gets "not available" exception |

---

## 9. Mistakes to Avoid in Interview

| Mistake | Why It's Bad |
|---------|-------------|
| Global lock instead of per-show | Kills concurrency — entire system serialized |
| Status on `Seat` instead of `ShowSeat` | Can't support same seat in multiple shows |
| No rollback on payment failure | Seats stay LOCKED forever — system degrades |
| Status as `String` | Typo-prone, not type-safe — always use enum |
| Booking without `userId` | Untrackable — who made the booking? |
| No `tryLock` timeout | Thread waits indefinitely under load |
| Fat controller (SRP violation) | If method has comments like `// check`, `// pay`, `// create` — each belongs in its own service |

---

## 10. One-Line Pattern Justification
> Say these out loud in the interview.

- **Strategy for Payment** — *"Payment method can change at runtime without touching booking logic."*
- **Per-show ReentrantLock** — *"Contention only exists between users booking the same show, not across shows."*
- **ShowSeat as separate entity** — *"Seat availability is a function of both the seat and the show — it needs its own entity."*
- **SRP service layer** — *"Each service has one reason to change — seat logic, payment logic, and booking creation are independent concerns."*