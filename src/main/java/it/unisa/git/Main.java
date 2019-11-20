package it.unisa.git;

import it.unisa.git.impl.GitProtocolImpl;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
    private static String master = "127.0.0.1";

    @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
    private static int id = 0;

    private static final String TEXT_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
    private static final String TEXT_2 = "Praesent egestas dolor sapien, sed sagittis nisl condimentum non.";
    private static final String TEXT_3 = "Quisque rutrum quam in enim pulvinar, ut fermentum ante commodo.";
    private static File dir;

    public static void main(String[] args) {
        Main exe = new Main();
        final CmdLineParser parser = new CmdLineParser(exe);
        try{
            parser.parseArgument(args);
            TextIO txtIO = TextIoFactory.getTextIO();
            TextTerminal terminal = txtIO.getTextTerminal();
            GitProtocolImpl peer = new GitProtocolImpl(id, master);

            terminal.printf("\nID %d MASTER %s\n", id, master);

            while(true){
                printMenu(terminal);

                int option = txtIO.newIntInputReader().withMaxVal(6).withMinVal(1).read("\nOption: ");
                switch (option){
                    case 1:
                        terminal.printf("Enter the repository name & the folder path!\n\n");
                        String repository_1 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Name:");
                        String path = txtIO.newStringInputReader().withDefaultValue("resources/files/").read("Path:");
                        dir = new File(path);
                        if(peer.createRepository(repository_1, dir)){
                            dir.mkdirs();
                            terminal.printf("\nRepository %s successfully created in the path '%s'!\n",repository_1,dir.getPath());
                        }
                        else
                            terminal.printf("\nError in repository creation or already exists.");
                        break;
                    case 2:
                        terminal.printf("Choose the file to add to the repository!\n\n");
                        String repository_2 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Name:");
                        int num_files = txtIO.newIntInputReader().withDefaultValue(1).read("Number of files:");
                        List<File> files = generateFiles(num_files, dir);
                        if(peer.addFilesToRepository(repository_2, files))
                            terminal.printf("\nFiles successfully added in the '%s' repository!\n",repository_2);
                        else
                            terminal.printf("\nError in adding the files to the repository or already exists.");
                        break;
                    case 3:
                        terminal.printf("Entry the commit!\n\n");
                        String repository_3 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Repository name:");
                        String commit = txtIO.newStringInputReader().withDefaultValue("default-commit").read("Text:");
                        if(peer.commit(repository_3, commit))
                            terminal.printf("\nCommit successfully added in the '%s' repository!\n",repository_3);
                        else
                            terminal.printf("\nError in adding the commit to the repository or already exists.");
                        break;
                    case 4:
                        terminal.printf("Push the changes to the repository!\n\n");
                        String repository_4 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Repository name:");
                        if(peer.push(repository_4).equals("SUCCESS!"))
                            terminal.printf("\nSuccessfully update the repository!\n");
                        else
                            terminal.printf("\nError!");
                        break;
                    case 5:
                        terminal.printf("Pull the changes to the repository!\n\n");
                        String repository_5 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Repository name:");
                        if(peer.push(repository_5).equals("SUCCESS!"))
                            terminal.printf("\nSuccessfully update the repository!\n");
                        else
                            terminal.printf("\nAlready-up-to-date or error.");
                        break;
                    case 6:
                        terminal.printf("\nARE YOU SURE TO LEAVE THE NETWORK?\n");
                        boolean exit = txtIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
                        if(exit) {
                            peer.leaveNetwork();
                            System.exit(0);
                        }
                        break;

                    default:
                        break;
                }
            }

        } catch (CmdLineException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printMenu(TextTerminal terminal) {
        terminal.printf("\n1 - CREATE REPOSITORY\n");
        terminal.printf("\n2 - ADD FILES TO REPOSITORY\n");
        terminal.printf("\n3 - WRITE A COMMIT\n");
        terminal.printf("\n4 - PUSH TO REPOSITORY\n");
        terminal.printf("\n5 - PULL FROM REPOSITORY\n");
        terminal.printf("\n6 - EXIT\n");
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
