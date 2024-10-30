import jdk.internal.classfile.impl.TransformImpl;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class CLItest {
    private static final Path TEMP_DIRECTORY = Paths.get("testFile");
    private static final Path DIRECTORY_NAME = Paths.get("new");
    private static final Path VALID_FILE = TEMP_DIRECTORY.resolve("validFile.txt");
    private static final Path INVALID_FILE = TEMP_DIRECTORY.resolve("invalidFile.txt");
    @BeforeEach
    public void setUp() throws IOException {
        // Ensure the directory structure is created before each test
        Files.createDirectories(TEMP_DIRECTORY);
        Files.createFile(TEMP_DIRECTORY.resolve("file1.txt"));
        Files.createFile(TEMP_DIRECTORY.resolve("file2.txt"));
        Files.createFile(TEMP_DIRECTORY.resolve(".hiddenfile"));
    }
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
        assertTrue("Directory does not exist", Files.exists(newDirectory));
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

        assertTrue("Output should contain file1.txt", output.contains("file1.txt"));
        assertTrue("Output should contain file2.txt", output.contains("file2.txt"));
        assertFalse("Output should not contain hidden files", output.contains(".hiddenfile"));
        String[] lines = output.split("\n");
        assertEquals("Second file should be file1.txt in reverse order", "file1.txt", lines[0]);
        assertEquals("First file should be file2.txt in reverse order", "file2.txt", lines[1]);

    }

    @Test
    public void lsAllTest() {
        String output = CLI.lsAll(TEMP_DIRECTORY.toString());

        assertTrue("Output should contain file1.txt", output.contains("file1.txt"));
        assertTrue("Output should contain file2.txt", output.contains("file2.txt"));
        assertTrue("Output should contain .hiddenfile", output.contains(".hiddenfile"));
    }

    @Test
    public void lsReverseTest() {
        String output = CLI.lsReverse(TEMP_DIRECTORY.toString());

        String[] lines = output.split("\n");
        assertEquals("First file should be file2.txt in reverse order", "file2.txt", lines[0]);
        assertEquals("Second file should be file1.txt in reverse order", "file1.txt", lines[1]);
    }

    @Test
    public void lsInvalidDirectoryTest() {
        String output = CLI.ls("invalid/directory/path");

        assertEquals("Directory does not exist.", output);
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

        assertFalse("File should not be created because the parent directory does not exist", result);
        assertFalse("File should not exist", file.exists());
    }

}

