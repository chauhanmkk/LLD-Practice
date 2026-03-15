package DecoratorPattern;

abstract class Decorator implements Pizza {
    protected Pizza pizza;

    Decorator(Pizza pizza) {
        this.pizza = pizza;
    }

    @Override
    public int getCost() {
        return  pizza.getCost();
    }

    @Override
    public String getDescription(){
        return pizza.getDescription();
    }
}
