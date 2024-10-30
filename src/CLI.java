import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CLI {
    public CLI() {}
    static String pwd(){
        return (System.getProperty("user.dir"));
    }
    static boolean mkdir(File directory){
        return directory.exists() || directory.mkdirs();
    }
    private static final Map<String, String> commands = new HashMap<String, String>() {{
        put("pwd", "Print Working Directory - Displays the current directory path.");
        put("cd", "Change Directory - Changes the current directory.");
        put("ls", "List - Lists files and directories in the current directory.");
        put("ls -a", "List All - Lists all files, including hidden files.");
        put("ls -r", "List Reverse - Lists files in reverse order.");
        put("mkdir", "Make Directory - Creates a new directory.");
        put("rmdir", "Remove Directory - Deletes an empty directory.");
        put("touch", "Touch - Creates an empty file or updates the timestamp of an existing file.");
        put("mv", "Move - Moves or renames files and directories.");
        put("rm", "Remove - Deletes files or directories.");
        put("cat", "Concatenate - Displays the contents of a file.");
        put(">", "Redirect Output - Redirects command output to a file, overwriting the file.");
        put(">>", "Append Output - Redirects command output to a file, appending to the file.");
        put("|", "Pipe - Passes the output of one command as input to another command.");
    }};
    static void help(){
        help("");
    }
    static void help(String c){
        if (c.isEmpty()) {
            System.out.println("Available commands:");
            for (Map.Entry<String, String> entry : commands.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
        else{
            String description = commands.get(c);
            if (description != null) {
                System.out.println(c + ": " + description);
            } else {
                System.out.println("Command not found: " + c);
            }
        }
    }

    static boolean rmdir(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                rmdir(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
    static String ls(String directoryPath) {
        return listDirectory(new File(directoryPath), false, false);
    }

    static String lsAll(String directoryPath) {
        return listDirectory(new File(directoryPath), true, false);
    }

    static String lsReverse(String directoryPath) {
        return listDirectory(new File(directoryPath), false, true);
    }

    private static String listDirectory(File directory, boolean showHidden, boolean reverse) {
        if (!directory.exists() || !directory.isDirectory()) {
            return "This directory doesn't exist. Please try again.";
        }

        File[] files = directory.listFiles();
        if (files == null) return "";

        Arrays.sort(files, reverse ? Comparator.reverseOrder() : Comparator.naturalOrder());

        StringBuilder output = new StringBuilder();
        for (File file : files) {
            if (showHidden || !file.isHidden()) {
                output.append(file.getName()).append("\n");
            }
        }
        return output.toString().trim();
    }
}


