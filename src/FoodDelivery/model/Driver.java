package FoodDelivery.model;

import java.util.concurrent.locks.ReentrantLock;

public class Driver {
    String driverId;
    String name;
    DriverStatus status;
    Location currentLocation;
    ReentrantLock lock = new ReentrantLock();

    Driver(String driverId, String name, Location location) {
        this.driverId = driverId;
        this.name = name;
        this.currentLocation = location;
        this.status = DriverStatus.IDLE;
    }
}
