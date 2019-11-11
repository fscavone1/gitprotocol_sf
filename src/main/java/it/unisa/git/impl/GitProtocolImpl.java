package it.unisa.git.impl;

import it.unisa.git.entity.Repository;

import java.io.File;
import java.util.List;

public class GitProtocolImpl implements GitProtocol {

    private Repository repository;

    public GitProtocolImpl(int _id, String _master_peer) throws Exception
    {

    }

    /**
     * Creates new repository in a directory
     * @param _repo_name a String, the name of the repository.
     * @param _directory a File, the directory where create the repository.
     * @return true if it is correctly created, false otherwise.
     */
    public boolean createRepository(String _repo_name, File _directory){
        if(repository != null){
            return false;                           //already exists
        }
        try {
            repository = new Repository(_directory.getPath(), _repo_name);
            return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Adds a list of File to the given local repository.
     * @param _repo_name a String, the name of the repository.
     * @param files a list of Files to be added to the repository.
     * @return true if it is correctly added, false otherwise.
     */
    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        if (repository.getName().equals(_repo_name)) {
            try {
                repository.addFiles(files);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * Apply the changing to the files in  the local repository.
     * @param _repo_name a String, the name of the repository.
     * @param _message a String, the message for this commit.
     * @return true if it is correctly committed, false otherwise.
     */
    public boolean commit(String _repo_name, String _message){
        if(repository.getName().equals(_repo_name)){
            return repository.addCommit(_message, _repo_name);
        }
        return false;
    }
    /**
     * Push all commits on the Network. If the status of the remote repository is changed,
     * the push fails, asking for a pull.
     * @param _repo_name _repo_name a String, the name of the repository.
     * @return a String, operation message.
     */
    public String push(String _repo_name){
        return "";
    }
    /**
     * Pull the files from the Network. If there is a conflict, the system duplicates
     * the files and the user should manually fix the conflict.
     * @param _repo_name _repo_name a String, the name of the repository.
     * @return a String, operation message.
     */
    public String pull(String _repo_name){
        return "";
    }
}
