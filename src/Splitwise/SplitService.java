package Splitwise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SplitService {
    List<Expense> expenseList;
    Map<User, Map<User, Double>> ledger;

    SplitService() {
        this.expenseList = new ArrayList<>();
        this.ledger = new ConcurrentHashMap<>();
    }

    void addExpense(Expense expense, Map<User, Double> participantMetadata) {
        expenseList.add(expense);
        expense.setSplit(participantMetadata);
        addToledger(expense);
    }

    private void addToledger(Expense expense) {
        Map<User, Double> userMap = ledger.computeIfAbsent(expense.paidBy, k-> new ConcurrentHashMap<>());
        for(Split split : expense.split) {
            userMap.put(split.user, userMap.getOrDefault(split.user,0.0) + split.amount);
        }
    }

    void settleAmount(User paidBy, User paidTO, double amount) {
        Map<User, Double> userMap = ledger.getOrDefault(paidTO, null);
        if(userMap == null) return;

        double amountPending = userMap.getOrDefault(paidBy, 0.0);
        if(amountPending >= 0 && amountPending >= amount) {
            userMap.put(paidBy, amountPending - amount);
        }
    }
}
