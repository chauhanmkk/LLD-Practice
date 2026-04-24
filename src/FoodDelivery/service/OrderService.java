package FoodDelivery.service;

import FoodDelivery.model.Order;
import FoodDelivery.model.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService {
    Map<String, List<Order>> orderStorage;

    OrderService() {
        this.orderStorage = new ConcurrentHashMap<>();
    }

    void addOrder(Order order) {
        orderStorage.computeIfAbsent(order.getCustomer().getCustomerId(), k-> new ArrayList<>()).add(order);
    }
}
