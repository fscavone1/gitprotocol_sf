package it.unisa.git.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commit)) return false;
        Commit commit = (Commit) o;
        return Objects.equals(text, commit.text) &&
                Objects.equals(repository, commit.repository) &&
                Objects.equals(timestamp, commit.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, repository, timestamp);
    }
}
