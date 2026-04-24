package FileSystem;

import java.util.HashMap;
import java.util.Map;

public class Directory extends FileSystemNode {

    private final Map<String, FileSystemNode> filesNodes;

    Directory(String name, Directory parent) {
        super(name, parent);
        filesNodes = new HashMap<>();
    }

    @Override
    boolean isDirectory() {
        return true;
    }

    Map<String, FileSystemNode> getFilesAndDirectory() {
        return this.filesNodes;
    }

    void addFileNode(FileSystemNode node) {
        filesNodes.putIfAbsent(node.name, node);
    }

}
