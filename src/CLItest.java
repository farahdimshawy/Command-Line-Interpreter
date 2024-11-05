import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CLItest {
    private static final Path WORKING_DIRECTORY = CLI.workingDirectory.toPath();
    private static final Path TEMP_DIRECTORY = Paths.get( "testFolder");
    private static final Path DIRECTORY_NAME = Paths.get("newFolder");
    private static final Path VALID_FILE = TEMP_DIRECTORY.resolve("validFile.txt");
    private static final Path INVALID_FILE = TEMP_DIRECTORY.resolve("invalidFile.txt");
    private static final Path F2 = TEMP_DIRECTORY.resolve("F2.txt");
    private static final Path F1 = TEMP_DIRECTORY.resolve("F1.txt");
    private static final Path SF = TEMP_DIRECTORY.resolve("sf.txt");
    private static final Path MF = TEMP_DIRECTORY.resolve("moveFile");
    private static final Path NEW = TEMP_DIRECTORY.resolve("new.txt");
    private static final Path MOVE_DIR = TEMP_DIRECTORY.resolve("moveDir");
    private static final Path TARGET_FILE = TEMP_DIRECTORY.resolve("targetFile.txt");
    private static final Path REDIRECT_FILE = TEMP_DIRECTORY.resolve("redirectFile.txt");

    private static final Path REMOVE_FILE = TEMP_DIRECTORY.resolve("removeFile.txt");
    private static final Path APPENDING_FILE = TEMP_DIRECTORY.resolve("appendingFile.txt");
    private static final Path CAT_FILE = TEMP_DIRECTORY.resolve("catFile.txt");
    private static final Path CAT2_FILE = TEMP_DIRECTORY.resolve("cat2.txt");
    private static final Path PIPE_FILE = TEMP_DIRECTORY.resolve("pipe.txt");


    @BeforeEach
    public void setUp() throws IOException {
        Files.walk(TEMP_DIRECTORY)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        // Ensure the directory structure is created before each test
        Files.createDirectories(TEMP_DIRECTORY);
        Files.createDirectories(DIRECTORY_NAME);
        createFile(VALID_FILE);
        createFile(INVALID_FILE);
        //createFile(SF);
        //createFile(MF);
        createFile(TARGET_FILE);
        createFile(REDIRECT_FILE);
        //createFile(F1);
        //createFile(F2);
        //createFile(PIPE_FILE);
    }

    private void createFile(Path file) {
        try {
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFilesExist() {
        assertTrue(Files.exists(VALID_FILE));
        assertTrue(Files.exists(INVALID_FILE));
        //assertTrue(Files.exists(SF));
        //assertTrue(Files.exists(MF));
        assertTrue(Files.exists(TARGET_FILE));
        assertTrue(Files.exists(REDIRECT_FILE));
        assertTrue(Files.exists(PIPE_FILE));
    }

    @Test
    public void testCdToValidAbsoluteDirectory() {
        String validAbsolutePath = System.getProperty("user.home");
        String result = CLI.cd(validAbsolutePath);

        assertEquals("The cd command should change to the specified absolute path.", validAbsolutePath, result);
    }

    @Test
    public void testCdToInvalidDirectory() {
        String invalidPath = "/nonexistentpath";
        String result = CLI.cd(invalidPath);

        assertEquals("The cd command should return an error for a nonexistent path.", "This directory doesn't exist. Please try again.", result);
    }

    @Test
    public void testCdToRelativeDirectory() {
        String initialDirectory = CLI.pwd();
        String result = CLI.cd("testdir");

        assertEquals("The cd command should change to the relative path correctly.", initialDirectory + File.separator + "testdir", result);
    }

    @Test
    public void testCdBackToPreviousDirectory() {
        String initialDirectory = CLI.pwd();
        CLI.mkdir(initialDirectory + File.separator + "testdir");

        // Move to a new directory
        CLI.cd("testdir");

        // Move back to the initial directory using ".."
        String result = CLI.cd("..");

        assertEquals("The cd command should return to the parent directory when using '..'.", initialDirectory, result);
    }

    @Test
    public void testPwdAfterCd() {
        // Ensure `pwd` returns the correct directory after using `cd`
        String homeDirectory = System.getProperty("user.home");
        CLI.cd(homeDirectory);

        assertEquals("The pwd command should return the correct current directory after changing it with cd.", homeDirectory, CLI.pwd());
    }

    @Test
    public void testCdToHomeDirectoryUsingTilde() {
        String initialDirectory = CLI.pwd();

        String result = CLI.cd("~");

        String expectedHomeDirectory = System.getProperty("user.home");

        assertEquals("The cd command should change to the home directory using ~.", expectedHomeDirectory, result);
    }

    @Test
    public void testCdToHomeDirectoryUsingEmptyString() {
        String initialDirectory = CLI.pwd();

        String result = CLI.cd("");

        String expectedHomeDirectory = System.getProperty("user.dir");

        assertEquals("The cd command should change to the home directory using an empty string.", expectedHomeDirectory, result);
    }


    @Test
    public void mkdirTest() {
        Path newDirectory = TEMP_DIRECTORY.resolve(DIRECTORY_NAME);

        boolean result = CLI.mkdir("testFolder/newFolder");

        assertTrue("Failed to create the directory", result);
        assertTrue("This directory doesn't exist. Please try again", Files.exists(newDirectory));
    }

    @Test
    public void lsAllTest() {
        String output = CLI.lsAll(TEMP_DIRECTORY.toString());
        File directory = new File(TEMP_DIRECTORY.toString());
        String[] dir = directory.list();
        assert dir != null;
        Arrays.sort(dir);
        assertTrue("Output should contain", output.contains(dir[0]));
        assertTrue("Output should contain", output.contains(dir[1]));
        assertTrue("Output should contain .hiddenfile", output.contains(".hiddenfile"));
    }

    @Test
    public void rmdirTest() {
        Path pathToBeDeleted = TEMP_DIRECTORY.resolve(DIRECTORY_NAME);

        boolean result = CLI.rmdir(pathToBeDeleted.toString());

        assertTrue(result);
        assertFalse("Directory still exists" + pathToBeDeleted, Files.exists(pathToBeDeleted));
    }

    @Test
    public void pwdTest() {

        //CLI cmd = new CLI();

        // Get the current working directory using the pwd method
        String currentDir = CLI.pwd();

        // Get the expected current directory using the Java NIO Paths class
        String expectedDirectory = System.getProperty("user.dir");

        // Assert that the current directory is equal to the expected directory
        assertEquals("The current directory should match the expected path", expectedDirectory, currentDir);

    }

    @Test
    public void lsTest() {
        //hidden file changes dir index +1
        String output = CLI.ls(TEMP_DIRECTORY.toString());
        File directory = new File(TEMP_DIRECTORY.toString());
        String[] dir = directory.list();
        assert dir != null;
        Arrays.sort(dir);
        assertTrue("Output should contain", output.contains(dir[1]));
        assertTrue("Output should contain ", output.contains(dir[2]));
        assertFalse("Output should not contain hidden files", output.contains(".hiddenfile"));
        String[] lines = output.split("\n");
        assertEquals("Second file ", dir[1], lines[0]);
        assertEquals("First file ", dir[2], lines[1]);

    }

    @Test
    public void lsReverseTest() {
        String output = CLI.lsReverse(TEMP_DIRECTORY.toString());
        File directory = new File(TEMP_DIRECTORY.toString());
        String[] dir = directory.list();
        assert dir != null;
        Arrays.sort(dir);
        String[] lines = output.split("\n");
        assertEquals("First file should be F2.txt in reverse order", dir[dir.length - 1], lines[0]);
        assertEquals("Second file should be F1.txt in reverse order", dir[dir.length - 2], lines[1]);
    }

    @Test
    public void lsInvalidDirectoryTest() {
        String output = CLI.ls("invalid/directory/path");

        assertEquals("This directory doesn't exist. Please try again.", output);
    }

    @Test
    public void testTouchCreateFile() throws IOException {
        // Test creating a new file
        File file = VALID_FILE.toFile();
        boolean result = CLI.touch(file.toString());

        assertTrue("File should be created successfully", result);
        assertTrue("File should exist after creation", file.exists());
    }

    @Test
    public void testTouchUpdateFile() throws IOException {
        // Create a file first
        File file = VALID_FILE.toFile();
        file.createNewFile();

        // Update the file's last modified time
        boolean result = CLI.touch(file.toString());

        assertTrue("File should be updated successfully", result);
        assertTrue("File should exist after update", file.exists());
        assertTrue("File's last modified time should be updated", file.lastModified() > System.currentTimeMillis() - 1000);
    }


    @Test
    public void testRedirectAppendToFile() throws IOException {
        String command = "echo 'Appending content'  " + APPENDING_FILE.toFile().getPath();
        CLI.redirectAppendToFile(command);

        // Simulate print manager output
        CLI.printManager.println("Appending content");

        // Verify that the content was appended
        String fileContent = Files.readString(APPENDING_FILE);
        assertTrue("Content was not appended correctly", fileContent.contains("Appending content"));
    }

    @Test
    public void testRedirectOverwriteToFile() throws IOException {
        String command = "Hello, World!\n";
        CLI.redirectOverwriteToFile(command + " > " + REDIRECT_FILE.toFile().getPath());

        // Verify that the content was overwritten
        String fileContent = Files.readString(REDIRECT_FILE);
//        System.out.println("File content: '" + fileContent + "'");
        assertEquals("Content was not overwritten correctly", fileContent, "Hello, World!\n");
    }


    @Test
    public void mvTest() throws IOException {
        assertTrue("Source file does not exist", Files.exists(MF));

        // Move the source file to the target location
        boolean result = CLI.mv(MF.toFile().getPath(), MOVE_DIR.toFile().getPath());

        // Assert that the move operation was successful
        assertTrue("File move failed", result);

        // Assert that the source file no longer exists
        assertFalse("Source file should no longer exist", Files.exists(MF));

        // Assert that the target file now exists
        assertTrue("Target file does not exist", Files.exists(MOVE_DIR));
    }

    @Test
    public void mvrenameTest() throws IOException {
        // Ensure source file exists
        if (!Files.exists(SF)) {
            Files.createFile(SF);
        }

        // Define a new path in the same directory but with a different filename
        Path renamedFile = TEMP_DIRECTORY.resolve("renamedFile.txt");

        // Ensure the renamed file does not already exist
        if (Files.exists(renamedFile)) {
            Files.delete(renamedFile);
        }

        // Attempt to rename the source file
        boolean result = CLI.mv(SF.toFile().getPath(), renamedFile.toFile().getPath());

        // Verify that the rename operation was successful
        assertTrue("File rename failed", result);
        assertFalse("Original file should no longer exist", Files.exists(SF));
        assertTrue("Renamed file does not exist", Files.exists(renamedFile));
    }


    @Test
    public void rmTest() throws IOException {
        // Ensure the target file exists for the test
        if (!Files.exists(REMOVE_FILE)) {
            Files.createFile(REMOVE_FILE);
        }

        boolean result = CLI.rm(REMOVE_FILE.toFile().getPath());

        assertTrue("File removal failed", result);
        assertFalse("File should have been deleted", Files.exists(REMOVE_FILE));
    }


    @Test
    public void catSingleParameterTest() throws IOException {
        // Test the `cat` function with one parameter to read content
        String content = CLI.cat(CAT_FILE.toFile().getPath());

        assertNotNull("Content should not be null", content);
        assertEquals("Content should match file content", "Sample content for testing", content);
    }

    @Test
    public void catTwoParametersTest() throws IOException {
        // Create a target file where the content will be appended
        Files.writeString(TARGET_FILE, "Existing content\n");

        // Use the `cat` function with two parameters to concatenate content from SF to TARGET_FILE
        boolean result = CLI.cat(CAT2_FILE.toFile().getPath(), TARGET_FILE.toFile().getPath());

        // Verify the operation was successful
        assertTrue("Concatenation should succeed", result);

        // Read the content of the target file to verify concatenation
        String targetContent = Files.readString(TARGET_FILE);
        assertEquals("Content should be appended to the target file", "Existing content\nSample content for testing", targetContent);
    }

    @Test
    public void pipingTest() throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(String.valueOf(PIPE_FILE)))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line); // Add each line to the list
            }
        }

        // Convert the List to an array
        String[] linesArray = lines.toArray(new String[0]);
        // Sort the array
        Arrays.sort(linesArray);
        String check = String.join("\n", linesArray);
        assertEquals(check, CLI.piping("cat " + CLI.pwdTests() + "/testFolder/pipe.txt | sort"));
    }
}

