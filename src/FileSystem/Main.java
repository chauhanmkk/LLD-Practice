package FileSystem;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        FileSystemController controller = new FileSystemController();

        controller.mkdir("/", "home");
        controller.mkdir("/home", "mohit");
        controller.mkdir("/home/mohit", "docs");
        controller.mkdir("/home/mohit", "projects");

        Directory root = controller.getRoot();
        printListing("Root", controller.ls(root));

        FileSystemNode home = controller.cd(root, "/home");
        printListing("/home", controller.ls(home));

        FileSystemNode mohit = controller.cd(home, "mohit");
        printListing("/home/mohit", controller.ls(mohit));

        FileSystemNode docs = controller.cd(mohit, "./docs");
        System.out.println("cd ./docs -> " + (docs == null ? "null" : docs.getName()));

        FileSystemNode parent = controller.cd(docs, "..");
        System.out.println("cd .. from docs -> " + (parent == null ? "null" : parent.getName()));

        FileSystemNode absolute = controller.cd(mohit, "/home/mohit/projects");
        System.out.println("absolute cd -> " + (absolute == null ? "null" : absolute.getName()));

        FileSystemNode invalid = controller.cd(mohit, "missing");
        System.out.println("cd missing -> " + (invalid == null ? "null" : invalid.getName()));
    }

    private static void printListing(String label, List<FileSystemNode> nodes) {
        System.out.println(label + " contains:");
        for (FileSystemNode node : nodes) {
            String type = node.isDirectory() ? "DIR" : "FILE";
            System.out.println(type + " " + node.getName());
        }
        System.out.println();
    }
}
