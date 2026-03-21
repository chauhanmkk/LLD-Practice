package ATMMachine;

public class CardInsertedState implements ATMState {
    @Override
    public void insertCard(String card) {
        System.out.println("Card");
    }

    @Override
    public void enterPin() {
        System.out.println("Enter Pin");
    }

    @Override
    public void dispenseCash() {
        throw new RuntimeException("Enter pin first!!");
    }
}
