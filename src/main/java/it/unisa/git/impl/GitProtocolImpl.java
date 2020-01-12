package it.unisa.git.impl;

import it.unisa.git.entity.Repository;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GitProtocolImpl implements GitProtocol {

    final private Peer peer;
    final private PeerDHT _dht;
    final private int DEFAULT_MASTER_PORT = 4000;
    final private int ID;
    private Repository repository;
    private int commit_pending = 0;

    public GitProtocolImpl(int _id, String _master_peer, final MessageListener _listener) throws Exception {
        this.ID = _id;
        peer = new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT + _id).start();
        _dht = new PeerBuilderDHT(peer).start();

        FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).
                ports(DEFAULT_MASTER_PORT).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        } else {
            throw new Exception("Error in master peer bootstrap.");
        }

        peer.objectDataReply(new ObjectDataReply() {

            public Object reply(PeerAddress sender, Object request) throws Exception {
                return _listener.parseMessage(request);
            }
        });
    }

    /**
     * Creates new repository in a directory
     *
     * @param _repo_name a String, the name of the repository.
     * @param _directory a File, the directory where create the repository.
     * @return true if it is correctly created, false otherwise.
     */
    public boolean createRepository(String _repo_name, File _directory) {
        try {
            if(repository == null){
                repository = new Repository(_directory, _repo_name);
                repository.setContributors(_dht.peer().peerAddress());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds a list of File to the given local repository.
     *
     * @param _repo_name a String, the name of the repository.
     * @param files      a list of Files to be added to the repository.
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
     *
     * @param _repo_name a String, the name of the repository.
     * @param _message   a String, the message for this commit.
     * @return true if it is correctly committed, false otherwise.
     */
    public boolean commit(String _repo_name, String _message) {
        if (repository.getName().equals(_repo_name)) {
            commit_pending += 1;
            repository.addCommit(_message, _repo_name, ID);
            return true;
        }
        return false;
    }

    /**
     * Push all commits on the Network. If the status of the remote repository is changed,
     * the push fails, asking for a pull.
     *
     * @param _repo_name _repo_name a String, the name of the repository.
     * @return a String, operation message.
     */
    public String push(String _repo_name) {
        try {
            if (repository == null || !repository.getName().equals(_repo_name)) {
                return ErrorMessage.REPOSITORY_NOT_FOUND.print();
            }
            else {
                Repository dht_repo = getFromDHT(_repo_name);
                if (dht_repo == null || repository.getCommits().size() - dht_repo.getCommits().size() == 1) {
                    saveOnDHT(_repo_name, repository);
                    commit_pending = 0;
                    return ErrorMessage.PUSH_SUCCESS.print();
                } else {
                    return ErrorMessage.PUSH_CONFLICT.print();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ErrorMessage.ERROR_MESSAGE.print();
    }

    /**
     * Pull the files from the Network. If there is a conflict, the system duplicates
     * the files and the user should manually fix the conflict.
     *
     * @param _repo_name _repo_name a String, the name of the repository.
     * @return a String, operation message.
     */
    public String pull(String _repo_name) {
        try {
            if (repository == null || !repository.getName().equals(_repo_name)) {
                return ErrorMessage.REPOSITORY_NOT_FOUND.print();
            }

            boolean append = false;
            Repository dht_repo = getFromDHT(_repo_name);
            int commit_diff = repository.getCommits().size() - dht_repo.getCommits().size();

            HashMap<File, List<String>> dht_map = dht_repo.getFileMap();

            if (commit_diff == commit_pending) {
                return ErrorMessage.PULL_NO_UPDATE.print();
            }

            if (commit_diff < commit_pending) {
                append = true;
            }

            if (commit_diff < 0) {
                append = false;
            }

            repository.addFiles(dht_map, append);
            repository.addCommit(dht_repo.getCommits());
            repository.setContributors(dht_repo.getContributors());

            return ErrorMessage.PULL_SUCCESS.print();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ErrorMessage.ERROR_MESSAGE.print();
    }

    /**
     * Retrieve the repository
     *
     * @return an Object, represent the current repository
     */

    public Repository getRepository() {
        return repository;
    }

    /**
     * A peer leave the network and remove itself from contributors
     */
    public void leaveNetwork() {
        _dht.peer().announceShutdown().start().awaitUninterruptibly();
    }

    /**
     * Retrieve the repository from the DHT
     *
     * @param _repo_name a String, the name of the repository
     * @return an Object, represent the DHT repository
     */

    private Repository getFromDHT(String _repo_name) throws IOException, ClassNotFoundException {
        FutureGet fg = _dht.get(Number160.createHash(_repo_name)).start();
        fg.awaitUninterruptibly();
        if (fg.isSuccess()) {
            Collection<Data> repositories = fg.dataMap().values();
            if (repositories.isEmpty()) {
                return null;
            }
            return (Repository) fg.dataMap().values().iterator().next().object();
        }
        return null;
    }

    /**
     * Retrieve the repository from the DHT
     *
     * @param _repo_name a String, the name of the repository
     * @param _dir an Object, the repository to add into the DHT
     * @return true if it is correctly saved, false otherwise.
     */

    private boolean saveOnDHT(String _repo_name, Repository _dir) {
        try {
            _dht.put(Number160.createHash(_repo_name)).data(new Data(_dir)).start().awaitUninterruptibly();
            for (PeerAddress peer : _dir.getContributors()) {
                if (!peer.equals(_dht.peer().peerAddress())) {
                    FutureDirect futureDirect = _dht.peer().sendDirect(peer).object("Peer " +
                            ID + " pushed some changes into the repository! ").start();
                    futureDirect.awaitUninterruptibly();
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
