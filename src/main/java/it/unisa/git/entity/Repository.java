package it.unisa.git.entity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Array;
import java.sql.Timestamp;
import java.util.*;

public class Repository implements Serializable {
    private static final long serialVersionUID = 1L;

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
                if(f.getName().equals(f2.getName()) && !FileUtils.contentEquals(f, f2)){
                    fileMap.remove(f2);
                    fileMap.put(f, new Timestamp(System.currentTimeMillis()));
                    FileUtils.writeLines(f, FileUtils.readLines(f2, "UTF-8"), true);
                    System.out.println(FileUtils.readLines(f, "UTF-8").toString());
                    remove.add(f2);
                    break;
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
        if(localDirectory.length() > 1){
            for (File f : localDirectory.listFiles()) {
                existingFiles.add(f);
            }
        }

        //System.out.println("I file esistenti sono: " + existingFiles.toString());
        return existingFiles;
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
