package FileSystem;

public class File extends FileSystemComponent {
    private String content;

    File(String name, String content) {
        super(name);
        this.content = content;
    }

    void content() {
        System.out.println(this.content);
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
