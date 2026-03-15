package FileSystem;

import java.util.ArrayList;
import java.util.List;

public class Directory extends FileSystemComponent {
    private List<FileSystemComponent> fileSystems;
    private FileSystemComponent parent;
    Directory(String name, FileSystemComponent parent) {
        super(name);
        fileSystems = new ArrayList<>();
        this.parent = parent;
    }

    public List<FileSystemComponent> getFiles() {
        return this.fileSystems;
    }

    public void addFile(FileSystemComponent file) {
        this.fileSystems.add(file);
    }

    public void removeFile(FileSystemComponent file) {
        this.fileSystems.remove(file);
    }


    @Override
    public int getSize() {
        // total size = sum of all children's sizes
        return fileSystems.stream().mapToInt(FileSystemComponent::getSize).sum();
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + "📁 " + name + "/");
        for (FileSystemComponent child : fileSystems) {
            child.display(indent + "  "); // recursive — works for any depth
        }
    }
}
