package it.unisa.git.entity;

import java.io.Serializable;

public class Commit implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String text;
    private final String repository;
    private final Long timestamp;

    public Commit(String text, String repository, Long timestamp) {
        this.text = text;
        this.repository = repository;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public String getRepository() {
        return repository;
    }

    public Long getTimestamp() {
        return timestamp;
    }

}
