package VendingMachine;

public class IdleState implements MachineState {
    @Override
    public void selectItem(VendingMachine machine, String slotId) {
        ItemSlot slot = machine.getInventory().getSlot(slotId);
        if (slot == null) {
            throw new IllegalArgumentException("Invalid slot selected: " + slotId);
        }
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Selected item is out of stock");
        }
        machine.setSelectedSlot(slot);
        machine.setCurrentState(new ItemSelectedState());
    }

    @Override
    public void insertMoney(VendingMachine machine, int amount) {
        throw new IllegalStateException("Select an item before inserting money");
    }

    @Override
    public void dispense(VendingMachine machine) {
        throw new IllegalStateException("No item selected");
    }

    @Override
    public void cancel(VendingMachine machine) {
        machine.resetTransaction();
    }

    @Override
    public String getName() {
        return "IDLE";
    }
}
