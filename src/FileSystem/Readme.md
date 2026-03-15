# FileSystem LLD — Composite Pattern

## Pattern Used

**Composite Pattern** — lets you treat individual objects (File) and compositions (Directory) uniformly through a common interface.

> Use when you have a tree structure and want to treat leaf and composite nodes the same way.

---

## Structure

```
FileSystemComponent (abstract)
├── File              → Leaf      (no children, no add/remove)
└── Directory         → Composite (holds children, delegates to them)
```

---

## Common Mistakes to Avoid

| Mistake | Why it's wrong |
|---|---|
| Putting `List<children>` in the base class | `File` is a leaf — it should never know about children |
| Not initializing the children list | Guaranteed `NullPointerException` on `add()` |
| No abstract methods in abstract base class | You're enforcing no contract — defeats the purpose |
| Importing `java.io.File` alongside your own `File` class | Naming conflict and ambiguity |
| `static void main()` instead of `public static void main(String[] args)` | Won't run |

---

## Code

### 1. Component — Abstract Base

```java
public abstract class FileSystemComponent {
    protected String name;

    FileSystemComponent(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public abstract int getSize();
    public abstract void display(String indent);
}
```

---

### 2. File — Leaf Node

```java
public class File extends FileSystemComponent {
    private String content;

    File(String name, String content) {
        super(name);
        this.content = content;
    }

    @Override
    public int getSize() {
        return content.length();
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + "📄 " + name + " (" + getSize() + " bytes)");
    }
}
```

---

### 3. Directory — Composite Node

```java
public class Directory extends FileSystemComponent {
    private List<FileSystemComponent> children = new ArrayList<>(); // always initialize
    private FileSystemComponent parent;

    Directory(String name, FileSystemComponent parent) {
        super(name);
        this.parent = parent;
    }

    public void add(FileSystemComponent component) {
        children.add(component);
    }

    public void remove(FileSystemComponent component) {
        children.remove(component);
    }

    @Override
    public int getSize() {
        // delegates to children — works recursively for any depth
        return children.stream().mapToInt(FileSystemComponent::getSize).sum();
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + "📁 " + name + "/");
        for (FileSystemComponent child : children) {
            child.display(indent + "  "); // recursive
        }
    }
}
```

---

### 4. Client

```java
public class Main {
    public static void main(String[] args) {

        Directory root = new Directory("root", null);

        root.add(new File("hello.txt", "Hello World"));
        root.add(new File("readme.md", "Read this"));

        Directory subDir = new Directory("src", root);
        subDir.add(new File("Main.java", "public class Main {}"));
        root.add(subDir);

        root.display("");
        System.out.println("Total size: " + root.getSize() + " bytes");
    }
}
```

**Output:**
```
📁 root/
  📄 hello.txt (11 bytes)
  📄 readme.md (9 bytes)
  📁 src/
    📄 Main.java (22 bytes)
Total size: 42 bytes
```

---

## Composite vs Decorator

| Property | Decorator | Composite |
|---|---|---|
| Wraps | One object | Collection of objects |
| Purpose | Add behavior at runtime | Treat tree uniformly |
| Example | `ExtraCheeseDecorator(pizza)` | `Directory` containing `File`s |

---

## Follow-up Extensions

### search(name) — Recursive Search

```java
// In FileSystemComponent
public abstract FileSystemComponent search(String name);

// In File
public FileSystemComponent search(String name) {
    return this.name.equals(name) ? this : null;
}

// In Directory
public FileSystemComponent search(String name) {
    if (this.name.equals(name)) return this;
    for (FileSystemComponent child : children) {
        FileSystemComponent result = child.search(name);
        if (result != null) return result;
    }
    return null;
}
```

---

### getSize() with Caching

```java
// In Directory
private int cachedSize = -1;

public void add(FileSystemComponent component) {
    children.add(component);
    cachedSize = -1; // invalidate cache on change
}

@Override
public int getSize() {
    if (cachedSize == -1) {
        cachedSize = children.stream().mapToInt(FileSystemComponent::getSize).sum();
    }
    return cachedSize;
}
```

---

## When to Use Composite Pattern

- You have a **tree structure** (filesystem, org chart, UI component hierarchy)
- You want clients to treat **leaf and composite nodes uniformly**
- Operations need to **propagate recursively** down the tree

## When NOT to Use

- When leaf and composite behavior is fundamentally different and sharing an interface creates confusion
- When the tree is flat and simple — overkill for shallow structures