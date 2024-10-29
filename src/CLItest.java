import jdk.internal.classfile.impl.TransformImpl;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class CLItest {
    private static final Path TEMP_DIRECTORY = Paths.get("testFile");
    private static final Path DIRECTORY_NAME = Paths.get("new");
    @BeforeEach
    public void setUp() throws IOException {
        // Ensure the directory structure is created before each test
        Files.createDirectories(TEMP_DIRECTORY);
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
}
