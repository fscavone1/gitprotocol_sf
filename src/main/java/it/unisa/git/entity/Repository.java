package it.unisa.git.entity;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

public class Repository implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String ENCODING = "UTF-8";

    private final String name;
    private File localDirectory;
    private final List<File> files;
    private List<Commit> commits;
    private final HashMap<File, Timestamp> fileMap;

    public Repository(File localDirectory, String name) {
        this.localDirectory = localDirectory;
        this.name = name;
        this.files = getExistingFiles();
        this.commits = new ArrayList<Commit>();
        this.fileMap = new HashMap<>();
        for (File f : this.files) {
            this.fileMap.put(f, new Timestamp(System.currentTimeMillis()));
        }
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

    public HashMap<File, Timestamp> getFileMap() {
        return fileMap;
    }

    public void addFiles(List<File> files) throws IOException {

        List<File> remove = new ArrayList<>();

        for (File f : files) {
            for(File f2 : this.files){
                if(f.getName().equals(f2.getName())) {
                    fileMap.remove(f2);
                    fileMap.put(f, new Timestamp(System.currentTimeMillis()));
                    remove.add(f2);
                    compareFiles(f, f2);
                }
            }
        }

        this.files.removeAll(remove);
        this.files.addAll(files);

        System.out.println(this.files.toString());

        for(File f : files){
            if(!fileMap.containsKey(f)){
                fileMap.put(f, new Timestamp(System.currentTimeMillis()));
            }
            if(!f.getParentFile().equals(localDirectory)){
                FileUtils.copyFile(f, new File(localDirectory, f.getName()));
            }
        }

    }

    public List<File> getExistingFiles(){
        List<File> existingFiles = new ArrayList<>();
        if(localDirectory.length() > 1) {
            for (File f : localDirectory.listFiles()) {
                existingFiles.add(f);
            }
        }

        //System.out.println("I file esistenti sono: " + existingFiles.toString());
        return existingFiles;
    }

    public void compareFiles(File f, File f2){
        try {
            if (!FileUtils.contentEquals(f, f2)) {
                List<String> read_f = FileUtils.readLines(f, ENCODING);
                List<String> read_f2 = FileUtils.readLines(f2, ENCODING);
                List<String> remove_f2 = new ArrayList<>();

                for (String s : read_f) {
                    for (String s2 : read_f2) {
                        if (s.equals(s2)) {
                            remove_f2.add(s2);
                            break;
                        }
                    }
                }

                read_f2.removeAll(remove_f2);

                if (!read_f2.isEmpty()) {
                    FileUtils.writeLines(f, Collections.singleton("\n---------------- MERGED ----------------------\n"), true);
                    FileUtils.writeLines(f, read_f2, true);
                }

                System.out.println(FileUtils.readLines(f, ENCODING).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean addCommit(String text, String repository){
        Commit commit = new Commit(text, repository);
        if(!commits.contains(commit)){
            commits.add(commit);
            System.out.println(commits.toString());
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
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", localDirectory='" + localDirectory + '\'' +
                ", files=" + files +
                ", commits=" + commits +
                ", fileMap=" + fileMap +
                '}';
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
                Objects.equals(fileMap, that.fileMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, commits, fileMap);
    }
}
