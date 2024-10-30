import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;
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
    static boolean touch(File file) throws IOException {
        // Check if the parent directory exists and is a directory
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            return false; // Parent directory does not exist
        }
        if (!file.exists()) {
            return file.createNewFile();
        } else {
            return file.setLastModified(System.currentTimeMillis());
        }
    }
    private static File workingDirectory = new File(System.getProperty("user.dir"));

    static File makeAbsolute(String srcPath) {
        File f = new File(srcPath);
        if (!f.isAbsolute()) {
            f = new File(workingDirectory.getAbsolutePath(), srcPath);
        }
        return f.getAbsoluteFile();
    }
     static class PrintManager{
        private final int NewLineLimit = 8;
        private String buff = "";
        private int newLineCount = 0;
        private PrintStream outputStream = System.out;
        public void setPrintStream(PrintStream p){
            if(outputStream != System.out){
                outputStream.close();
            }
            outputStream = p;
        }

        public  void print(){
            int i;
            for(i = 0;i < buff.length() && newLineCount < NewLineLimit; i++){
                if(buff.charAt(i) == '\n')
                    newLineCount++;
                outputStream.print(buff.charAt(i));
            }
            buff = buff.substring(i);
            if (newLineCount == NewLineLimit)
                outputStream.print("...");
        }
        public void print(String s){
            if(outputStream == System.out) {
                buff += s;
                if (newLineCount < NewLineLimit)
                    print();
            }
            else{
                outputStream.print(s);
            }
        }
        public  void println(String s){
            print(s + System.getProperty("line.separator"));
        }
//         //clears all previous commands in console
//         public void clear(){
//             for(int i = 0;i < 100;i++){
//                 outputStream.println();
//             }
//         }
//         public void printMore(){
//             newLineCount = 0;
//             print();
//         }


    }
     static PrintManager printManager = new PrintManager();
    static void redirectAppendToFile(String command) throws IOException {
        String filePath = command.substring(command.indexOf(">>") + 2).trim();
        //command = command.substring(0, command.indexOf(">>")).trim();
        File file = makeAbsolute(filePath);
        printManager.setPrintStream(new PrintStream(new FileOutputStream(file, true)));
        //printManager.print(command + System.getProperty("line.separator")); // Print the command to the file

    }

    static void redirectOverwriteToFile(String command) throws IOException {
        String filePath = command.substring(command.indexOf(">") + 1).trim();
        command = command.substring(0, command.indexOf(">")).trim();
        File file = makeAbsolute(filePath);
        printManager.setPrintStream(new PrintStream(new FileOutputStream(file, false)));
        printManager.print(command + System.getProperty("line.separator")); // Print the command to the file
    }

    // Moves or renames a file or directory
    static boolean mv(String srcPath, String destPath) throws IOException {
        File src = makeAbsolute(srcPath);
        File dst = makeAbsolute(destPath);
        if (!src.exists()) {
            throw new NoSuchFileException(src.getAbsolutePath(), null, "does not exist.");
        }
        if (dst.isFile()) {
            throw new IOException("Can't move into file.");
        }
        if (!dst.exists()) { // Renaming
            Files.move(src.toPath(), src.toPath().resolveSibling(dst.getName()));
        } else {
            Files.move(src.toPath(), dst.toPath().resolve(src.toPath().getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        return true;
    }

    // Deletes a file given a specific path
    static boolean rm(String srcPath) throws IOException {
        File f = makeAbsolute(srcPath);
        if (!f.exists()) throw new NoSuchFileException(srcPath, null, "does not exist.");
        else if (f.isDirectory()) throw new IOException("Cannot delete directory.");
        else if (!f.delete()) throw new IOException("Cannot delete file.");
        return true;
    }

    // Concatenates a file and returns its content as a String
    static String cat(String f1) throws IOException {
        File file = makeAbsolute(f1);
        if (file.exists()) {
            StringBuilder content = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
            }
            return content.toString().trim();
        } else {
            throw new NoSuchFileException(file.getAbsolutePath(), null, "does not exist");
        }
    }

    // Concatenates the contents of one file to another file
    static boolean cat(String src, String dest) throws IOException {
        File infile = makeAbsolute(src);
        File outfile = makeAbsolute(dest);
        if (!infile.exists() || !outfile.exists()) throw new IOException("No such file exists.");

        try (FileInputStream instream = new FileInputStream(infile);
             FileOutputStream outstream = new FileOutputStream(outfile, true)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = instream.read(buffer)) > 0) {
                outstream.write(buffer, 0, length);
            }
        }
        return true;
    }

    public static void exit() {
        System.exit(0);
    }
}


