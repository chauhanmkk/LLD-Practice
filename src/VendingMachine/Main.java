package VendingMachine;

public class Main {
    public static void main(String[] args) {
        Product coke = new Product("P1", "Coke", 40);
        Product chips = new Product("P2", "Chips", 25);

        Inventory inventory = new Inventory();
        inventory.addSlot(new ItemSlot("A1", coke, 2));
        inventory.addSlot(new ItemSlot("B1", chips, 1));

        VendingMachine machine = new VendingMachine(inventory);

        printInventory(machine);

        System.out.println("\nSuccessful purchase:");
        machine.selectItem("A1");
        machine.insertMoney(20);
        machine.insertMoney(30);
        System.out.println("Current state: " + machine.getCurrentState().getName());
        machine.dispense();

        System.out.println("\nCancel transaction:");
        machine.selectItem("B1");
        machine.insertMoney(10);
        machine.cancel();

        System.out.println("\nOut of stock flow:");
        machine.selectItem("B1");
        machine.insertMoney(25);
        machine.dispense();

        try {
            machine.selectItem("B1");
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }

        System.out.println("\nFinal inventory:");
        printInventory(machine);
    }

    private static void printInventory(VendingMachine machine) {
        for (ItemSlot slot : machine.getInventory().getAllSlots()) {
            System.out.println(slot.getSlotId() + " -> " + slot.getProduct().getProductName()
                    + ", price=" + slot.getProduct().getPrice()
                    + ", quantity=" + slot.getQuantity());
        }
    }
}
