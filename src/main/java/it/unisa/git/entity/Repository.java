package it.unisa.git.entity;

import net.tomp2p.peers.PeerAddress;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

public class Repository implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String ENCODING = "UTF-8";
    private static final String MERGED = "\n---------------- MERGED ----------------------\n";

    private final String name;
    private File localDirectory;
    private final List<File> files;
    private List<Commit> commits;
    private final HashMap<File, List<String>> fileMap;
    HashSet<PeerAddress> contributors;

    public Repository(File localDirectory, String name) throws IOException {
        this.localDirectory = localDirectory;
        this.name = name;
        this.files = getExistingFiles();
        this.commits = new ArrayList<Commit>();
        this.fileMap = new HashMap<>();
        for (File f : this.files) {
            this.fileMap.put(f, FileUtils.readLines(f, ENCODING));
        }
        this.contributors = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public List<File> getFiles() {
        return files;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public HashMap<File, List<String>> getFileMap() {
        return fileMap;
    }

    public void setContributors(PeerAddress c){
        contributors.add(c);
    }

    public void setContributors(HashSet<PeerAddress> c){
        contributors.addAll(c);
    }

    public HashSet<PeerAddress> getContributors(){
        return contributors;
    }

    public void addFiles(List<File> files) throws IOException {

        List<File> remove = new ArrayList<>();

        for (File f : files) {
            for(File f2 : this.files){
                if(f.getName().equals(f2.getName())) {
                    fileMap.remove(f2);
                    remove.add(f);
                    fileMap.put(f, FileUtils.readLines(f, ENCODING));
                }
            }
        }

        files.removeAll(remove);
        this.files.addAll(files);

        System.out.println("REMOVE: " + remove.toString());
        System.out.println("ADD FILES: " + this.files.toString());
        System.out.println("ADD FILES: " + fileMap.toString());

        for(File f : this.files){
            if(!fileMap.containsKey(f)){
                fileMap.put(f, FileUtils.readLines(f, ENCODING));
                System.out.println(FileUtils.readLines(f, ENCODING).toString());
            }
        }
    }

    public void addFiles(HashMap<File, List<String>> dht_map, boolean append) throws IOException {

        System.out.println("ADD FILES: " + dht_map.toString());

        List<File> dht_files = new ArrayList<>(dht_map.keySet());

        for (File f : dht_files) {
            for(File f2 : this.files){
                if(f.getName().equals(f2.getName())) {
                    compareFiles(f2, dht_map.get(f), append);
                    dht_map.keySet().remove(f);
                    fileMap.put(f2, FileUtils.readLines(f2, ENCODING));
                }
            }
        }

        //fileMap.putAll(dht_map);
        this.files.addAll(fileMap.keySet());
        System.out.println(this.files.toString());

        for(File f : dht_map.keySet()){
            if(!fileMap.containsKey(f)){
                File repo_f = new File(localDirectory, f.getName());
                FileUtils.writeLines(repo_f, dht_map.get(f));
                fileMap.put(repo_f, dht_map.get(f));
                this.files.add(repo_f);
            }
        }

        System.out.println("MERGE FILES: " + fileMap.toString());

    }

    public List<File> getExistingFiles(){
        List<File> existingFiles = new ArrayList<>();
        if(localDirectory.length() > 1) {
            for (File f : localDirectory.listFiles()) {
                existingFiles.add(f);
            }
        }

        return existingFiles;
    }

    private void compareFiles(File f, List<String> read_f2, boolean append){
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
                    System.out.println(read_f2.toString());

                    if (!read_f2.isEmpty()) {
                        FileUtils.writeLines(f, Collections.singleton(MERGED), append);
                        FileUtils.writeLines(f, read_f2, append);
                    }
                }
                else {
                    FileUtils.writeLines(f, read_f2, append);
                }
            }

            System.out.println("COMPAREFILE: " + f.getPath() + "           " + FileUtils.readLines(f, ENCODING).toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean addCommit(String text, String repository){
        Commit commit = new Commit(text, repository);
        if(!commits.contains(commit)){
            commits.add(commit);
            //System.out.println(commits.toString());
            return true;
        }
        return false;
    }

    public boolean addCommit(List<Commit> commits){
        for (Commit c : commits) {
            if (!this.commits.contains(c)) {
                this.commits.add(c);
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, commits, fileMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Repository)) return false;
        Repository that = (Repository) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(localDirectory, that.localDirectory) &&
                Objects.equals(files, that.files) &&
                Objects.equals(commits, that.commits) &&
                Objects.equals(fileMap, that.fileMap) &&
                Objects.equals(contributors, that.contributors);
    }

    @Override
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", localDirectory=" + localDirectory +
                ", files=" + files +
                ", commits=" + commits +
                ", fileMap=" + fileMap +
                ", contributors=" + contributors +
                '}';
    }
}
