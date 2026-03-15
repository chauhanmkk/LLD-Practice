package DecoratorPattern;

public class ExtraCheese extends Decorator {
    ExtraCheese(Pizza pizza) {
        super(pizza);
    }

    @Override
    public int getCost() {
        return this.pizza.getCost() + 10;
    }
    @Override
    public String getDescription() {
        return this.pizza.getDescription() + "Extra Cheese";
    }

}
