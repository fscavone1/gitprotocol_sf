# Git Protocol 
[![Build Status](https://travis-ci.org/fscavone1/gitprotocol_sf.svg?branch=master)](https://travis-ci.org/fscavone1/gitprotocol_sf)

The purpose of this project is to design and develop the Git protocol distributed versioning control on a P2P network. Each peer can manage its projects (a set of files) using the Git protocol (a minimal version of it). The system allows the users to create a new repository in a specific folder, add new files to be tracked by the system, apply the changing on the local repository (commit function), push the changing in the network and pull the changing from the network. The git protocol has a lot specific behaviors to manage the conflicts, in this version it is only required that if there are some conflicts the systems can download the remote copy and the merge is manually done. 

### Basic operations
- *createRepository* : creates a new repository in a directory;
- *addFilesToRepository* : adds a list of File to the given local repository;
- *commit* : applies the changing to the files in the local repository;
- *push* : pushes all commits on the network;
- *pull* : pulls the files from the network.

### Additional operations
- *getRepository* : retrieves the repository;
- *leaveNewtork* : a peer can leave the network;
- *getFromDHT* : retrives the elements from the DHT;
- *saveOnDHT* : uploads the elements into the DHT.

### Technologies
The project has been implemented with:
- Java 8;
- TomP2P;
- Apache Maven;
- JUnit;
- Docker;
- IntelliJ IDEA.

## Project structure
```
├───src
│   ├───main
│   │   └───java
│   │       └───it
│   │           └───unisa
│   │               └───git
│   │                   ├───entity
│   │                   └───impl
│   └───test
│       └───java
│           └───it
│               └───unisa
│                  └───git
│                      └───impl
```
### Entity package
The ```it.unisa.git.entity``` package provides two classes:
- *Commit*: the abstract Object which represents a commit;
- *Repository*: the abstract Object which represents the repository and allows to handle the commits, the saved files, and others general informations.

### Impl package
The ```it.unisa.git.impl ``` package provides five classes:
- *ErrorMessage*: the enumeration of all the possibile messages whichh can be returned from various methods;
- *GitProtocol*: the API that define all the operations of the project;
- *GitProtocolImpl*: the implementation of the previous API;
- *MessageListener*: the API that define the message listener;
- *MessageListenerImpl*: the implementation of a method which parse the messages.

### Git pacakge
The ```it.unisa.git ``` package provide a *Main* class which allows the interaction of a user with the system. 

## Test
The ```test``` packages contains two java classes that implements all the unit tests. 

#### GitProtocolImpl - Test cases
- *createRepositorySuccess*: creates a new repository in a directory successfully;
- *createRepositoryFailure*: the creation of a new repository in a directory fails because it's duplicated;
- *addFilesToRepositorySuccess*: adds a list of File to the given local repository successfully;
- *addFilesToRepositoryFailure*: the add of a list of File fails when the given repository is wrong;
- *commitSuccess*: applies the changing to the files in the local repository successfully;
- *commitFailure*: the changing to the files fails because the given repository is wrong;
- *pushSuccess* : pushes all commits on the network successfully;
- *pushConflict* : the push of all commits fails because the repository isn't updated;
- *pullSuccess* : pulls the files from the network successfully;
- *pullRepositoryNotFound*: the pull fails because there isn't a local repository with the given name;
- *pullNoUpdate* : the repository has no new files to download;
- *pushRepositoryNotFound* : the push fails because there isn't a local repository with the given name.

#### GitProtocolImplSimulation
This class tests all the implemented methods which simulates the interaction with the system of four peer. 

The ```MASTER-PEER``` creates a repository and push one file into the network; then the ```PEER_1``` creates a repository, pull the files into it, changes the text of the file and creates a new file and then pushes them into the network; the ```PEER_2``` creates a repository and generate a ```PUSH_CONFLICT``` because it pushes new files into the network without making a pull first, so it's forced to make a pull; in the mean time the ```PEER_3``` creates a repository and pulls some changes successfully; at this point, the ```PEER_2``` detect a new ```PUSH_CONFLICT``` and after the push, it's able to pull its changes successfully. In the end all the peers pulls the changes of the others in their local repositories.

## Build in a Docker container
An example application is provided using Docker container, running on a local machine. See the Dockerfile, for the builing details.

First of all you can build your docker container:
```
docker build --no-cache -t gitprotocol_sf .
```
#### Start the master peer
After that you can start the master peer, in interactive mode (-i) and with two (-e) environment variables:
```
docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 gitprotocol_sf
```
the ```MASTERIP``` envirnoment variable is the master peer ip address and the ID environment variable is the unique id of your peer. Rember you have to run the master peer using the ```ID=0```.

#### Start a generic peer
When master is started you have to check the ip address of your container:

- Check the docker : ```docker ps```
- Check the IP address: ```docker inspect <container ID>```

Now you can start your peers varying the unique peer id:
```
docker run -i --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 gitprotocol_sf
```

## Author
***Francesca Scavone** (M. 0522500705)* for Distributed System class of Computer Science @ University of Salerno of Professor Alberto Negro and Ph.D. Carmine Spagnuolo.
