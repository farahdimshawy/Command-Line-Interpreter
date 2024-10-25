import java.util.Scanner;
import java.io.*;

public class Main {
    static void pwd(){
        System.out.println(System.getProperty("user.dir"));
    }
    static void mkdir(String directory_name){
        File    directory;       // Desired current working directory
        directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs())
        {
            System.setProperty("user.dir", directory.getAbsolutePath());
        }
    }
    public static void main(String[] args) {
        pwd();
        mkdir("file3");
        pwd();
    }
}