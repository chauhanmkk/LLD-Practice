package FoodDelivery.model;

public class Item {
    String itemId;
    String name;
    double price;

    Item(String itemId, String name, double price) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
    }
}
