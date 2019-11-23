package it.unisa.git.impl;

public enum ErrorMessage {

    ERROR_MESSAGE("Something went wrong!"),
    REPOSITORY_NOT_FOUND ("Repository not found! You need to create it before requesting a pull."),
    PULL_SUCCESS("Successfully pulled changes into the repository!"),
    PULL_NO_UPDATE("Already up-to-date!"),
    PUSH_CONFLICT("Someone else changed something into the repository, " +
            "you need to pull it before pushing your changes!"),
    PUSH_SUCCESS("Succesfully pushed the changes!")
    ;

    private String msg;

    ErrorMessage(String msg) {
        this.msg = msg;
    }

    public String print(){
        return msg;
    }
}
