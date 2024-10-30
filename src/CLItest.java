import jdk.internal.classfile.impl.TransformImpl;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CLItest {
    private static final Path TEMP_DIRECTORY = Paths.get("testFolder");
    private static final Path DIRECTORY_NAME = Paths.get("newDir");
    private static final Path VALID_FILE = TEMP_DIRECTORY.resolve("validFile.txt");
    private static final Path INVALID_FILE = TEMP_DIRECTORY.resolve("invalidFile.txt");
    private static final Path FILE2 = TEMP_DIRECTORY.resolve("file2.txt");
    private static final Path FILE1 = TEMP_DIRECTORY.resolve("file1.txt");
    private static final Path SOURCE_FILE = TEMP_DIRECTORY.resolve("sourceFile.txt");
    private static final Path MOVE_FILE = TEMP_DIRECTORY.resolve("MOVEFile.txt");
    private static final Path TARGET_FILE = TEMP_DIRECTORY.resolve("targetFile.txt");
    private static final Path REDIRECT_FILE = TEMP_DIRECTORY.resolve("redirectFile.txt");

    @BeforeEach
    public void setUp() throws IOException {
        // Ensure the directory structure is created before each test
        Files.createDirectories(TEMP_DIRECTORY);
        createFile(VALID_FILE);
        createFile(INVALID_FILE);
        createFile(SOURCE_FILE);
        createFile(MOVE_FILE);
        createFile(TARGET_FILE);
        createFile(REDIRECT_FILE);
        createFile(FILE1);
        createFile(FILE2);
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
        assertTrue(Files.exists(SOURCE_FILE));
        assertTrue(Files.exists(MOVE_FILE));
        assertTrue(Files.exists(TARGET_FILE));
        assertTrue(Files.exists(REDIRECT_FILE));
    }
 //   @BeforeEach
//    public void setUp() throws IOException {
//        // Ensure the directory structure is created before each test
//        Files.createDirectories(TEMP_DIRECTORY);
//        Files.createFile(TEMP_DIRECTORY.resolve("file1.txt"));
//        Files.createFile(TEMP_DIRECTORY.resolve("file2.txt"));
//        Files.createFile(TEMP_DIRECTORY.resolve("sourceFile.txt"));
//        Files.createFile(TEMP_DIRECTORY.resolve("MOVEFile.txt"));
//        Files.createFile(TEMP_DIRECTORY.resolve(".hiddenfile"));
//        Files.writeString(SOURCE_FILE, "Sample content for testing");
//        if (!Files.exists(REDIRECT_FILE)) {
//            Files.createFile(REDIRECT_FILE);
//            Files.writeString(REDIRECT_FILE, "Initial content\n");
//        }
//    }
    @Test
    public void rmdirTest() throws IOException {
        Path pathToBeDeleted = TEMP_DIRECTORY.resolve(DIRECTORY_NAME);

        boolean result = CLI.rmdir(pathToBeDeleted.toFile());

        assertTrue(result);
        assertFalse(
                "Directory still exists",
                Files.exists(pathToBeDeleted));
    }
    @Test
    public void mkdirTest() throws IOException {
        Path newDirectory = TEMP_DIRECTORY.resolve("newDir");

        boolean result = CLI.mkdir(newDirectory.toFile());

        assertTrue("Failed to create the directory", result);
        assertTrue("This directory doesn't exist. Please try again", Files.exists(newDirectory));
    }
    @Test
    public void pwdTest() throws IOException {
        String currentDirectory = CLI.pwd();

        // Get the expected current directory using the Java NIO Paths class
        String expectedDirectory = Paths.get("").toAbsolutePath().toString();

        // Assert that the current directory is equal to the expected directory
        assertEquals("The current directory should match the expected path", expectedDirectory, currentDirectory);

    }
    @Test
    public void lsTest() {
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
    public void lsReverseTest() {
        String output = CLI.lsReverse(TEMP_DIRECTORY.toString());
        File directory = new File(TEMP_DIRECTORY.toString());
        String[] dir = directory.list();
        assert dir != null;
        Arrays.sort(dir);
        String[] lines = output.split("\n");
        assertEquals("First file should be file2.txt in reverse order", dir[dir.length-1], lines[0]);
        assertEquals("Second file should be file1.txt in reverse order", dir[dir.length-2], lines[1]);
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
        boolean result = CLI.touch(file);

        assertTrue("File should be created successfully", result);
        assertTrue("File should exist after creation", file.exists());
    }

    @Test
    public void testTouchUpdateFile() throws IOException {
        // Create a file first
        File file = VALID_FILE.toFile();
        file.createNewFile();

        // Update the file's last modified time
        boolean result = CLI.touch(file);

        assertTrue("File should be updated successfully", result);
        assertTrue("File should exist after update", file.exists());
        assertTrue("File's last modified time should be updated", file.lastModified() > System.currentTimeMillis() - 1000);
    }

    @Test
    public void testTouchMissingParentDirectory() throws IOException {
        // Test creating a file with a missing parent directory
        File file = INVALID_FILE.toFile();
        boolean result = CLI.touch(file);

        //assertFalse("File should not be created because the parent This directory doesn't exist. Please try again", result);
        assertFalse("File should not exist", file.exists());
    }
    @Test
    public void testRedirectAppendToFile() throws IOException {
        String command = "echo 'Appending content' >> " + REDIRECT_FILE.toFile().getPath();
        CLI.redirectAppendToFile(command);

        // Simulate print manager output
        CLI.printManager.println("Appending content");

        // Verify that the content was appended
        String fileContent = Files.readString(REDIRECT_FILE);
        assertTrue( "Content was not appended correctly", fileContent.contains("Appending content"));
    }

    @Test
    public void testRedirectOverwriteToFile() throws IOException {
        String command = "Hello, World!\n";
        CLI.redirectOverwriteToFile(command + " > " + REDIRECT_FILE.toFile().getPath());

        // Verify that the content was overwritten
        String fileContent = Files.readString(REDIRECT_FILE);
//        System.out.println("File content: '" + fileContent + "'");
        assertEquals("Content was not overwritten correctly",  fileContent, "Hello, World!\n");
    }





@Test
public void mvTest() throws IOException {
    // Ensure the target file does not exist for a clean move operation
    if (Files.exists(TARGET_FILE)) {
        Files.delete(TARGET_FILE);
    }

    // Move the source file to the target location
    boolean result = CLI.mv(MOVE_FILE.toFile().getPath(), TARGET_FILE.toFile().getPath());

    assertTrue("File move failed", result );
    assertFalse( "Source file should no longer exist", Files.exists(MOVE_FILE));
    assertTrue( "Target file does not exist", Files.exists(TARGET_FILE));
}
    @Test
    public void mvrenameTest() throws IOException {
        // Ensure source file exists
        if (!Files.exists(SOURCE_FILE)) {
            Files.createFile(SOURCE_FILE);
        }

        // Define a new path in the same directory but with a different filename
        Path renamedFile = TEMP_DIRECTORY.resolve("renamedFile.txt");

        // Ensure the renamed file does not already exist
        if (Files.exists(renamedFile)) {
            Files.delete(renamedFile);
        }

        // Attempt to rename the source file
        boolean result = CLI.mv(SOURCE_FILE.toFile().getPath(), renamedFile.toFile().getPath());

        // Verify that the rename operation was successful
        assertTrue("File rename failed", result );
        assertFalse( "Original file should no longer exist", Files.exists(SOURCE_FILE));
        assertTrue( "Renamed file does not exist", Files.exists(renamedFile));
    }



   
@Test
public void rmTest() throws IOException {
    // Ensure the target file exists for the test
    if (!Files.exists(TARGET_FILE)) {
        Files.createFile(TARGET_FILE);
    }

    boolean result = CLI.rm(TARGET_FILE.toFile().getPath());

    assertTrue("File removal failed", result);
    assertFalse( "File should have been deleted", Files.exists(TARGET_FILE));
}



    @Test
    public void catSingleParameterTest() throws IOException {
        // Test the `cat` function with one parameter to read content
        String content = CLI.cat(SOURCE_FILE.toFile().getPath());

        assertNotNull( "Content should not be null", content);
        assertEquals(  "Content should match file content","Sample content for testing", content );
    }

    @Test
    public void catTwoParametersTest() throws IOException {
        // Create a target file where the content will be appended
        Files.writeString(TARGET_FILE, "Existing content\n");

        // Use the `cat` function with two parameters to concatenate content from SOURCE_FILE to TARGET_FILE
        boolean result = CLI.cat(SOURCE_FILE.toFile().getPath(), TARGET_FILE.toFile().getPath());

        // Verify the operation was successful
        assertTrue( "Concatenation should succeed", result);

        // Read the content of the target file to verify concatenation
        String targetContent = Files.readString(TARGET_FILE);
        assertEquals("Existing content\nSample content for testing", targetContent, "Content should be appended to the target file");
    }

}

