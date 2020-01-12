package it.unisa.git;

import it.unisa.git.entity.Commit;
import it.unisa.git.entity.Repository;
import it.unisa.git.impl.ErrorMessage;
import it.unisa.git.impl.GitProtocolImpl;
import it.unisa.git.impl.MessageListenerImpl;
import net.tomp2p.peers.PeerAddress;
import org.apache.commons.io.FileUtils;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    private static final String TEXT_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
    private static final String TEXT_2 = "Praesent egestas dolor sapien, sed sagittis nisl condimentum non.";
    private static final String TEXT_3 = "Quisque rutrum quam in enim pulvinar, ut fermentum ante commodo.";
    private static final String PATH = "resources/files/";
    @Option(name = "-m", aliases = "--masterip", usage = "the master peer ip address", required = true)
    private static String master = "127.0.0.1";
    @Option(name = "-id", aliases = "--identifierpeer", usage = "the unique identifier for this peer", required = true)
    private static int id = 0;
    private static File dir;

    public static void main(String[] args) {
        boolean filesAdded = false;
        boolean commitAdded = false;

        Main exe = new Main();
        final CmdLineParser parser = new CmdLineParser(exe);
        try {
            parser.parseArgument(args);
            TextIO txtIO = TextIoFactory.getTextIO();
            TextTerminal terminal = txtIO.getTextTerminal();
            GitProtocolImpl peer = new GitProtocolImpl(id, master, new MessageListenerImpl(id));

            terminal.printf("\nID %d MASTER %s\n", id, master);

            while (true) {
                printMenu(terminal);

                int option = txtIO.newIntInputReader().withMaxVal(7).withMinVal(1).read("\nOption: ");
                switch (option) {
                    case 1:
                        terminal.printf("Enter the repository name & the folder path!\n\n");
                        String repository_1 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Name:");
                        dir = new File(PATH, id + "/" + repository_1);
                        if (peer.createRepository(repository_1, dir)) {
                            dir.mkdirs();
                            terminal.printf("\nRepository %s successfully created in the path '%s'!\n", repository_1, dir.getPath());
                        } else
                            terminal.printf("\nError in repository creation or already exists.");
                        break;
                    case 2:
                        terminal.printf("Choose the file to add to the repository!\n\n");
                        String repository_2 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Name:");
                        int num_files = txtIO.newIntInputReader().withDefaultValue(1).read("Number of files:");

                        List<File> files = new ArrayList<>();
                        for (int i = 0; i < num_files; i++) {
                            String name = txtIO.newStringInputReader().withDefaultValue("" + (i + 1)).read("Name:");
                            String text = txtIO.newStringInputReader().withDefaultValue(TEXT_1).read("Text:");
                            files.add(generateFile(name, text, dir));
                        }

                        if (peer.addFilesToRepository(repository_2, files)) {
                            filesAdded = true;
                            terminal.printf("\nFiles successfully added in the '%s' repository!\n", repository_2);
                        } else
                            terminal.printf("\nThe given repository doesn't exists.");
                        break;
                    case 3:
                        if (filesAdded) {
                            terminal.printf("Entry the commit!\n\n");
                            String repository_3 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Repository name:");
                            String commit = txtIO.newStringInputReader().withDefaultValue("default-commit").read("Text:");
                            if (peer.commit(repository_3, commit)) {
                                commitAdded = true;
                                terminal.printf("\nCommit successfully added in the '%s' repository!\n", repository_3);
                            } else
                                terminal.printf("\nThe given repository doesn't exists.");
                        } else {
                            terminal.printf("You need to add some files before creating a commit!\n\n");
                        }
                        break;
                    case 4:
                        if (filesAdded && commitAdded) {
                            terminal.printf("Push the changes to the repository!\n\n");
                            String repository_4 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Repository name:");
                            String push = peer.push(repository_4);
                            terminal.printf("\n" + push + "\n");
                            if (push.equals(ErrorMessage.PUSH_SUCCESS.print())) {
                                filesAdded = false;
                                commitAdded = false;
                                printRepo(terminal, peer.getRepository());
                            }
                        } else {
                            terminal.printf("You need to add some files AND a commit before pushing something!\n\n");
                        }
                        break;
                    case 5:
                        terminal.printf("Pull the changes to the repository!\n\n");
                        String repository_5 = txtIO.newStringInputReader().withDefaultValue("default-repository").read("Repository name:");
                        String pull = peer.pull(repository_5);
                        terminal.printf("\n" + pull + "\n");
                        if (pull.equals(ErrorMessage.PULL_SUCCESS.print())) {
                            printRepo(terminal, peer.getRepository());
                        }
                        break;
                    case 6:
                        List<Commit> commits = peer.getRepository().getCommits();
                        terminal.printf("\nLIST OF COMMITS: \n");
                        for (Commit c : commits) {
                            terminal.printf("\nAUTHOR: "+ c.getAuthor() + "\n" );
                            terminal.printf("TEXT: " + c.getText() + "\n");
                            terminal.printf("DATE: " + c.getTimestamp() + "\n");
                            terminal.printf("MODIFIED FILES: " + c.getModifiedFiles() + "\n");
                        }
                        break;
                    case 7:
                        terminal.printf("\nARE YOU SURE TO LEAVE THE NETWORK?\n");
                        boolean exit = txtIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
                        if (exit) {
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
        terminal.printf("\n6 - SHOW COMMITS\n");
        terminal.printf("\n7 - EXIT\n");

    }

    public static File generateFile(String name, String text, File dir) throws IOException {

        File f = new File(dir, name + ".txt");
        FileUtils.writeLines(f, Collections.singleton(text));

        return f;

    }

    public static void printRepo(TextTerminal terminal, Repository rep) {
        terminal.printf("\n\nREPOSITORY NAME: " + rep.getName());
        terminal.printf("\nLIST OF FILES: \n");
        for (File f : rep.getFileMap().keySet()) {
            terminal.printf("- " + f.getName() + "\n");
            for (String s : rep.getFileMap().get(f)) {
                terminal.printf("   " + s + "\n");
            }
        }
    }
}
