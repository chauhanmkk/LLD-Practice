package ATMMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ATM {
    Map<Denomination, Integer> mp;
    ATMState state;
    ATM() {
        mp = new ConcurrentHashMap<>();
        state = new IdleState(this);
    }

    void setState(ATMState state) {
        this.state = state;
    }

}
