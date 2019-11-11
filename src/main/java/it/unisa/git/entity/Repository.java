package it.unisa.git.entity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;


public class Repository implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private String localDirectory;
    private final ArrayList<File> files;
    private ArrayList<Commit> commits;
    private final HashMap<File, Timestamp> fileMap;

    public Repository(String localDirectory, String name) {
        this.localDirectory = localDirectory;
        this.name = name;
        this.files = getExistingFiles();
        this.commits = new ArrayList<Commit>();
        this.fileMap = new HashMap<>();
        for (File f: this.files) {
            this.fileMap.put(f, new Timestamp(System.currentTimeMillis()));
        }
    }

    public String getName() {
        return name;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public ArrayList<Commit> getCommits() {
        return commits;
    }

    public void setCommits(ArrayList<Commit> commits) {
        this.commits = commits;
    }

    public void addFiles(List<File> files) throws IOException {

        for(int i=0; i<files.size(); i++){
            File f = files.get(i);
            for (int j=0; j<this.files.size(); j++){
                File f2 = this.files.get(j);
                if(f.getName().equals(f2.getName())){
                    fileMap.remove(f2);
                    fileMap.put(f, new Timestamp(System.currentTimeMillis()));
                    files.set(i, null);
                    break;
                }
            }
        }

        files.removeAll(Collections.singletonList(null));
        if(!files.isEmpty()) {
            this.files.addAll(files);
            for (File f : files) {
                fileMap.put(f, new Timestamp(System.currentTimeMillis()));
            }
        }

        System.out.println(fileMap.toString());
        System.out.println(this.files.toString());
    }

    public ArrayList<File> getExistingFiles(){
        ArrayList<File> existingFiles = new ArrayList<File>();
        File dir = new File(localDirectory);
        if(dir.length() > 1){
            for (File f : dir.listFiles()) {
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
}
