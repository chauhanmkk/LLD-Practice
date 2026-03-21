package ATMMachine;

public class ATMController {

    void handleATM(String bankAccountId, String cardNumber, ATM atm, String pin, int amount) {
        //1. Insert card
        //2. Enter Pin
        //3. Verify Pin with Bank throw exception if incorrect
        //4. Enter amount
        //5. Verify amount and check available balance with bank throw error if insufficent balance
        //6. Dispense cash using cash dispenser strategy -> RIght now greedy strategy

        atm.setState(new CardInsertedState());
        atm.state.insertCard(cardNumber);
        verifyPin(pin);

    }

    boolean verifyPin(String pin) {
        return true;
    }
}
