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
			File dir = new File(PATH, dir_name);

			if (dir.exists()) {
				FileUtils.deleteDirectory(dir);
			}

			dir.mkdir();
			dirs[i]=dir;
		}
	}

	/**
	 * TEST CASE 1 : createRepository
	 * @result Repository creata con successo.
	 */
	@Test
	public void createRepositorySuccess() {
		assertTrue(master_peer.createRepository(REPO_NAME, dirs[0]));
	}

	/**
	 * TEST CASE 2 : createRepository
	 * @result Repository duplicata.
	 */
	@Test
	public void createRepositoryFailure() {
		master_peer.createRepository(REPO_NAME, dirs[0]);
		assertFalse(master_peer.createRepository(REPO_NAME, dirs[0]));
	}

	/**
	 * TEST CASE 3 : addFilesToRepository
	 * @result Aggiunta di file nella repository con successo.
	 */
	@Test
	public void addFilesToRepositorySuccess() throws IOException {
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		assertTrue(master_peer.addFilesToRepository(REPO_NAME, files));
		System.out.println("addFilesToRepositorySuccess = " + master_peer.getRepository().getFileMap().keySet());

	}

	/**
	 * TEST CASE 4 : addFilesToRepository
	 * @result Aggiunta di file in una respository sbagliata.
	 */
	@Test
	public void addFilesToRepositoryFailure() throws IOException {
		master_peer.createRepository(REPO_NAME, dirs[0]);

		List<File> files = new ArrayList<>();
		for (int i=0; i<2; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[0], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		assertFalse(master_peer.addFilesToRepository("X", files));
		System.out.println("addFilesToRepositoryFailure = " + master_peer.getRepository().getFileMap().keySet());

	}

	/**
	 * TEST CASE 5 : commit
	 * @result Eseguizione di una commit con successo*
	 */
	@Test
	public void commitSuccess() throws IOException {
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
		System.out.println("commitSuccess = " + master_peer.getRepository().getCommits());

	}

	/**
	 * TEST CASE 6 : commit
	 * @result Commit eseguita su repository errata.
	 */
	@Test
	public void commitFailure() throws IOException {
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
		System.out.println("commitFailure = " + master_peer.getRepository().getCommits());

	}

	/**
	 * TEST CASE 7 : push
	 * @result Push di file eseguita con successo
	 */
	@Test
	public void pushSuccess() throws IOException {
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

		System.out.println("pushSuccess (master_peer) = " + master_peer.getRepository().getFileMap().keySet());

	}

	/**
	 * TEST CASE 8 : push
	 * @result Un peer effettua la push, un secondo peer non riesce ad effettuare la push non avendo
	 * aggiornato la repository prima di inoltrare i suoi aggiornamenti.
	 */
	@Test
	public void pushConflict() throws IOException {
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

		System.out.println("pushConflict (master_peer) = " + master_peer.getRepository().getFileMap().keySet());
		System.out.println("pushConflict (peer_1) = " + peer_1.getRepository().getFileMap().keySet());

	}

	/**
	 * TEST CASE 9 : pull
	 * @result Pull eseguita con successo.
	 */
	@Test
	public void pullSuccess() throws IOException {
		
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

		System.out.println("pullSuccess (master_peer) = " + master_peer.getRepository().getFileMap().keySet());
		System.out.println("pullSuccess (peer_1) = " + peer_1.getRepository().getFileMap().keySet());

	}

	/**
	 * TEST CASE 10 : pull
	 * @result Repository non trovata.
	 */
	@Test
	public void pullRepositoryNotFound(){
		assertEquals(ErrorMessage.REPOSITORY_NOT_FOUND.print(), master_peer.pull(REPO_NAME));
	}

	/**
	 * TEST CASE 11 : pull
	 * @result Due peer effettuano le push con successo per poi effettuare due pull
	 * che non riscontrano ulteriori cambiamenti.
	 */
	@Test
	public void pullNoUpdate() throws IOException {
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

		System.out.println("pullNoUpdate (master_peer) = " + master_peer.getRepository().getFileMap().keySet());
		System.out.println("pullNoUpdate (peer_2) = " + peer_2.getRepository().getFileMap().keySet());

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