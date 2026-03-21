package ATMMachine;

public class IdleState implements ATMState {

    ATM atm;

    IdleState(ATM atm) {
        this.atm = atm;
    }

    @Override
    public void insertCard(String card) {
        System.out.println("Inserting card " + card);
        atm.setState(new CardInsertedState());
    }

    @Override
    public void enterPin() {
        throw new RuntimeException("No card inserted");
    }

    @Override
    public void dispenseCash() {
        throw new RuntimeException("No card inserted");
    }
}
