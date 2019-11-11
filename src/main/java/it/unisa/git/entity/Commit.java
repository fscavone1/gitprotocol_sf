package it.unisa.git.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class Commit implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String text;
    private final String repository;
    private final Timestamp timestamp;

    public Commit(String text, String repository) {
        this.text = text;
        this.repository = repository;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public String getText() {
        return text;
    }

    public String getRepository() {
        return repository;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "text='" + text + '\'' +
                ", repository='" + repository + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
