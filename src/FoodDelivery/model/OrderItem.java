package FoodDelivery.model;

public class OrderItem {
    Item item;
    int quantity;
    double priceAtOrderTime;    // snapshot — item price can change later

    OrderItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        this.priceAtOrderTime = item.price;
    }
}
