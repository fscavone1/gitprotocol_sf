package it.unisa.git.impl;

import org.apache.commons.io.FileUtils;
import org.junit.After;
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
	private static final String TEXT_3 = "Quisque rutrum quam in enim pulvinar, ut fermentum ante commodo.";

	private GitProtocolImpl master_peer;
	private GitProtocolImpl peer_1;
	private GitProtocolImpl peer_2;
	private GitProtocolImpl peer_3;
	private File dirs[];

	public GitProtocolImplTest() throws Exception {
		master_peer = new GitProtocolImpl(0, "127.0.0.1", new MessageListenerImpl(0));
		peer_1 = new GitProtocolImpl(1, "127.0.0.1", new MessageListenerImpl(1));
		peer_2 = new GitProtocolImpl(2, "127.0.0.1", new MessageListenerImpl(2));
		peer_3 = new GitProtocolImpl(3, "127.0.0.1", new MessageListenerImpl(3));
		dirs = new File[4];
	}

	@Before
	public void setUp() throws Exception {

		for (int i = 0; i < 4; i++) {
			String dir_name = "peer_" + i;
			File dir = new File(PATH, dir_name);

			if (dir.exists()) {
				FileUtils.deleteDirectory(dir);
			}

			dir.mkdirs();
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
	}

	/**
	 * TEST CASE 7 : push
	 * @result Quattro peer eseguiti in successione che eseguono con successo la push.
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

		peer_1.createRepository(REPO_NAME, dirs[1]);
		peer_1.pull(REPO_NAME);

		files.clear();
		for (int i=2; i<4; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[1], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_2));
			files.add(f);
		}

		peer_1.addFilesToRepository(REPO_NAME, files);
		peer_1.commit(REPO_NAME, COMMIT_TEST);
		assertEquals(ErrorMessage.PUSH_SUCCESS.print(), peer_1.push(REPO_NAME));

		peer_2.createRepository(REPO_NAME, dirs[2]);
		peer_2.pull(REPO_NAME);

		files.clear();
		for (int i=4; i<6; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[2], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		peer_2.addFilesToRepository(REPO_NAME, files);
		peer_2.commit(REPO_NAME, COMMIT_TEST);
		assertEquals(ErrorMessage.PUSH_SUCCESS.print(), peer_2.push(REPO_NAME));

		peer_3.createRepository(REPO_NAME, dirs[3]);
		peer_3.pull(REPO_NAME);

		files.clear();
		for (int i=6; i<8; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[3], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_3));
			files.add(f);
		}

		peer_3.addFilesToRepository(REPO_NAME, files);
		peer_3.commit(REPO_NAME, COMMIT_TEST);
		assertEquals(ErrorMessage.PUSH_SUCCESS.print(), peer_3.push(REPO_NAME));

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
		peer_1.push(REPO_NAME);

		assertEquals(ErrorMessage.PUSH_CONFLICT.print(), peer_1.push(REPO_NAME));
	}

	/**
	 * TEST CASE 9 : pull
	 * @result Quattro peer eseguiti in successione che eseguono con successo la pull.
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

		files.clear();
		for (int i=2; i<4; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[1], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_2));
			files.add(f);
		}

		peer_1.addFilesToRepository(REPO_NAME, files);
		peer_1.commit(REPO_NAME, COMMIT_TEST);
		peer_1.push(REPO_NAME);

		peer_2.createRepository(REPO_NAME, dirs[2]);
		assertEquals(ErrorMessage.PULL_SUCCESS.print(), peer_2.pull(REPO_NAME));

		files.clear();
		for (int i=4; i<6; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[2], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		peer_2.addFilesToRepository(REPO_NAME, files);
		peer_2.commit(REPO_NAME, COMMIT_TEST);
		peer_2.push(REPO_NAME);

		peer_3.createRepository(REPO_NAME, dirs[3]);
		assertEquals(ErrorMessage.PULL_SUCCESS.print(), peer_3.pull(REPO_NAME));

		files.clear();
		for (int i=6; i<8; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[3], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_3));
			files.add(f);
		}

		peer_3.addFilesToRepository(REPO_NAME, files);
		peer_3.commit(REPO_NAME, COMMIT_TEST);
		peer_3.push(REPO_NAME);
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

		files.clear();
		for (int i=4; i<6; i++) {
			String name = "file_"+(i+1);
			File f = new File(dirs[2], name + ".txt");
			FileUtils.writeLines(f, Collections.singleton(TEXT_1));
			files.add(f);
		}

		peer_2.addFilesToRepository(REPO_NAME, files);
		peer_2.commit(REPO_NAME, COMMIT_TEST);
		peer_2.push(REPO_NAME);

		master_peer.pull(REPO_NAME);

		assertEquals(ErrorMessage.PULL_NO_UPDATE.print(), peer_2.pull(REPO_NAME));
		assertEquals(ErrorMessage.PULL_NO_UPDATE.print(), master_peer.pull(REPO_NAME));

	}

	@After
	public void tearDown() throws Exception {
		master_peer.leaveNetwork();
		peer_1.leaveNetwork();
		peer_2.leaveNetwork();
		peer_3.leaveNetwork();
	}
}