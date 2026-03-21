# Splitwise — LLD Interview Ready Sheet
> Target: Tier-1 (Amazon, Flipkart, Uber) | Pattern: Strategy | Difficulty: Hard

---

## 1. Clarifying Questions (Ask These First)

- Do we need debt simplification — minimize total transactions?
- Should users be able to create groups, or only split between two friends?
- Split types supported — equal, exact, percentage?
- Multi-currency support or single currency?
- Can an expense exist outside a group (just between two friends)?
- Do we need expense history / audit trail?

---

## 2. Core Entities

| Class | Key Fields | Responsibility |
|-------|-----------|---------------|
| `User` | userId, name | Person in the system |
| `Group` | groupId, groupName, List\<User\>, List\<Expense\>, balances | Holds members, expenses, and running balances |
| `Expense` | expenseId, paidBy, amount, description, List\<Split\>, SplitStrategy | One shared expense |
| `Split` | user, amount | One person's computed share in an expense |
| `SplitStrategy` | interface | Computes splits — Equal, Exact, Percentage |

### Balance Map
```java
// Lives inside Group
Map<String, Map<String, Double>> balances;
// balances.get("Mohit").get("Raj") = 300.0 → Mohit owes Raj ₹300
```

---

## 3. Key Design Insight — Strategy Pattern
> Interviewers check if split types are cleanly swappable.

Split type is decided **per expense** at runtime — classic Strategy.

```
SplitStrategy (interface)
    ├── EqualSplit       — divide totalAmount equally
    ├── ExactSplit       — each person's share explicitly provided
    └── PercentageSplit  — each person's share as % of total
```

---

## 4. Strategy Interface + Implementations

```java
public interface SplitStrategy {
    // metadata: percentages for PercentageSplit, exact amounts for ExactSplit, empty for EqualSplit
    List<Split> calculateSplit(double totalAmount, List<User> participants, Map<User, Double> metadata);
}

// Equal Split
public class EqualSplit implements SplitStrategy {
    @Override
    public List<Split> calculateSplit(double totalAmount, List<User> participants, Map<User, Double> metadata) {
        List<Split> splits = new ArrayList<>();
        double share = totalAmount / participants.size();
        for (User user : participants) {
            splits.add(new Split(user, share));
        }
        return splits;
    }
}

// Exact Split
public class ExactSplit implements SplitStrategy {
    @Override
    public List<Split> calculateSplit(double totalAmount, List<User> participants, Map<User, Double> metadata) {
        // metadata = { user -> exact amount they owe }
        double sum = metadata.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum != totalAmount) throw new RuntimeException("Exact amounts don't add up to total");

        List<Split> splits = new ArrayList<>();
        for (User user : participants) {
            splits.add(new Split(user, metadata.get(user)));
        }
        return splits;
    }
}

// Percentage Split
public class PercentageSplit implements SplitStrategy {
    @Override
    public List<Split> calculateSplit(double totalAmount, List<User> participants, Map<User, Double> metadata) {
        // metadata = { user -> percentage (0-100) }
        double totalPercent = metadata.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalPercent != 100.0) throw new RuntimeException("Percentages don't add up to 100");

        List<Split> splits = new ArrayList<>();
        for (User user : participants) {
            double share = (metadata.get(user) / 100.0) * totalAmount;
            splits.add(new Split(user, share));
        }
        return splits;
    }
}
```

---

## 5. Core Algorithm 1 — addExpense()

```java
public class Group {
    String groupId;
    String groupName;
    List<User> members;
    List<Expense> expenses;
    Map<String, Map<String, Double>> balances; // payer → (ower → amount)

    public Group(String groupId, String groupName) {
        this.groupId   = groupId;
        this.groupName = groupName;
        this.members   = new ArrayList<>();
        this.expenses  = new ArrayList<>();
        this.balances  = new HashMap<>();
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);

        String payerId = expense.getPaidBy().getUserId();
        List<Split> splits = expense.getSplits();

        for (Split split : splits) {
            String owerId = split.getUser().getUserId();

            // Payer doesn't owe themselves
            if (owerId.equals(payerId)) continue;

            double amount = split.getAmount();

            // Update: ower owes payer `amount` more
            balances
                .computeIfAbsent(owerId, k -> new HashMap<>())
                .merge(payerId, amount, Double::sum);

            // Update: payer is owed `amount` more from ower
            // (net offset — reduce if payer already owes ower)
            double reverseBalance = balances
                .getOrDefault(payerId, new HashMap<>())
                .getOrDefault(owerId, 0.0);

            if (reverseBalance > 0) {
                // Net off against reverse debt
                if (reverseBalance >= amount) {
                    balances.get(payerId).put(owerId, reverseBalance - amount);
                    balances.get(owerId).put(payerId, 0.0);
                } else {
                    balances.get(owerId).put(payerId, amount - reverseBalance);
                    balances.get(payerId).put(owerId, 0.0);
                }
            }
        }
    }

    public void settleUp(User payer, User payee, double amount) {
        String payerId = payer.getUserId();
        String payeeId = payee.getUserId();
        balances
            .computeIfAbsent(payerId, k -> new HashMap<>())
            .merge(payeeId, -amount, Double::sum); // reduce debt
    }

    public void printBalances() {
        for (Map.Entry<String, Map<String, Double>> outer : balances.entrySet()) {
            for (Map.Entry<String, Double> inner : outer.getValue().entrySet()) {
                if (inner.getValue() > 0) {
                    System.out.println(outer.getKey() + " owes " + inner.getKey() + " ₹" + inner.getValue());
                }
            }
        }
    }
}
```

---

## 6. Core Algorithm 2 — simplifyDebts()
> This is the hard part. Interviewers at Google/Flipkart specifically probe this.

**Problem:** Given a balance map, minimize total number of transactions.

**Approach:** Compute net balance for each person. Greedy — match largest creditor with largest debtor.

```java
public class DebtSimplifier {

    public List<String> simplify(Map<String, Map<String, Double>> balances) {
        // Step 1: Compute net balance per person
        Map<String, Double> netBalance = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> outer : balances.entrySet()) {
            String ower = outer.getKey();
            for (Map.Entry<String, Double> inner : outer.getValue().entrySet()) {
                String payee = inner.getKey();
                double amount = inner.getValue();
                if (amount <= 0) continue;

                netBalance.merge(ower, -amount, Double::sum);   // ower loses money
                netBalance.merge(payee, amount, Double::sum);   // payee gains money
            }
        }

        // Step 2: Separate into creditors (positive) and debtors (negative)
        // Use max heaps for greedy matching
        PriorityQueue<double[]> creditors = new PriorityQueue<>((a, b) -> Double.compare(b[0], a[0]));
        PriorityQueue<double[]> debtors   = new PriorityQueue<>((a, b) -> Double.compare(a[0], b[0]));

        // Store as [amount, index] to track user
        List<String> users = new ArrayList<>(netBalance.keySet());
        for (int i = 0; i < users.size(); i++) {
            double bal = netBalance.get(users.get(i));
            if (bal > 0) creditors.offer(new double[]{bal, i});
            else if (bal < 0) debtors.offer(new double[]{bal, i});
        }

        // Step 3: Greedy match largest creditor with largest debtor
        List<String> transactions = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            double[] creditor = creditors.poll();
            double[] debtor   = debtors.poll();

            double settleAmount = Math.min(creditor[0], -debtor[0]);
            String from = users.get((int) debtor[1]);
            String to   = users.get((int) creditor[1]);

            transactions.add(from + " pays " + to + " ₹" + settleAmount);

            double creditorRemainder = creditor[0] - settleAmount;
            double debtorRemainder   = debtor[0] + settleAmount;

            if (creditorRemainder > 0.001) creditors.offer(new double[]{creditorRemainder, creditor[1]});
            if (debtorRemainder < -0.001)  debtors.offer(new double[]{debtorRemainder, debtor[1]});
        }

        return transactions;
    }
}
```

**Example:**
```
Before simplification:
  A owes B ₹100
  B owes C ₹100

After simplification:
  A pays C ₹100  ← B removed from chain, 1 transaction instead of 2
```

---

## 7. Supporting Classes

```java
public class User {
    String userId;
    String name;
    User(String userId, String name) {
        this.userId = userId;
        this.name   = name;
    }
    public String getUserId() { return userId; }
}

public class Split {
    User user;
    double amount;
    Split(User user, double amount) {
        this.user   = user;
        this.amount = amount;
    }
    public User getUser()     { return user; }
    public double getAmount() { return amount; }
}

public class Expense {
    String expenseId;
    User paidBy;
    double amount;
    String description;
    List<Split> splits;
    SplitStrategy splitStrategy;

    public Expense(String expenseId, User paidBy, double amount,
                   String description, List<User> participants,
                   SplitStrategy strategy, Map<User, Double> metadata) {
        this.expenseId     = expenseId;
        this.paidBy        = paidBy;
        this.amount        = amount;
        this.description   = description;
        this.splitStrategy = strategy;
        this.splits        = strategy.calculateSplit(amount, participants, metadata);
    }

    public User getPaidBy()      { return paidBy; }
    public List<Split> getSplits() { return splits; }
}
```

---

## 8. Curveballs + Answers

| Curveball | Answer |
|-----------|--------|
| Non-group expense between 2 friends | Treat as a Group with 2 members — reuses same logic |
| Settle partial amount | `settleUp(payer, payee, amount)` reduces balance by that amount |
| User leaves group with pending balance | Check if net balance is 0 before allowing exit — throw otherwise |
| Show who owes you across ALL groups | Maintain a global `UserBalanceService` aggregating across groups |
| Percentage doesn't add to 100 | `PercentageSplit.calculateSplit()` throws `RuntimeException` early |

---

## 9. Mistakes to Avoid in Interview

| Mistake | Why It's Bad |
|---------|-------------|
| Putting status on `Expense` instead of `Split` | Per-person share belongs in `Split`, not `Expense` |
| No `Split` entity — tracking amounts in a raw Map | Loses type safety, hard to extend |
| Strategy without `metadata` param | Can't pass percentages or exact amounts to strategy |
| Balance map as `Map<String, Map<String, Expense>>` | Balance is a number, not an Expense object |
| Skipping debt simplification | It's explicitly asked — greedy net-balance approach is expected |
| No net-offset in `addExpense` | Balances grow unboundedly — A owes B and B owes A simultaneously |

---

## 10. One-Line Pattern Justifications
> Say these out loud in the interview.

- **Strategy for Split** — *"Split type is decided per expense at runtime — Strategy lets us swap Equal, Exact, Percentage without touching Expense logic."*
- **Split as separate entity** — *"Each participant has their own share — Split bridges the Strategy output and balance tracking."*
- **Net balance map** — *"We don't track raw debts per expense — we maintain net balance between each pair so simplification is O(users) not O(expenses)."*
- **Greedy debt simplification** — *"Match largest creditor with largest debtor greedily — provably minimizes transaction count."*