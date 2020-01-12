package it.unisa.git.entity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import net.tomp2p.peers.PeerAddress;
import org.apache.commons.io.FileUtils;

public class Repository implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String ENCODING = "UTF-8";
    private static final String MERGED = "\n---------------- MERGED ----------------------\n";

    private final String name;
    private final List<String> modifiedFiles;
    private final HashMap<File, List<String>> fileMap;
    private HashSet<PeerAddress> contributors;
    private File localDirectory;
    private List<Commit> commits;

    public Repository(File localDirectory, String name) throws IOException {
        this.localDirectory = localDirectory;
        this.name = name;
        this.modifiedFiles = new ArrayList<>();
        this.commits = new ArrayList<Commit>();
        this.fileMap = new HashMap<>();

        List<File> files = getExistingFiles();
        for (File f : files) {
            this.fileMap.put(f, FileUtils.readLines(f, ENCODING));
        }

        this.contributors = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public HashMap<File, List<String>> getFileMap() {
        return fileMap;
    }

    public HashSet<PeerAddress> getContributors() {
        return contributors;
    }

    public void setContributors(PeerAddress c) {
        contributors.add(c);
    }

    public void setContributors(HashSet<PeerAddress> c) {
        contributors.addAll(c);
    }

    public void removeContributor(PeerAddress c) {
        contributors.remove(c);
    }

    /**
     * Adds the files to the repository
     *
     * @param files a List, the files to add to the repository.
     */

    public void addFiles(List<File> files) throws IOException {

        modifiedFiles.clear();

        List<File> remove = new ArrayList<>();
        List<File> repoFiles = new ArrayList<>(fileMap.keySet());

        for (File f : files) {
            for (File f2 : repoFiles) {
                if (f.getName().equals(f2.getName())) {
                    fileMap.remove(f2);
                    fileMap.put(f, FileUtils.readLines(f, ENCODING));
                    modifiedFiles.add(f.getName());
                }
            }
        }

        files.removeAll(remove);

        for (File f : files) {
            if (!fileMap.containsKey(f)) {
                fileMap.put(f, FileUtils.readLines(f, ENCODING));
                modifiedFiles.add(f.getName());
            }
        }

    }

    /**
     * Adds the DHT files to the repository
     *
     * @param dht_map an HashMap, the DHT files to add to the repository.
     * @param append a Boolean, if the files needs to be merged or not.
     */

    public void addFiles(HashMap<File, List<String>> dht_map, boolean append) throws IOException {

        modifiedFiles.clear();

        List<File> dht_files = new ArrayList<>(dht_map.keySet());
        List<File> files = new ArrayList<>(fileMap.keySet());

        for (File f : dht_files) {
            for (File f2 : files) {
                if (f.getName().equals(f2.getName())) {
                    compareFiles(f2, dht_map.get(f), append);
                    dht_map.keySet().remove(f);
                    fileMap.put(f2, FileUtils.readLines(f2, ENCODING));
                    modifiedFiles.add(f2.getName());
                }
            }
        }

        for (File f : dht_map.keySet()) {
            if (!fileMap.containsKey(f)) {
                File repo_f = new File(localDirectory, f.getName());
                FileUtils.writeLines(repo_f, dht_map.get(f));
                fileMap.put(repo_f, dht_map.get(f));
                modifiedFiles.add(f.getName());
            }
        }

    }

    /**
     * Retrieve the files existing in the repository.
     *
     * @return the list of files.
     */

    public List<File> getExistingFiles() {
        List<File> existingFiles = new ArrayList<>();
        if (localDirectory.length() > 1) {
            for (File f : localDirectory.listFiles()) {
                existingFiles.add(f);
            }
        }

        return existingFiles;
    }


    /**
     * Merges the local file with the DHT file, which has the same name and different content.
     *
     * @param f a File, the local file with the same name of a file in the DHT.
     * @param read_f2 a List of String, the content of the file in the DHT.
     */

    private void compareFiles(File f, List<String> read_f2, boolean append) {
        try {
            List<String> read_f = FileUtils.readLines(f, ENCODING);

            if (!read_f.equals(read_f2)) {
                if (append) {
                    List<String> remove_f2 = new ArrayList<>();
                    for (String s : read_f) {
                        for (String s2 : read_f2) {
                            if (s.equals(s2))
                                remove_f2.add(s2);
                            break;
                        }
                    }
                    read_f2.removeAll(remove_f2);
                    read_f2.removeAll(Collections.singleton(MERGED));

                    if (!read_f2.isEmpty()) {
                        FileUtils.writeLines(f, Collections.singleton(MERGED), append);
                        FileUtils.writeLines(f, read_f2, append);
                    }
                } else {
                    FileUtils.writeLines(f, read_f2, append);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a commit to the repository.
     *
     * @param text a String, the text of the repository.
     * @param repository a String, the name of the repository.
     * @param peer an Integer, the peer ID.
     */

    public void addCommit(String text, String repository, int peer) {
        Commit commit = new Commit(text, repository, modifiedFiles, peer+"");
        if (!commits.contains(commit)) {
            commits.add(commit);
        }
    }

    /**
     * Adds DHT commits to the repository.
     *
     * @param commits a List of Commit, the list of DHT commits to add.
     */

    public void addCommit(List<Commit> commits) {
        for (Commit c : commits) {
            if (!this.commits.contains(c)) {
                this.commits.add(c);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, commits, fileMap);
    }

    @Override
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", modifiedFiles=" + modifiedFiles +
                ", fileMap=" + fileMap +
                ", contributors=" + contributors +
                ", localDirectory=" + localDirectory +
                ", commits=" + commits +
                '}';
    }

    public String testToString() {
        return "Repository{" +
                "files=" + fileMap.keySet() +
                ",\n              commits=" + commits +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Repository)) return false;
        Repository that = (Repository) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(modifiedFiles, that.modifiedFiles) &&
                Objects.equals(fileMap, that.fileMap) &&
                Objects.equals(contributors, that.contributors) &&
                Objects.equals(localDirectory, that.localDirectory) &&
                Objects.equals(commits, that.commits);
    }
}
