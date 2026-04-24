package VendingMachine;

public class ReadyToDispenseState implements MachineState {
    @Override
    public void selectItem(VendingMachine machine, String slotId) {
        throw new IllegalStateException("Cannot change item after enough money has been inserted");
    }

    @Override
    public void insertMoney(VendingMachine machine, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Inserted amount should be positive");
        }
        machine.addInsertedMoney(amount);
    }

    @Override
    public void dispense(VendingMachine machine) {
        ItemSlot selectedSlot = machine.getSelectedSlot();
        if (selectedSlot == null) {
            throw new IllegalStateException("No item selected");
        }

        selectedSlot.dispenseOne();
        int change = machine.getInsertedMoney() - selectedSlot.getProduct().getPrice();

        System.out.println("Dispensed item: " + selectedSlot.getProduct().getProductName());
        if (change > 0) {
            System.out.println("Returned change: " + change);
        }

        machine.resetTransaction();
    }

    @Override
    public void cancel(VendingMachine machine) {
        int refund = machine.getInsertedMoney();
        machine.resetTransaction();
        System.out.println("Refunded amount: " + refund);
    }

    @Override
    public String getName() {
        return "READY_TO_DISPENSE";
    }
}
