package it.unisa.git.impl;

import it.unisa.git.entity.Repository;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.*;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GitProtocolImpl implements GitProtocol {

    private Repository repository;
    final private Peer peer;
    final private PeerDHT _dht;
    final private int DEFAULT_MASTER_PORT = 4000;

    public GitProtocolImpl(int _id, String _master_peer) throws Exception {
        repository = new Repository(new File(""),"");
        peer = new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
        _dht = new PeerBuilderDHT(peer).start();

        FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).
                ports(DEFAULT_MASTER_PORT).start();
        fb.awaitUninterruptibly();
        if(fb.isSuccess()){
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        } else {
            throw new Exception("Error in master peer bootstrap.");
        }
    }

    /**
     * Creates new repository in a directory
     * @param _repo_name a String, the name of the repository.
     * @param _directory a File, the directory where create the repository.
     * @return true if it is correctly created, false otherwise.
     */
    public boolean createRepository(String _repo_name, File _directory){
        try {
            repository = new Repository(_directory, _repo_name);
            System.out.println(repository.toString());
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
                System.out.println(repository.toString());
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
        try{
            Repository dht_repo = getFromDHT(_repo_name);
            if(dht_repo == null || repository.getCommits().size() - dht_repo.getCommits().size() == 1) {
                saveOnDHT(_repo_name, repository);
                System.out.println(repository.toString());
                return ErrorMessage.PUSH_SUCCESS.print();
            }
            else{
                return ErrorMessage.PUSH_CONFLICT.print();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ErrorMessage.ERROR_MESSAGE.print();
    }
    /**
     * Pull the files from the Network. If there is a conflict, the system duplicates
     * the files and the user should manually fix the conflict.
     * @param _repo_name _repo_name a String, the name of the repository.
     * @return a String, operation message.
     */
    public String pull(String _repo_name){
        try{
            Repository dht_repo = getFromDHT(_repo_name);

            if(dht_repo.hashCode() == repository.hashCode()){
                System.out.println(dht_repo.toString());
                return ErrorMessage.PULL_NO_UPDATE.print();
            }
            else {
                HashMap<File, Timestamp> dht_map = dht_repo.getFileMap();
                HashMap<File, Timestamp> repo_map = repository.getFileMap();

                HashMap<File, Timestamp> remove = new HashMap<>();

                for (File f : repo_map.keySet()) {
                    for (File f2 : dht_map.keySet()) {
                        if (f.getName().equals(f2.getName()) && repo_map.get(f).after(dht_map.get(f2))) {
                            remove.put(f2, dht_map.get(f2));
                            dht_repo.getFiles().remove(f2);
                        }
                    }
                }

                dht_map.keySet().removeAll(remove.keySet());
                repo_map.putAll(dht_map);

                List<File> dht_files = new ArrayList<>(dht_repo.getFiles());

                repository.addFiles(dht_files);
                repository.addCommit(dht_repo.getCommits());

                System.out.println(repository.toString());

                return ErrorMessage.PULL_SUCCESS.print();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ErrorMessage.ERROR_MESSAGE.print();
    }

    public boolean leaveNetwork() {
        _dht.peer().announceShutdown().start().awaitUninterruptibly();
        return true;
    }

    private Repository getFromDHT(String _repo_name) throws IOException, ClassNotFoundException {
        FutureGet fg = _dht.get(Number160.createHash(_repo_name)).start();
        fg.awaitUninterruptibly();
        if(fg.isSuccess()){
            Collection<Data> repositories = fg.dataMap().values();
            if(repositories.isEmpty()){
                return new Repository(new File(""), "");
            }
            return (Repository) fg.dataMap().values().iterator().next().object();
        }
        return null;
    }

    private boolean saveOnDHT(String _repo_name, Repository _dir){
        try {
            _dht.put(Number160.createHash(_repo_name)).data(new Data(_dir)).start().awaitUninterruptibly();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
