package it.unisa.git.entity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Repository implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final ArrayList<File> files;
    private ArrayList<Commit> commits;

    public Repository(String name, ArrayList<File> files, ArrayList<Commit> commits) {
        this.name = name;
        this.files = files;
        this.commits = commits;
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
}
