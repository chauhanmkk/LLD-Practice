package FoodDelivery.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    String customerId;
    String name;
    List<Address> addresses;

    Customer(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
        this.addresses = new ArrayList<>();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
}

