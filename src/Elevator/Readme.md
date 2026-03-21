# Elevator System — LLD Revision Notes

## Problem Summary
Design an Elevator System for a multi-floor building with multiple elevators, supporting hall requests (from floors) and cabin requests (inside elevator), with a swappable assignment strategy.

---

## Core Entities

| Class | Responsibility |
|-------|---------------|
| `Elevator` | Holds state, currentFloor, upPQ, downPQ. Processes requests |
| `Request` | Interface — HallRequest / CabinRequest implement it |
| `ElevatorController` | Orchestrator — receives requests, delegates to strategy |
| `ElevatorAssignmentStrategy` | Interface for swappable assignment logic |
| `NearestElevatorStrategy` | Finds elevator with minimum floor distance |

---

## Enums

```java
enum ElevatorState  { IDLE, UP, DOWN, MAINTENANCE }
enum Direction      { UP, DOWN }
enum RequestType    { HALL, CABIN }
```

---

## Request Hierarchy

```java
interface Request {
    int getTargetFloor();
    RequestType getRequestType();
}

class HallRequest implements Request {
    int targetFloor;
    Direction direction; // user pressed UP or DOWN on the floor
}

class CabinRequest implements Request {
    int targetFloor;     // user pressed button inside elevator
}
```

---

## Elevator — Key Fields + Core Logic

```java
class Elevator {
    int id;
    int currentFloor;
    ElevatorState state;
    PriorityQueue<Integer> upPQ;    // min-heap
    PriorityQueue<Integer> downPQ;  // max-heap (Collections.reverseOrder())

    void addRequest(Request request) {
        if (request.getTargetFloor() > currentFloor)
            upPQ.add(request.getTargetFloor());
        else
            downPQ.add(request.getTargetFloor());
    }

    // CORE ALGORITHM — SCAN (Elevator Algorithm)
    synchronized void processNextRequest() {
        if (state == UP && !upPQ.isEmpty()) {
            currentFloor = upPQ.poll();
            if (upPQ.isEmpty()) state = IDLE;

        } else if (state == DOWN && !downPQ.isEmpty()) {
            currentFloor = downPQ.poll();
            if (downPQ.isEmpty()) state = IDLE;

        } else if (state == IDLE) {
            if (!upPQ.isEmpty())       { state = UP;   processNextRequest(); }
            else if (!downPQ.isEmpty()){ state = DOWN; processNextRequest(); }
        }
    }
}
```

**Why SCAN?** Serves all floors in one direction before reversing — minimizes total travel distance vs naive nearest-floor.

---

## Strategy Pattern

```java
interface ElevatorAssignmentStrategy {
    Elevator assignElevator(List<Elevator> elevators, Request request);
}

class NearestElevatorStrategy implements ElevatorAssignmentStrategy {
    public Elevator assignElevator(List<Elevator> elevators, Request request) {
        Elevator best = null;
        for (Elevator e : elevators) {
            if (best == null) { best = e; continue; }
            int diff     = Math.abs(e.getCurrentFloor() - request.getTargetFloor());
            int bestDiff = Math.abs(best.getCurrentFloor() - request.getTargetFloor());
            if (diff < bestDiff) best = e;
        }
        return best;
    }
}
```

---

## ElevatorController

```java
class ElevatorController {
    List<Elevator> elevators;
    ElevatorAssignmentStrategy strategy;

    void addHallRequest(Request request) {
        // Guard: only HALL requests go through assignment
        if (request.getRequestType() != RequestType.HALL) return;
        Elevator elevator = strategy.assignElevator(elevators, request);
        elevator.addRequest(request);
    }

    void addCabinRequest(Request request, Elevator elevator) {
        // Cabin request goes directly to the specific elevator
        elevator.addRequest(request);
    }
}
```

---

## Thread Safety
- `processNextRequest()` must be `synchronized` — multiple threads could trigger it
- `addRequest()` should also be `synchronized` — concurrent requests to same elevator
- PriorityQueue is **not** thread-safe — use `PriorityBlockingQueue` in production

---

## Key Design Patterns Used

| Pattern | Where |
|---------|-------|
| **Strategy** | `ElevatorAssignmentStrategy` — swappable assignment logic |
| **State** | `ElevatorState` — IDLE/UP/DOWN/MAINTENANCE transitions |

---

## Common Interview Curveballs

| Curveball | Answer |
|-----------|--------|
| Add priority for VIP floors | New `PriorityElevatorStrategy` — no existing code changes |
| One elevator goes to MAINTENANCE | Strategy skips elevators with `state == MAINTENANCE` |
| Swap to Zoning strategy | Implement `ZoneBasedStrategy` — floors 1-5 → Elevator 1, etc. |
| Concurrency — 1000 simultaneous requests | `PriorityBlockingQueue` + thread pool in controller |

---

## Mistakes to Avoid
1. **`return null` at end of strategy** — always return the best found elevator
2. **No state transition back to IDLE** — elevator stays UP forever when PQ empties
3. **Dead code** — unused variables (`ElevatorState direction`) flags poor attention
4. **CABIN requests going through strategy** — controller must guard this
5. **No `id` on Elevator** — undebuggable with multiple elevators

---

## Score Card (Mock Interview)
| Area | Score |
|------|-------|
| Class design | 6/10 |
| Strategy pattern | 7/10 |
| Core algorithm | 6/10 |
| Thread safety | 0/10 |
| Overall | **6.5/10** |