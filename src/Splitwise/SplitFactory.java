package Splitwise;

public class SplitFactory {


    SplitStrategy getStrategy(SplitType splitType) {
        switch (splitType) {
            case EQUAL -> {
                return new EqualStrategy();
            }
            case EXACT -> {
                return new ExactSplit();
            }
            default -> {
                return null;
            }
        }
    }
}
