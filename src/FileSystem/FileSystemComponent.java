package FileSystem;

import java.util.List;

// Component — defines the contract
public abstract class FileSystemComponent {
    protected String name;

    FileSystemComponent(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public abstract int getSize();
    public abstract void display(String indent);
}