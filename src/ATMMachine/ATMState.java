package ATMMachine;

public interface ATMState {
    void insertCard(String card);

    void enterPin();

    void dispenseCash();
}
