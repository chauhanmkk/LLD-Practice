package FoodDelivery.service;

import FoodDelivery.model.Cart;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CartService {

    Map<String, List<Cart>> cartStorage;

    CartService() {
        this.cartStorage = new ConcurrentHashMap<>();
    }

    void addToCart(Cart cart) {
        cartStorage.computeIfAbsent(cart.getCustomer().getCustomerId(), k-> new ArrayList<>()).add(cart);
    }

    void clearCart(Cart cart) {
        String key = cart.getCustomer().getCustomerId();
        if(cartStorage.containsKey(key)) {
            cartStorage.get(key).clear();
        }
    }
}
