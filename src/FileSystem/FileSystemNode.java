package FileSystem;

public abstract class FileSystemNode {
    protected final String name;
    protected final Directory parent;

    FileSystemNode(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
    }

    abstract boolean isDirectory();

    String getName() {
        return name;
    }

    Directory getParent() {
        return parent;
    }
}
