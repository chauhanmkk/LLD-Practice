package VendingMachine;

public class ItemSlot {
    private final String slotId;
    private final Product product;
    private int quantity;

    ItemSlot(String slotId, Product product, int quantity) {
        this.slotId = slotId;
        this.product = product;
        this.quantity = quantity;
    }

    String getSlotId() {
        return slotId;
    }

    Product getProduct() {
        return product;
    }

    int getQuantity() {
        return quantity;
    }

    boolean isAvailable() {
        return quantity > 0;
    }

    void dispenseOne() {
        if (!isAvailable()) {
            throw new IllegalStateException("Item out of stock for slot: " + slotId);
        }
        quantity--;
    }
}
