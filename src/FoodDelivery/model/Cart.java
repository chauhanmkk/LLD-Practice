package FoodDelivery.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    String cartId;
    Customer customer;
    List<CartItem> items;
    double totalAmount;

    public void addCartItem(CartItem cartItem) {
        this.totalAmount += (cartItem.quantity * cartItem.item.price);
        items.add(cartItem);
    }

    public void removeCartItem(CartItem cartItem) {
        this.totalAmount -= (cartItem.quantity * cartItem.item.price);
        items.remove(cartItem);
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    Cart(String cartId, Customer customer) {
        this.cartId = cartId;
        this.customer = customer;
        this.items = new ArrayList<>();
        this.totalAmount = 0;
    }
}
