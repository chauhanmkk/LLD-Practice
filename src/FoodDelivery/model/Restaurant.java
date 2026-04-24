package FoodDelivery.model;

public class Restaurant {
    String restId;
    String name;
    Menu menu;
    Location location;
    int maxCapacity;

    Restaurant(String restId, String name, Location location, int maxCapacity) {
        this.restId = restId;
        this.name = name;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.menu = new Menu();
    }
}
