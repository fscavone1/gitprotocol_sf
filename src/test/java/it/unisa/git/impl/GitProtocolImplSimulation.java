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

public class GitProtocolImplSimulation {

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

	public GitProtocolImplSimulation() throws Exception {
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

	@After
	public void tearDown() throws Exception {
		master_peer.leaveNetwork();
		peer_1.leaveNetwork();
		peer_2.leaveNetwork();
		peer_3.leaveNetwork();
		FileUtils.deleteDirectory(new File(PATH));
	}

	@Test
	public void execution() throws IOException {

		//STEP 1: the MASTER-PEER creates a repository and pushes a file

		assertTrue(master_peer.createRepository(REPO_NAME, dirs[0]));

		List<File> files = new ArrayList<>();
		File f = new File(dirs[0],  "file_1.txt");
		FileUtils.writeLines(f, Collections.singleton(TEXT_1));
		files.add(f);

		assertTrue(master_peer.addFilesToRepository(REPO_NAME, files));
		assertTrue(master_peer.commit(REPO_NAME, COMMIT_TEST));
		assertEquals(ErrorMessage.PUSH_SUCCESS.print(),master_peer.push(REPO_NAME));

		System.out.println("---------- STEP 1 ----------");

		System.out.println("REPOSITORY MASTER PEER:");
		System.out.println(master_peer.getRepository().getFileMap().keySet());

		//STEP 2: the PEER-1 creates a repository, pulls from the network, modifies a file, adds a new one and then
		//pushes the changes into the network

		assertTrue(peer_1.createRepository(REPO_NAME, dirs[1]));
		assertEquals(ErrorMessage.PULL_SUCCESS.print(), peer_1.pull(REPO_NAME));

		files.clear();
		File f2 = new File(dirs[1],  "file_1.txt");
		File f3 = new File(dirs[1],  "file_2.txt");
		FileUtils.writeLines(f2, Collections.singleton(TEXT_2));
		FileUtils.writeLines(f3, Collections.singleton(TEXT_1));
		files.add(f2);
		files.add(f3);

		assertTrue(peer_1.addFilesToRepository(REPO_NAME, files));
		assertTrue(peer_1.commit(REPO_NAME, COMMIT_TEST));
		assertEquals(ErrorMessage.PUSH_SUCCESS.print(), peer_1.push(REPO_NAME));

		System.out.println("---------- STEP 2 ----------");

		System.out.println("REPOSITORY MASTER PEER:");
		System.out.println(master_peer.getRepository().getFileMap().keySet());
		System.out.println("REPOSITORY PEER 1:");
		System.out.println(peer_1.getRepository().getFileMap().keySet());

		//STEP 3: the PEER- 2 creates a repository and generates a "push conflict" because it
		//pushes some changes into the network before a pull.

		assertTrue(peer_2.createRepository(REPO_NAME, dirs[2]));

		files.clear();
		File f4 = new File(dirs[2],  "file_3.txt");
		FileUtils.writeLines(f4, Collections.singleton(TEXT_3));
		files.add(f4);

		assertTrue(peer_2.addFilesToRepository(REPO_NAME, files));
		assertTrue(peer_2.commit(REPO_NAME, COMMIT_TEST));
		assertEquals(ErrorMessage.PUSH_CONFLICT.print(), peer_2.push(REPO_NAME));
		assertEquals(ErrorMessage.PULL_SUCCESS.print(), peer_2.pull(REPO_NAME));

		System.out.println("---------- STEP 3 ----------");

		System.out.println("REPOSITORY MASTER PEER:");
		System.out.println(master_peer.getRepository().getFileMap().keySet());
		System.out.println("REPOSITORY PEER 1:");
		System.out.println(peer_1.getRepository().getFileMap().keySet());
		System.out.println("REPOSITORY PEER 2:");
		System.out.println(peer_2.getRepository().getFileMap().keySet());

		//STEP 4: The PEER-3 pushes something new to the network, before the push of PEER-2.
		assertTrue(peer_3.createRepository(REPO_NAME, dirs[3]));
		assertEquals(ErrorMessage.PULL_SUCCESS.print(), peer_3.pull(REPO_NAME));

		files.clear();
		File f5 = new File(dirs[1],  "file_5.txt");
		FileUtils.writeLines(f5, Collections.singleton(TEXT_2));
		files.add(f5);

		assertTrue(peer_3.addFilesToRepository(REPO_NAME, files));
		assertTrue(peer_3.commit(REPO_NAME, COMMIT_TEST));
		assertEquals(ErrorMessage.PUSH_SUCCESS.print(), peer_3.push(REPO_NAME));

		System.out.println("---------- STEP 4 ----------");

		System.out.println("REPOSITORY MASTER PEER:");
		System.out.println(master_peer.getRepository().getFileMap().keySet());
		System.out.println("REPOSITORY PEER 1:");
		System.out.println(peer_1.getRepository().getFileMap().keySet());
		System.out.println("REPOSITORY PEER 2:");
		System.out.println(peer_2.getRepository().getFileMap().keySet());
		System.out.println("REPOSITORY PEER 3:");
		System.out.println(peer_3.getRepository().getFileMap().keySet());

		//STEP 5: At this point, the PEER-2 generates a new push conflict. A new pull resolves the confict and it
		// can pushes its changes to the network. In the end all peers pulls the changes into their local repositories.

		assertEquals(ErrorMessage.PUSH_CONFLICT.print(), peer_2.push(REPO_NAME));
		assertEquals(ErrorMessage.PULL_SUCCESS.print(), peer_2.pull(REPO_NAME));
		assertEquals(ErrorMessage.PUSH_SUCCESS.print(), peer_2.push(REPO_NAME));

		assertEquals(ErrorMessage.PULL_SUCCESS.print(), master_peer.pull(REPO_NAME));
		assertEquals(ErrorMessage.PULL_SUCCESS.print(), peer_1.pull(REPO_NAME));
		assertEquals(ErrorMessage.PULL_NO_UPDATE.print(), peer_2.pull(REPO_NAME));
		assertEquals(ErrorMessage.PULL_SUCCESS.print(), peer_3.pull(REPO_NAME));

		System.out.println("---------- STEP 5 ----------");

		System.out.println("REPOSITORY MASTER PEER:");
		System.out.println(master_peer.getRepository().getFileMap().keySet());
		System.out.println("REPOSITORY PEER 1:");
		System.out.println(peer_1.getRepository().getFileMap().keySet());
		System.out.println("REPOSITORY PEER 2:");
		System.out.println(peer_2.getRepository().getFileMap().keySet());
		System.out.println("REPOSITORY PEER 3:");
		System.out.println(peer_3.getRepository().getFileMap().keySet());

	}
}