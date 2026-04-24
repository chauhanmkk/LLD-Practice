package Splitwise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExactSplit implements SplitStrategy {
    @Override
    public List<Split> splitStrategy(User paidBy, double amount, Map<User, Double> participantMetadata) {
        List<Split> result = new ArrayList<>();
        //cross check if metadata total amount == amount , if not throw exception

        //core logic
        for(User user : participantMetadata.keySet()) {
            Split split = new Split();
            split.amount = participantMetadata.get(user);
            split.user = user;
            result.add(split);
        }
        return result;
    }
}
