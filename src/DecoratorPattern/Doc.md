# Decorator Pattern — Pizza Example

## What is the Decorator Pattern?

Wrap objects inside other objects to **add behavior dynamically**, without modifying the original class.

> **Rule of thumb:** Use Decorator when you want to add responsibilities to objects at runtime, and subclassing would lead to an explosion of classes.

---

## Structure

```
Pizza (interface)
├── MargheritaPizza        → Concrete Component
└── PizzaDecorator         → Abstract Decorator
    └── ExtraCheeseDecorator → Concrete Decorator
```

---

## Code

### 1. Component Interface

```java
public interface Pizza {
    String getDescription();
    double getCost();
}
```

---

### 2. Concrete Component (Base Pizza)

```java
public class MargheritaPizza implements Pizza {

    @Override
    public String getDescription() {
        return "Margherita Pizza";
    }

    @Override
    public double getCost() {
        return 200.0;
    }
}
```

---

### 3. Abstract Decorator

Wraps a `Pizza` and also **IS-A** `Pizza`. This is the heart of the pattern.

```java
public abstract class PizzaDecorator implements Pizza {

    protected Pizza pizza; // the wrapped object

    public PizzaDecorator(Pizza pizza) {
        this.pizza = pizza;
    }

    @Override
    public String getDescription() {
        return pizza.getDescription(); // delegates to wrapped object
    }

    @Override
    public double getCost() {
        return pizza.getCost();
    }
}
```

---

### 4. Concrete Decorator

```java
public class ExtraCheeseDecorator extends PizzaDecorator {

    public ExtraCheeseDecorator(Pizza pizza) {
        super(pizza);
    }

    @Override
    public String getDescription() {
        return pizza.getDescription() + " + Extra Cheese";
    }

    @Override
    public double getCost() {
        return pizza.getCost() + 50.0;
    }
}
```

---

### 5. Client — Stacking Decorators

```java
public class Main {
    public static void main(String[] args) {

        Pizza myPizza = new MargheritaPizza();
        System.out.println(myPizza.getDescription()); // Margherita Pizza
        System.out.println(myPizza.getCost());         // 200.0

        // Wrap with Extra Cheese
        myPizza = new ExtraCheeseDecorator(myPizza);
        System.out.println(myPizza.getDescription()); // Margherita Pizza + Extra Cheese
        System.out.println(myPizza.getCost());         // 250.0

        // Stack another decorator
        myPizza = new ExtraCheeseDecorator(myPizza);
        System.out.println(myPizza.getDescription()); // Margherita Pizza + Extra Cheese + Extra Cheese
        System.out.println(myPizza.getCost());         // 300.0
    }
}
```

---

## The 3 Things to Lock In

| Thing | Why it matters |
|---|---|
| Decorator **implements** the same interface | So it can substitute anywhere a `Pizza` is expected |
| Decorator **holds a reference** to a `Pizza` | This is the wrapping — behavior is delegated here |
| Decorator **calls** `pizza.method()` and adds its own | That's how the chain builds up dynamically |

---

## Real-World Java Example

`BufferedReader` wrapping `FileReader` — exact same pattern.

```java
BufferedReader br = new BufferedReader(new FileReader("file.txt"));
```

- `BufferedReader` IS-A `Reader`
- Wraps a `Reader`
- Adds buffering behavior on top

That is Decorator.

---

## When to Use

- You want to add behavior to individual objects, not the entire class
- Subclassing is impractical (would cause a class explosion)
- You want to stack multiple behaviors at runtime

## When NOT to Use

- When the wrapping order matters in non-obvious ways — can get hard to debug
- When a simple subclass is sufficient

---

## Pattern Classification

| Property | Value |
|---|---|
| Type | Structural |
| Also known as | Wrapper |
| Java I/O usage | `InputStream`, `OutputStream`, `Reader`, `Writer` hierarchies |