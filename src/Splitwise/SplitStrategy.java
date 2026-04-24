package Splitwise;

import java.util.List;
import java.util.Map;

public interface SplitStrategy {
    List<Split> splitStrategy(User paidBy, double amount, Map<User, Double> participantMetadata);
}
