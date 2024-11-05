import java.util.Scanner;
import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        CLI.cd("testFolder");
        System.out.println(CLI.pwd());
        CLI.piping("cat pipingFile.txt | cd");
        System.out.println(CLI.pwd());

    }
}