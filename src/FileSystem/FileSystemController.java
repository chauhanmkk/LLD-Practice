package FileSystem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileSystemController {
    private final Directory root;

    FileSystemController() {
        root = new Directory("/", null);
    }

    Directory getRoot() {
        return root;
    }

    FileSystemNode mkdir(String path, String name) {
        FileSystemNode parentNode = resolvePath(root, path);
        if (parentNode == null || !parentNode.isDirectory()) {
            throw new IllegalArgumentException("Invalid parent path: " + path);
        }

        Directory parent = (Directory) parentNode;
        Directory newDirectory = new Directory(name, parent);
        parent.addFileNode(newDirectory);
        return newDirectory;
    }

    List<FileSystemNode> ls(FileSystemNode node) {
        if (node == null || !node.isDirectory()) {
            return Collections.emptyList();
        }
        return ((Directory) node).getFilesAndDirectory().values().stream().toList();
    }

    FileSystemNode cd(FileSystemNode currentDirectory, String path) {
        Directory start = path.startsWith("/") ? root : (Directory) currentDirectory;
        return resolvePath(start, path);
    }

    private FileSystemNode resolvePath(FileSystemNode start, String path) {
        if (path == null || path.isEmpty() || ".".equals(path)) {
            return start;
        }
        if ("/".equals(path)) {
            return root;
        }

        FileSystemNode current = path.startsWith("/") ? root : start;
        String[] tokens = Arrays.stream(path.split("/"))
                .filter(token -> !token.isEmpty())
                .toArray(String[]::new);

        for (String token : tokens) {
            if (".".equals(token)) {
                continue;
            }
            if ("..".equals(token)) {
                current = current.parent == null ? current : current.parent;
                continue;
            }
            if (!current.isDirectory()) {
                return null;
            }

            Directory directory = (Directory) current;
            FileSystemNode next = directory.getFilesAndDirectory().get(token);
            if (next == null) {
                return null;
            }
            current = next;
        }
        return current;
    }
}
