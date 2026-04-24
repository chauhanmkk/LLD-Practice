package Splitwise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EqualStrategy implements SplitStrategy {
    @Override
    public List<Split> splitStrategy(User paidBy, double amount, Map<User, Double> participantMetadata) {
        List<Split> result = new ArrayList<>();
        int totalParticipants = participantMetadata.size();
        for(User user : participantMetadata.keySet()) {
            if(user == paidBy) continue;
            Split split = new Split();
            split.amount = (amount)/totalParticipants;
            split.user = user;
            result.add(split);
        }
        return result;
    }
}
