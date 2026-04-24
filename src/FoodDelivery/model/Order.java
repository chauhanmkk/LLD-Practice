package FoodDelivery.model;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class Order {
    String orderId;
    Customer customer;
    Restaurant restaurant;
    Driver driver;              // null until assigned
    List<OrderItem> items;
    Location dropLocation;
    OrderStatus status;

    public String getOrderId() {
        return orderId;
    }



    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(Location dropLocation) {
        this.dropLocation = dropLocation;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    double totalAmount;

    Order(String orderId, Customer customer, Restaurant restaurant,
          List<OrderItem> items, Location dropLocation, double totalAmount) {
        this.orderId = orderId;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = items;
        this.dropLocation = dropLocation;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PLACED;
        this.driver = null;
    }
}
