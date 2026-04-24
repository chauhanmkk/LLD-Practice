package VendingMachine;

public class VendingMachine {
    private final Inventory inventory;
    private MachineState currentState;
    private ItemSlot selectedSlot;
    private int insertedMoney;

    VendingMachine(Inventory inventory) {
        this.inventory = inventory;
        this.currentState = new IdleState();
    }

    void selectItem(String slotId) {
        currentState.selectItem(this, slotId);
    }

    void insertMoney(int amount) {
        currentState.insertMoney(this, amount);
    }

    void dispense() {
        currentState.dispense(this);
    }

    void cancel() {
        currentState.cancel(this);
    }

    boolean hasEnoughMoney() {
        return selectedSlot != null && insertedMoney >= selectedSlot.getProduct().getPrice();
    }

    void resetTransaction() {
        selectedSlot = null;
        insertedMoney = 0;
        currentState = new IdleState();
    }

    Inventory getInventory() {
        return inventory;
    }

    MachineState getCurrentState() {
        return currentState;
    }

    void setCurrentState(MachineState currentState) {
        this.currentState = currentState;
    }

    ItemSlot getSelectedSlot() {
        return selectedSlot;
    }

    void setSelectedSlot(ItemSlot selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    int getInsertedMoney() {
        return insertedMoney;
    }

    void addInsertedMoney(int amount) {
        insertedMoney += amount;
    }
}
