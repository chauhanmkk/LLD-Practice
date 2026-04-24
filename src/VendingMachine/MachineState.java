package VendingMachine;

public interface MachineState {
    void selectItem(VendingMachine machine, String slotId);
    void insertMoney(VendingMachine machine, int amount);
    void dispense(VendingMachine machine);
    void cancel(VendingMachine machine);
    String getName();
}
