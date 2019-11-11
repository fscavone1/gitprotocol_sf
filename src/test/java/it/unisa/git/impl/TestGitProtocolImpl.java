package it.unisa.git.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestGitProtocolImpl {

    private static final String PATH = "resources/files/";
    private static final String REPOSITORY_TEST = "repo_test";
    private static final String COMMIT_TEST = "Some text for commit";
    private static final String TEXT_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
    private static final String TEXT_2 = "Praesent egestas dolor sapien, sed sagittis nisl condimentum non.";
    private static final String TEXT_3 = "Quisque rutrum quam in enim pulvinar, ut fermentum ante commodo.";

    public static void main(String[] args) throws Exception {

        GitProtocolImpl test = new GitProtocolImpl(0, "127.0.0.1");

        File dir = new File(PATH);
        boolean testRep = test.createRepository(REPOSITORY_TEST, dir);

        System.out.println("TEST CREATE 1: " + testRep);

       if(testRep)
            dir.mkdirs();

        List<File> files = new ArrayList<File>();
        for(int i = 1; i<5; i++){
            File f = new File(dir,i+".txt");
            FileOutputStream stream = new FileOutputStream(f);
            PrintStream printStream = new PrintStream(stream);

            printStream.println(TEXT_1);
            //printStream.println(TEXT_2);
            //printStream.println(TEXT_3);

            files.add(f);
        }

        //Thread.sleep(5000);

        boolean testAdd = test.addFilesToRepository(REPOSITORY_TEST, files);

        System.out.println("TEST ADD 1: " + testAdd);

        File f = new File(dir, "1.txt");

        FileOutputStream stream = new FileOutputStream(f);
        PrintStream printStream = new PrintStream(stream);

        printStream.println(TEXT_2);

        List<File> files1 = new ArrayList<>();
        files1.add(f);

        //Thread.sleep(5000);

        test.addFilesToRepository(REPOSITORY_TEST, files1);

        test.commit(REPOSITORY_TEST, COMMIT_TEST);


    }
}
