package VendingMachine;

public class ItemSelectedState implements MachineState {
    @Override
    public void selectItem(VendingMachine machine, String slotId) {
        throw new IllegalStateException("Item already selected. Cancel current transaction first");
    }

    @Override
    public void insertMoney(VendingMachine machine, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Inserted amount should be positive");
        }

        machine.addInsertedMoney(amount);
        if (machine.hasEnoughMoney()) {
            machine.setCurrentState(new ReadyToDispenseState());
        }
    }

    @Override
    public void dispense(VendingMachine machine) {
        throw new IllegalStateException("Insufficient money inserted");
    }

    @Override
    public void cancel(VendingMachine machine) {
        int refund = machine.getInsertedMoney();
        machine.resetTransaction();
        System.out.println("Refunded amount: " + refund);
    }

    @Override
    public String getName() {
        return "ITEM_SELECTED";
    }
}
