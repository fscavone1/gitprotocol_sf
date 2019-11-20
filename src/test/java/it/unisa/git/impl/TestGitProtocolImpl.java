package it.unisa.git.impl;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

public class TestGitProtocolImpl {

    private static final int FILES = 5;
    private static final String REPO_NAME = "repo_test";
    private static final String PATH = "resources/files/";
    private static final String REPOSITORY_TEST = "repo_test";
    private static final String COMMIT_TEST = "Some text for commit";
    private static final String TEXT_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
    private static final String TEXT_2 = "Praesent egestas dolor sapien, sed sagittis nisl condimentum non.";
    private static final String TEXT_3 = "Quisque rutrum quam in enim pulvinar, ut fermentum ante commodo.";

    public static void main(String[] args) throws Exception {

        File dir_1 = new File(PATH+"peer_1");

        if(dir_1.exists()){
            FileUtils.deleteDirectory(dir_1);
        }

        dir_1.mkdirs();

        File dir_2 = new File(PATH+"peer_2");

        if(dir_2.exists()) {
            FileUtils.deleteDirectory(dir_2);
        }

        dir_2.mkdirs();

        try{
            GitProtocolImpl peer_1 = new GitProtocolImpl(0, "127.0.0.1");
            GitProtocolImpl peer_2 = new GitProtocolImpl(1, "127.0.0.1");

            System.out.println("PEER1 CREATE REPO");
            peer_1.createRepository(REPO_NAME, dir_1);

            System.out.println("\nPEER1 ADD FILE");
            peer_1.addFilesToRepository(REPO_NAME, generateFiles(1, dir_1));

            System.out.println("\nPEER1 COMMIT");
            peer_1.commit(REPO_NAME, COMMIT_TEST);

            System.out.println("\nPEER1 PUSH");
            peer_1.push(REPO_NAME);

            System.out.println("\nPEER2 CREATE REPO");
            peer_2.createRepository(REPO_NAME, dir_2);

            System.out.println("\nPEER2 PULL");
            peer_2.pull(REPO_NAME);

            System.out.println("\nPEER2 ADD FILE");
            peer_2.addFilesToRepository(REPO_NAME, generateFiles(2, dir_2));

            System.out.println("\nPEER2 COMMIT");
            peer_2.commit(REPO_NAME, COMMIT_TEST);

            System.out.println("\nPEER2 PUSH");
            peer_2.push(REPO_NAME);

            System.out.println("\nPEER1 PULL");
            peer_1.pull(REPO_NAME);

            peer_1.leaveNetwork();
            peer_2.leaveNetwork();

        }catch (Exception e){
            e.printStackTrace();
        }

        return;

    }

    public static List<File> generateFiles(int num_files, File dir) throws FileNotFoundException {
        List<File> generated_files = new ArrayList<File>();
        String[] texts = {TEXT_1, TEXT_2, TEXT_3};

        for(int i = 1; i<num_files+1; i++){
            File f = new File(dir,i+".txt");
            FileOutputStream stream = new FileOutputStream(f);
            PrintStream printStream = new PrintStream(stream);

            Random r = new Random();
            String text = texts[r.nextInt(texts.length)];
            printStream.println(text);

            generated_files.add(f);
        }
        return generated_files;
    }
}
