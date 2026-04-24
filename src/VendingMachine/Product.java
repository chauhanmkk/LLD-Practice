package VendingMachine;

public class Product {
    private final String productId;
    private final String productName;
    private final int price;

    Product(String productId, String productName, int price) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
    }

    String getProductId() {
        return productId;
    }

    String getProductName() {
        return productName;
    }

    int getPrice() {
        return price;
    }
}
