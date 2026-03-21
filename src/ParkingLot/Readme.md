# Parking Lot LLD

## Requirements (Clarify These in Interview First)

- Multiple floors, multiple spots per floor
- Spot types: Small, Medium, Large
- Vehicle types: Bike, Car, Truck
- Bike → Small, Car → Medium, Truck → Large
- Generate ticket on entry, calculate fee on exit
- Multiple payment modes: Cash, Card
- Parking lot is a single building — Singleton
- Track available spots per type

---

## Design Patterns Used

| Pattern | Where |
|---|---|
| **Singleton** | `ParkingLot` — only one instance |
| **Strategy** | `PaymentStrategy` — swap Cash/Card without changing fee logic |
| **Factory** (optional) | `SpotFactory` — create correct spot type |

---

## Class Structure

```
ParkingLot (Singleton)
└── List<ParkingFloor>
    └── List<ParkingSpot> (abstract)
        ├── SmallSpot
        ├── MediumSpot
        └── LargeSpot

Vehicle (abstract)
├── Bike       → needs SmallSpot
├── Car        → needs MediumSpot
└── Truck      → needs LargeSpot

Ticket
└── Vehicle, ParkingSpot, entryTime

PaymentStrategy (interface)
├── CashPayment
└── CardPayment
```

---

## Code

### 1. Vehicle Hierarchy

```java
public enum VehicleType {
    BIKE, CAR, TRUCK
}

public abstract class Vehicle {
    protected String licensePlate;
    protected VehicleType type;

    Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public VehicleType getType() { return type; }
    public String getLicensePlate() { return licensePlate; }
}

public class Bike extends Vehicle {
    Bike(String licensePlate) {
        super(licensePlate, VehicleType.BIKE);
    }
}

public class Car extends Vehicle {
    Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }
}

public class Truck extends Vehicle {
    Truck(String licensePlate) {
        super(licensePlate, VehicleType.TRUCK);
    }
}
```

---

### 2. ParkingSpot Hierarchy

```java
public enum SpotType {
    SMALL, MEDIUM, LARGE
}

public abstract class ParkingSpot {
    protected int spotId;
    protected SpotType spotType;
    protected boolean isOccupied;
    protected Vehicle vehicle;

    ParkingSpot(int spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.isOccupied = false;
    }

    public boolean isAvailable() { return !isOccupied; }

    public void assignVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.isOccupied = true;
    }

    public void removeVehicle() {
        this.vehicle = null;
        this.isOccupied = false;
    }

    public int getSpotId() { return spotId; }
    public SpotType getSpotType() { return spotType; }
}

public class SmallSpot extends ParkingSpot {
    SmallSpot(int spotId) { super(spotId, SpotType.SMALL); }
}

public class MediumSpot extends ParkingSpot {
    MediumSpot(int spotId) { super(spotId, SpotType.MEDIUM); }
}

public class LargeSpot extends ParkingSpot {
    LargeSpot(int spotId) { super(spotId, SpotType.LARGE); }
}
```

---

### 3. Ticket

```java
public class Ticket {
    private static int counter = 0;

    private int ticketId;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;

    Ticket(Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = ++counter;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public long getParkingDurationInHours() {
        return ChronoUnit.HOURS.between(entryTime, exitTime == null ? LocalDateTime.now() : exitTime);
    }

    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public int getTicketId() { return ticketId; }
    public LocalDateTime getEntryTime() { return entryTime; }
}
```

---

### 4. Payment Strategy

```java
// Strategy interface
public interface PaymentStrategy {
    void pay(double amount);
}

public class CashPayment implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Paid ₹" + amount + " via Cash.");
    }
}

public class CardPayment implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Paid ₹" + amount + " via Card.");
    }
}
```

---

### 5. FeeCalculator

```java
public class FeeCalculator {
    // rates per hour (₹)
    private static final Map<SpotType, Double> RATES = Map.of(
        SpotType.SMALL,  20.0,
        SpotType.MEDIUM, 40.0,
        SpotType.LARGE,  80.0
    );

    public double calculate(Ticket ticket) {
        long hours = Math.max(1, ticket.getParkingDurationInHours()); // minimum 1 hour
        double rate = RATES.get(ticket.getSpot().getSpotType());
        return hours * rate;
    }
}
```

---

### 6. ParkingFloor

```java
public class ParkingFloor {
    private int floorNumber;
    private List<ParkingSpot> spots = new ArrayList<>();

    ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public void addSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    // Find first available spot matching the required type
    public ParkingSpot getAvailableSpot(SpotType type) {
        return spots.stream()
                .filter(s -> s.getSpotType() == type && s.isAvailable())
                .findFirst()
                .orElse(null);
    }

    public int getFloorNumber() { return floorNumber; }
}
```

---

### 7. ParkingLot — Singleton

```java
public class ParkingLot {

    private static ParkingLot instance;  // Singleton instance
    private List<ParkingFloor> floors = new ArrayList<>();
    private Map<Integer, Ticket> activeTickets = new HashMap<>(); // ticketId → Ticket
    private FeeCalculator feeCalculator = new FeeCalculator();

    // SpotType required per VehicleType
    private static final Map<VehicleType, SpotType> SPOT_MAP = Map.of(
        VehicleType.BIKE,  SpotType.SMALL,
        VehicleType.CAR,   SpotType.MEDIUM,
        VehicleType.TRUCK, SpotType.LARGE
    );

    private ParkingLot() {}  // private constructor

    public static ParkingLot getInstance() {
        if (instance == null) {
            synchronized (ParkingLot.class) {
                if (instance == null) {        // double-checked locking
                    instance = new ParkingLot();
                }
            }
        }
        return instance;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    // Entry
    public Ticket parkVehicle(Vehicle vehicle) {
        SpotType required = SPOT_MAP.get(vehicle.getType());

        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.getAvailableSpot(required);
            if (spot != null) {
                spot.assignVehicle(vehicle);
                Ticket ticket = new Ticket(vehicle, spot);
                activeTickets.put(ticket.getTicketId(), ticket);
                System.out.println("Parked " + vehicle.getLicensePlate()
                        + " at Floor " + floor.getFloorNumber()
                        + " Spot " + spot.getSpotId());
                return ticket;
            }
        }

        System.out.println("No spot available for " + vehicle.getType());
        return null;
    }

    // Exit
    public void exitVehicle(int ticketId, PaymentStrategy paymentStrategy) {
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            System.out.println("Invalid ticket.");
            return;
        }

        ticket.setExitTime(LocalDateTime.now());
        double fee = feeCalculator.calculate(ticket);

        paymentStrategy.pay(fee);

        ticket.getSpot().removeVehicle();
        activeTickets.remove(ticketId);
        System.out.println("Vehicle " + ticket.getVehicle().getLicensePlate() + " exited.");
    }
}
```

---

### 8. Main — Client

```java
public class Main {
    public static void main(String[] args) {

        // Setup
        ParkingLot lot = ParkingLot.getInstance();

        ParkingFloor floor1 = new ParkingFloor(1);
        floor1.addSpot(new SmallSpot(101));
        floor1.addSpot(new SmallSpot(102));
        floor1.addSpot(new MediumSpot(201));
        floor1.addSpot(new MediumSpot(202));
        floor1.addSpot(new LargeSpot(301));
        lot.addFloor(floor1);

        // Entry
        Vehicle bike = new Bike("KA-01-AB-1234");
        Vehicle car  = new Car("KA-02-CD-5678");

        Ticket bikeTicket = lot.parkVehicle(bike);
        Ticket carTicket  = lot.parkVehicle(car);

        // Exit
        lot.exitVehicle(bikeTicket.getTicketId(), new CashPayment());
        lot.exitVehicle(carTicket.getTicketId(),  new CardPayment());
    }
}
```

---

## Interview Follow-up Questions

### "How do you handle concurrency — two vehicles getting the same spot?"
The `parkVehicle` method needs synchronization. Simplest approach: `synchronized` on the method. Better: lock per `SpotType` to reduce contention.

```java
public synchronized Ticket parkVehicle(Vehicle vehicle) { ... }
```

### "How would you support monthly passes?"
Add a `PricingStrategy` interface — `HourlyPricing`, `MonthlyPass`. Inject into `FeeCalculator` instead of hardcoding rates.

### "What if a vehicle type can use multiple spot sizes?"
Change `SPOT_MAP` from `VehicleType → SpotType` to `VehicleType → List<SpotType>` and try in order of preference.

### "How would you add a display board showing available spots?"
Add a `DisplayBoard` class that holds `Map<SpotType, Integer>` counts. `ParkingFloor` notifies it on `assignVehicle` / `removeVehicle` — Observer Pattern.

---

## Pattern Summary

| Pattern | Class | Why |
|---|---|---|
| Singleton | `ParkingLot` | One lot, shared global state |
| Strategy | `PaymentStrategy` | Swap payment mode without changing exit logic |
| Observer *(extension)* | `DisplayBoard` | React to spot availability changes |
| Factory *(extension)* | `SpotFactory` | Centralize spot creation logic |