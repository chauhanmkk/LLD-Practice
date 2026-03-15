package FileSystem;

public class Main {

    static void main() {
        Directory root = new Directory("root", null);

        File f1 = new File("hello.txt", "Hello World");
        File f2 = new File("readme.md", "Read this");
        root.addFile(f1);
        root.addFile(f2);

        Directory subDir = new Directory("src", root);
        subDir.addFile(new File("Main.java", "public class Main {}"));
        root.addFile(subDir);

        root.display("");
        System.out.println("Total size: " + root.getSize() + " bytes");
    }
}