import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CLI {
    public static File workingDirectory = new File(System.getProperty("user.dir"));

    public CLI() {
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

    // print working directory
    static String pwd() {
        return workingDirectory.getAbsolutePath();
    }

    static File makeAbsolute(String srcPath) {
        File file = new File(srcPath);
        // Check if the path is already absolute
        if (!file.isAbsolute()) {
            // Convert to an absolute path using the current working directory
            file = new File(workingDirectory, srcPath);
        }
        return file.getAbsoluteFile();
    }

    // change directory
    static String cd(String directoryPath) {

        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            directoryPath = workingDirectory.getParentFile().toString();
        } else if (directoryPath.equals("~")) {
            directoryPath = workingDirectory.getParentFile().toString();
        }

        File newDirectory;

        if (directoryPath.equals("..")) {
            newDirectory = workingDirectory.getParentFile();
            if (newDirectory == null) {
                return "Already at the root directory.";
            }
        } else {
            newDirectory = makeAbsolute(directoryPath);
        }

        if (!newDirectory.exists() || !newDirectory.isDirectory()) {
            return "This directory doesn't exist. Please try again.";
        }

        workingDirectory = newDirectory;
        return pwd(); // Return the new working directory path
    }


    // create directory
    static boolean mkdir(String d) {
        File directory = new File(workingDirectory + File.separator + d);
        return directory.exists() || directory.mkdirs();
    }

    // remove directory
    static boolean rmdir(String d) {
        File directoryToBeDeleted = new File(workingDirectory + File.separator + d);
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                rmdir(file.toString());
            }
        }
        return directoryToBeDeleted.delete();
    }

    static String ls(String directoryPath) {
        return listDirectory(new File(workingDirectory + File.separator + directoryPath), false, false);
    }

    static String lsAll(String directoryPath) {
        return listDirectory(new File(workingDirectory + File.separator + directoryPath), true, false);
    }

    static String lsReverse(String directoryPath) {
        return listDirectory(new File(workingDirectory + File.separator + directoryPath), false, true);
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

    static boolean touch(String file) throws IOException {
        // Check if the parent directory exists and is a directory
        File file2 = new File(workingDirectory + File.separator + file);
        if (file2.getParentFile() != null && !file2.getParentFile().exists()) {
            return false; // Parent directory does not exist
        }
        if (!file2.exists()) {
            return file2.createNewFile();
        } else {
            return file2.setLastModified(System.currentTimeMillis());
        }
    }

    static class PrintManager {
        private final int NewLineLimit = 8;
        private String buff = "";
        private int newLineCount = 0;
        private PrintStream outputStream = System.out;

        public void setPrintStream(PrintStream p) {
            if (outputStream != System.out) {
                outputStream.close();
            }
            outputStream = p;
        }

        public void print() {
            int i;
            for (i = 0; i < buff.length() && newLineCount < NewLineLimit; i++) {
                if (buff.charAt(i) == '\n')
                    newLineCount++;
                outputStream.print(buff.charAt(i));
            }
            buff = buff.substring(i);
            if (newLineCount == NewLineLimit)
                outputStream.print("...");
        }

        public void print(String s) {
            if (outputStream == System.out) {
                buff += s;
                if (newLineCount < NewLineLimit)
                    print();
            } else {
                outputStream.print(s);
            }
        }

        public void println(String s) {
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
        String commands = command;
        String filePath = commands;
        //command = command.substring(0, command.indexOf(">>")).trim();
        File file = makeAbsolute(filePath);
        printManager.setPrintStream(new PrintStream(new FileOutputStream(file, true)));
    }

    // >
    static void redirectOverwriteToFile(String command) throws IOException {
        String filePath = command.substring(command.indexOf(">") + 1).trim();
        command = command.substring(0, command.indexOf(">")).trim();
        File file = makeAbsolute(filePath);
        printManager.setPrintStream(new PrintStream(new FileOutputStream(file, false)));
        printManager.print(command); // Print the command to the file
    }

    // Moves or renames a file or directory
    static boolean mv(String srcPath, String destPath) throws IOException {
        File src = makeAbsolute(srcPath);
        File dst = makeAbsolute(destPath);
        if (!src.exists()) {
            throw new NoSuchFileException(src.getAbsolutePath(), null, "does not exist.");
        }
        if (dst.isFile()) {
            //throw new IOException("Can't move into file.");
            Files.move(src.toPath(), dst.toPath().resolveSibling(dst.getName()));
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
    static String[] cat(String src, String dest) throws IOException {
        File file = makeAbsolute(src);
        File file1 = makeAbsolute(dest);
        StringBuilder content = new StringBuilder();
        StringBuilder content1 = new StringBuilder();


        if (file.exists()) {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
            }

        } else {
            throw new NoSuchFileException(file.getAbsolutePath(), null, "does not exist");
        }
        if (file1.exists()) {
            try (BufferedReader in = new BufferedReader(new FileReader(file1))) {
                String line;
                while ((line = in.readLine()) != null) {
                    content1.append(line).append(System.lineSeparator());
                }
            }
        } else {
            throw new NoSuchFileException(file1.getAbsolutePath(), null, "does not exist");
        }
        return new String[]{content.toString().trim(), content1.toString().trim()};
    }

    // sort (to test |)
    static String[] sort(String lines) {
        String[] arrayOfStrings = lines.split("\n");
        Arrays.sort(arrayOfStrings, String.CASE_INSENSITIVE_ORDER);
        return arrayOfStrings;
    }

    // |
    static Object piping(String command) throws IOException {
        // Split commands by | character
        String[] commands = command.split("\\|");
        String output = null; // To hold the output of each command

        for (int i = 0; i < commands.length; i++) {
            String cmd = commands[i].trim(); // Trim whitespace
            String[] arrayOfStrings = cmd.split("\\s+"); // Split by whitespace

            if (arrayOfStrings.length == 0) {
                continue; // Skip empty commands
            }

            // Process each command
            if (arrayOfStrings[0].equals("pwd")) {
                output = pwd(); // Get output from pwd
                System.out.println(output); // Print output
            } else if (arrayOfStrings[0].equals("ls")) {
                // Determine the filename or use output from the previous command
                String param = (arrayOfStrings.length > 1) ? arrayOfStrings[1] : output; // Get the first parameter if specified

                if (arrayOfStrings.length > 2 && arrayOfStrings[2].equals("-r")) {
                    output = lsReverse(param); // Use param directly or previous output
                } else if (arrayOfStrings.length > 2 && arrayOfStrings[2].equals("-a")) {
                    output = lsAll(param); // Use param directly or previous output
                } else {
                    output = ls(param); // Pass the filename or null
                }
            } else if (arrayOfStrings[0].equals("cat")) {
                String param; // To hold the filename or previous output

                // Use the first parameter directly from the command if specified
                if (arrayOfStrings.length > 1) {
                    param = arrayOfStrings[1]; // Get filename if specified
                } else {
                    param = output; // Use the previous command's output if filename not specified
                }

                if (param != null) {
                    output = cat(param); // Pass the filename or output as a parameter
                } else {
                    System.out.println("Error: No file specified for cat command.");
                }
            } else if (arrayOfStrings[0].equals("sort")) {
                String param = output; // Use previous output as input for sort
                if (param != null) {
                    String[] sortedArray = sort(param); // Call the sort function
                    output = String.join("\n", sortedArray); // Join the sorted array into a single string with line breaks
                } else {
                    output = "Error: No input provided for sort command.";
                }
            } else if (arrayOfStrings[0].equals("cd")) {
                cd(output);
            } else {
                System.out.println("Unknown command: " + arrayOfStrings[0]);
            }

        }
        return output;
    }

    public static void exit() {
        System.exit(0);
    }

    static void help() {
        help("");
    }

    static void help(String c) {
        if (c.isEmpty()) {
            System.out.println("Available commands:");
            for (Map.Entry<String, String> entry : commands.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } else {
            String description = commands.get(c);
            if (description != null) {
                System.out.println(c + ": " + description);
            } else {
                System.out.println("Command not found: " + c);
            }
        }
    }
}


