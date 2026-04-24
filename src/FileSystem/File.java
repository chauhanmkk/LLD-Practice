package FileSystem;

public class File extends FileSystemNode {
    private String content;

    File(String name, Directory parent) {
        super(name, parent);
    }

    String getContent() {
        return this.content;
    }

    void writeContent(String content) {
        this.content = content;
    }

    @Override
    boolean isDirectory() {
        return false;
    }
}
