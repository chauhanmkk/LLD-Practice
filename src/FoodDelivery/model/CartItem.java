package FoodDelivery.model;

public class CartItem {
    Item item;
    int quantity;

    CartItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
}
