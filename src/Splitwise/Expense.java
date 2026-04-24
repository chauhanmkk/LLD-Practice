package Splitwise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Expense {
    int expenseId;
    User paidBy;
    List<Split> split;
    double amount;
    SplitType splitType;

    public Expense(int expenseId, User paidBy, double amount, SplitType splitType) {
        this.expenseId = expenseId;
        this.paidBy = paidBy;
        this.split = new ArrayList<>();
        this.amount = amount;
        this.splitType = splitType;
    }

    void setSplit(Map<User, Double> participantMetadata) {
        SplitFactory factory =new SplitFactory();
        SplitStrategy strategy = factory.getStrategy(this.splitType);
        this.split = strategy.splitStrategy(this.paidBy, amount, participantMetadata);
    }
}
