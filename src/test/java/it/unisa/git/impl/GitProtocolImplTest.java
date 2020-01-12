package it.unisa.git.impl;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class GitProtocolImplTest {

	private static final String REPO_NAME = "repo_test";
	private static final String PATH = "resources/files/";
	private static final String COMMIT_TEST = "Some text for commit";
	private static final String TEXT_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
	private static final String TEXT_2 = "Praesent egestas dolor sapien, sed sagittis nisl condimentum non.";

	private GitProtocolImpl master_peer;
	private GitProtocolImpl peer_1;
	private GitProtocolImpl peer_2;
	private File dirs[];

	private final static Logger log = Logger.getLogger(GitProtocolImplTest.class.getName());

	public GitProtocolImplTest() throws Exception {
		master_peer = new GitProtocolImpl(0, "127.0.0.1", new MessageListenerImpl(0));
		peer_1 = new GitProtocolImpl(1, "127.0.0.1", new MessageListenerImpl(1));
		peer_2 = new GitProtocolImpl(2, "127.0.0.1", new MessageListenerImpl(2));

		dirs = new File[4];
	}

	@Before
	public void setUp() throws Exception {

		for (int i = 0; i < dirs.length; i++) {
			String dir_name = "peer_" + i;
			File dir = new File(PATH, dir_name + "/" + REPO_NAME);

			if (dir.exists()) {
				FileUtils.deleteDirectory(dir);
			}

			dir.mkdir();
			dirs[i]=dir;
		}
	}

	/**
	 * TEST CASE 1 : createRepository
	 * @result Creates a new repository in a directory successfully.
	 */
	@Test
	public void createRepositorySuccess() {
		System.out.println("createRepositorySuccess is running...");
		assertTrue(master_peer.createRepository(REPO_NAME, dirs[0]));
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 2 : createRepository
	 * @result The creation of a new repository in a directory fails
	 * because it's duplicated.
	 */
	@Test
	public void createRepositoryFailure() {
		System.out.println("createRepositoryFailure is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);
		assertFalse(master_peer.createRepository(REPO_NAME, dirs[0]));
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 3 : addFilesToRepository
	 * @result Adds a list of File to the given local repository successfully.
	 */
	@Test
	public void addFilesToRepositorySuccess() throws IOException {
		System.out.println("addFilesToRepositorySuccess is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		assertTrue(master_peer.addFilesToRepository(REPO_NAME, files));
		System.out.println("MASTER-PEER = " + master_peer.getRepository().getFileMap().keySet());
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 4 : addFilesToRepository
	 * @result The add of a list of File fails when the given repository is wrong.
	 */
	@Test
	public void addFilesToRepositoryFailure() throws IOException {
		System.out.println("addFilesToRepositoryFailure is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		assertFalse(master_peer.addFilesToRepository("X", files));
		System.out.println("MASTER-PEER = " + master_peer.getRepository().getFileMap().keySet());
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 5 : commit
	 * @result Applies the changing to the files in the local repository successfully.
	 */
	@Test
	public void commitSuccess() throws IOException {
		System.out.println("commitSuccess is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		master_peer.addFilesToRepository(REPO_NAME, files);
		assertTrue(master_peer.commit(REPO_NAME, COMMIT_TEST));
		System.out.println("MASTER-PEER = " + master_peer.getRepository().getFileMap().keySet());
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 6 : commit
	 * @result The changing to the files fails because the given repository is wrong.
	 */
	@Test
	public void commitFailure() throws IOException {
		System.out.println("commitFailure is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		master_peer.addFilesToRepository(REPO_NAME, files);
		assertFalse(master_peer.commit("X", COMMIT_TEST));
		System.out.println("MASTER-PEER = " + master_peer.getRepository().getFileMap().keySet());
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 7 : push
	 * @result Pushes all commits on the network successfully.
	 */
	@Test
	public void pushSuccess() throws IOException {
		System.out.println("pushSuccess is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		master_peer.addFilesToRepository(REPO_NAME, files);
		master_peer.commit(REPO_NAME, COMMIT_TEST);
		assertEquals(ErrorMessage.PUSH_SUCCESS.print(), master_peer.push(REPO_NAME));

		System.out.println("MASTER-PEER = " + master_peer.getRepository().testToString());
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 8 : push
	 * @result The push of all commits fails because the repository of
	 * the second peer isn't updated;
	 */
	@Test
	public void pushConflict() throws IOException {
		System.out.println("pushConflict is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		master_peer.addFilesToRepository(REPO_NAME, files);
		master_peer.commit(REPO_NAME, COMMIT_TEST);
		master_peer.push(REPO_NAME);

		peer_1.createRepository(REPO_NAME, dirs[1]);

		files.clear();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[1], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		peer_1.addFilesToRepository(REPO_NAME, files);
		peer_1.commit(REPO_NAME, COMMIT_TEST);

		assertEquals(ErrorMessage.PUSH_CONFLICT.print(), peer_1.push(REPO_NAME));

		System.out.println("MASTER-PEER = " + master_peer.getRepository().testToString());
		System.out.println("PEER-1 = " + peer_1.getRepository().testToString());
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 9 : pull
	 * @result Pulles the files from the network successfully.
	 */
	@Test
	public void pullSuccess() throws IOException {
		System.out.println("pullSuccess is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		master_peer.addFilesToRepository(REPO_NAME, files);
		master_peer.commit(REPO_NAME, COMMIT_TEST);
		master_peer.push(REPO_NAME);

		peer_1.createRepository(REPO_NAME, dirs[1]);
		assertEquals(ErrorMessage.PULL_SUCCESS.print(), peer_1.pull(REPO_NAME));

		System.out.println("MASTER-PEER = " + master_peer.getRepository().getFileMap().keySet());
		System.out.println("PEER-1 = " + peer_1.getRepository().getFileMap().keySet());
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 10 : pull
	 * @result The pull fails because there isn't a local repository with the given name.
	 */
	@Test
	public void pullRepositoryNotFound(){
		System.out.println("pullRepositoryNotFound is running...");
		assertEquals(ErrorMessage.REPOSITORY_NOT_FOUND.print(), master_peer.pull(REPO_NAME));
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 11 : pull
	 * @result The repository has no new files to download;
	 */
	@Test
	public void pullNoUpdate() throws IOException {
		System.out.println("pullNoUpdate is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		master_peer.addFilesToRepository(REPO_NAME, files);
		master_peer.commit(REPO_NAME, COMMIT_TEST);
		master_peer.push(REPO_NAME);

		peer_2.createRepository(REPO_NAME, dirs[2]);
		peer_2.pull(REPO_NAME);

		assertEquals(ErrorMessage.PULL_NO_UPDATE.print(), peer_2.pull(REPO_NAME));

		System.out.println("MASTER-PEER = " + master_peer.getRepository().getFileMap().keySet());
		System.out.println("PEER-2 = " + peer_2.getRepository().getFileMap().keySet());
		System.out.println("---------- END ----------");
	}

	/**
	 * TEST CASE 12 : push
	 * @result The push fails because there isn't a local repository with the given name.
	 */
	@Test
	public void pushRepositoryNotFound(){
		System.out.println("pushRepositoryNotFound is running...");
		master_peer.createRepository(REPO_NAME, dirs[0]);
		List<File> files = new ArrayList<>();
		File f = new File(dirs[0], "file_1.txt");
		files.add(f);
		master_peer.commit(REPO_NAME, COMMIT_TEST);
		assertEquals(ErrorMessage.REPOSITORY_NOT_FOUND.print(), master_peer.push("X"));
		System.out.println("---------- END ----------");
	}


	@AfterClass
	public static void cleanUp() throws IOException {
		FileUtils.deleteDirectory(new File(PATH));
	}

	@After
	public void leave(){
		master_peer.leaveNetwork();
		peer_1.leaveNetwork();
		peer_2.leaveNetwork();
	}

}