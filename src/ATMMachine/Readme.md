# ATM Machine — LLD Interview Ready Sheet
> Target: Tier-1 (Google, Amazon, Microsoft) | Pattern: State + Strategy | Difficulty: Medium

---

## 1. Clarifying Questions (Ask These First)

- Finite notes or infinite supply? (Track denominations?)
- User selects note breakdown or greedy default?
- How many wrong PIN attempts before card is blocked?
- Do we need to simulate bank validation or mock it?
- Does ATM go OUT_OF_SERVICE when cash runs out?

---

## 2. Core Entities

| Class | Key Fields | Responsibility |
|-------|-----------|---------------|
| `ATM` | ATMState, CashDispenser, BankService | Host of state machine — delegates all actions to current state |
| `ATMState` | interface | Contract for all states |
| `IdleState` | ATM ref | Accepts card, rejects everything else |
| `CardInsertedState` | ATM ref | Accepts PIN, rejects card insert + cash |
| `PinEnteredState` | ATM ref | Accepts cash request, rejects card + PIN |
| `CashDispenser` | Map\<Denomination, Integer\> | Greedy dispensing logic — SRP |
| `BankService` | — | validateCard(), validatePin(), debit() |
| `Card` | cardNumber | Represents inserted card |

### Enums
```java
enum Denomination {
    TWO_THOUSAND(2000), FIVE_HUNDRED(500), TWO_HUNDRED(200), ONE_HUNDRED(100);
    private final int value;
}
```

---

## 3. Key Design Insight — State Pattern
> This is the #1 thing interviewers check for ATM.

**ATM behaviour changes completely based on current state.**
Without State pattern you end up with deeply nested if-else chains:
```java
// BAD — without State pattern
void insertCard() {
    if (state == IDLE) { ... }
    else if (state == CARD_INSERTED) { throw ... }
    else if (state == PIN_ENTERED) { throw ... }
    // grows forever with each new state
}
```

**With State pattern — each state is its own class, zero if-else:**
```java
// IdleState — only valid action
void insertCard(String card) { atm.setState(new CardInsertedState(atm)); }

// CardInsertedState — guard everything except enterPin
void insertCard(String card) { throw new RuntimeException("Card already inserted"); }
```

---

## 4. State Transition Diagram

```
                    insertCard()
    IDLE ──────────────────────────> CARD_INSERTED
     ^                                     │
     │                         enterPin()  │
     │  wrong PIN         ┌────────────────┘
     │◄───────────── PIN_ENTERED
     │                    │
     │                    │ dispenseCash()
     │                    ▼
     └────────────── CASH_DISPENSED ──> IDLE
                         (ejectCard)

    Any state ──> OUT_OF_SERVICE (when CashDispenser empty)
```

---

## 5. Golden Rule — States Transition States, Never Controller

```java
// WRONG — controller manually sets state
atm.setState(new CardInsertedState());
atm.state.insertCard(cardNumber);

// RIGHT — controller only calls action, state handles transition internally
atm.state.insertCard(cardNumber); // IdleState internally calls atm.setState()
```

**Every state needs ATM reference** to trigger the next transition.
If a state doesn't have `ATM ref`, it cannot transition — you'll get stuck.

---

## 6. ATMState Interface

```java
public interface ATMState {
    void insertCard(String cardNumber);
    void enterPin(String pin);           // pin must be passed
    void dispenseCash(int amount);       // amount must be passed
    void ejectCard();
}
```

---

## 7. Full State Implementations

```java
// IDLE — only insertCard is valid
public class IdleState implements ATMState {
    ATM atm;
    IdleState(ATM atm) { this.atm = atm; }

    @Override
    public void insertCard(String cardNumber) {
        System.out.println("Card inserted: " + cardNumber);
        atm.setState(new CardInsertedState(atm));        // transition
    }

    @Override public void enterPin(String pin)     { throw new RuntimeException("Insert card first"); }
    @Override public void dispenseCash(int amount) { throw new RuntimeException("Insert card first"); }
    @Override public void ejectCard()              { throw new RuntimeException("No card inserted"); }
}

// CARD_INSERTED — only enterPin is valid
public class CardInsertedState implements ATMState {
    ATM atm;
    CardInsertedState(ATM atm) { this.atm = atm; }

    @Override
    public void insertCard(String cardNumber) {
        throw new RuntimeException("Card already inserted"); // guard
    }

    @Override
    public void enterPin(String pin) {
        boolean valid = atm.getBankService().validatePin(pin);
        if (valid) {
            atm.setState(new PinEnteredState(atm));          // transition forward
        } else {
            System.out.println("Wrong PIN — ejecting card");
            atm.setState(new IdleState(atm));                // transition back
        }
    }

    @Override public void dispenseCash(int amount) { throw new RuntimeException("Enter PIN first"); }
    @Override public void ejectCard()              { atm.setState(new IdleState(atm)); }
}

// PIN_ENTERED — only dispenseCash is valid
public class PinEnteredState implements ATMState {
    ATM atm;
    PinEnteredState(ATM atm) { this.atm = atm; }

    @Override public void insertCard(String cardNumber) { throw new RuntimeException("Card already inserted"); }
    @Override public void enterPin(String pin)          { throw new RuntimeException("PIN already entered"); }

    @Override
    public void dispenseCash(int amount) {
        Map<Denomination, Integer> dispensed = atm.getCashDispenser().dispense(amount);
        boolean debited = atm.getBankService().debit(amount);
        if (debited) {
            System.out.println("Dispensing: " + dispensed);
            atm.setState(new IdleState(atm));                // back to IDLE after success
        } else {
            System.out.println("Insufficient balance");
            atm.setState(new IdleState(atm));
        }
    }

    @Override public void ejectCard() { atm.setState(new IdleState(atm)); }
}
```

---

## 8. CashDispenser — Greedy Algorithm

```java
public class CashDispenser {
    // TreeMap sorted descending — largest denomination first
    private final TreeMap<Denomination, Integer> notes = new TreeMap<>(
        (a, b) -> b.getValue() - a.getValue()
    );

    public Map<Denomination, Integer> dispense(int amount) {
        Map<Denomination, Integer> result = new LinkedHashMap<>();
        int remaining = amount;

        for (Map.Entry<Denomination, Integer> entry : notes.entrySet()) {
            int denomValue = entry.getKey().getValue();
            int available  = entry.getValue();
            if (remaining <= 0) break;

            int needed = Math.min(remaining / denomValue, available);
            if (needed > 0) {
                result.put(entry.getKey(), needed);
                remaining -= needed * denomValue;
                entry.setValue(available - needed);  // deduct from stock
            }
        }

        if (remaining > 0)
            throw new RuntimeException("Cannot dispense exact amount — insufficient notes");

        return result;
    }
}
```

**Example:** dispense(2600) → {TWO_THOUSAND: 1, FIVE_HUNDRED: 1, ONE_HUNDRED: 1}

---

## 9. ATM Host Class

```java
public class ATM {
    private ATMState state;
    private CashDispenser cashDispenser;
    private BankService bankService;

    ATM(CashDispenser cashDispenser, BankService bankService) {
        this.cashDispenser = cashDispenser;
        this.bankService   = bankService;
        this.state         = new IdleState(this);  // always starts IDLE
    }

    public void setState(ATMState state)       { this.state = state; }
    public CashDispenser getCashDispenser()     { return cashDispenser; }
    public BankService getBankService()         { return bankService; }

    // Controller calls these — ATM delegates to current state
    public void insertCard(String card)        { state.insertCard(card); }
    public void enterPin(String pin)           { state.enterPin(pin); }
    public void dispenseCash(int amount)       { state.dispenseCash(amount); }
    public void ejectCard()                    { state.ejectCard(); }
}
```

---

## 10. ATMController — Entry Point

```java
public class ATMController {

    private final ATM atm;

    ATMController(ATM atm) { this.atm = atm; }

    // Full user flow — controller only calls actions, never sets state
    public void runSession(String cardNumber, String pin, int amount) {
        try {
            atm.insertCard(cardNumber);   // IDLE → CARD_INSERTED
            atm.enterPin(pin);            // CARD_INSERTED → PIN_ENTERED (or back to IDLE on wrong PIN)
            atm.dispenseCash(amount);     // PIN_ENTERED → IDLE
        } catch (RuntimeException e) {
            System.out.println("Session failed: " + e.getMessage());
            atm.ejectCard();              // always eject on failure
        }
    }
}
```

**Key points:**
- Controller has **zero state transition logic** — it just calls actions in sequence
- Every action delegates to `atm.state.xyz()` — the current state decides what to do
- `catch + ejectCard()` ensures ATM never stays stuck in a non-IDLE state on failure

---

## 10. ATMController — Thin Orchestrator

```java
public class ATMController {
    private final ATM atm;

    ATMController(ATM atm) { this.atm = atm; }

    public void handleTransaction(String cardNumber, String pin, int amount) {
        try {
            atm.insertCard(cardNumber);   // IDLE → CARD_INSERTED
            atm.enterPin(pin);            // CARD_INSERTED → PIN_ENTERED (or IDLE on wrong PIN)
            atm.dispenseCash(amount);     // PIN_ENTERED → IDLE
        } catch (RuntimeException e) {
            System.out.println("Transaction failed: " + e.getMessage());
            atm.ejectCard();              // always eject on failure — safety net
        }
    }
}
```

**Rules:**
- Controller sequences actions only — zero state knowledge
- Never calls `setState()` directly — that's the states' job
- `ejectCard()` in catch block ensures ATM always resets to IDLE on failure

---

## 11. Curveballs + Answers

| Curveball | Answer |
|-----------|--------|
| 3 wrong PIN attempts blocks card | Counter in `CardInsertedState` — after 3 failures transition to `CardBlockedState` |
| ATM runs out of cash mid-session | `CashDispenser.dispense()` throws → `PinEnteredState` catches → transition to `OutOfServiceState` |
| Add receipt printing | Observer pattern — `ReceiptPrinter` observes successful dispense event |
| Concurrent users on same ATM | `synchronized` on ATM state transitions — one session at a time |
| Multiple denominations unavailable | Greedy handles it — `remaining > 0` after loop throws exception |

---

## 11. Mistakes to Avoid in Interview

| Mistake | Why It's Bad |
|---------|-------------|
| States without ATM reference | Cannot trigger next transition — dead-end state |
| Controller calling `setState()` directly | Violates State pattern — controller only calls actions |
| `enterPin()` with no parameter | Can't pass PIN — incomplete interface |
| `CardInsertedState.insertCard()` printing instead of throwing | Wrong guard — card is already inserted |
| CashDispenser logic inside ATM | SRP violation — greedy algorithm belongs in its own class |
| Missing `OUT_OF_SERVICE` state | ATM can't handle cash depletion gracefully |

---

## 12. One-Line Pattern Justifications
> Say these out loud in the interview.

- **State Pattern** — *"ATM behaviour changes completely per state — State pattern eliminates if-else chains and makes each state independently extensible."*
- **States transition states, not controller** — *"The controller doesn't know transition logic — each state knows what comes next."*
- **CashDispenser as separate class** — *"Greedy dispensing logic is independent of ATM state — SRP keeps it testable and swappable."*
- **TreeMap descending for greedy** — *"Sorting denominations largest-first ensures minimum notes dispensed naturally."*
- **Every state needs ATM ref** — *"Without ATM reference, a state is a dead end — it can validate but cannot transition."*