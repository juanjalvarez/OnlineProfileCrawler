package io.github.juanjalvarez.socialnetwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class TwitterCrawler implements Runnable {

	public static final Twitter TWITTER = TwitterFactory.getSingleton();
	public static final File METADATA_FILE = new File("metadata");
	public static final File DATA_DIRECTORY = new File("twitter_profiles/");
	public static final BufferedReader KEYBOARD = new BufferedReader(new InputStreamReader(System.in));

	static {
		if (!DATA_DIRECTORY.exists())
			DATA_DIRECTORY.mkdir();
	}

	private TwitterCrawlerMetaData meta;
	private Thread thread;

	public static void crawl() throws Exception {
		System.out.println("\n====================\nCRAWLER\n====================\n");
		PrintStream ps = new PrintStream(new LoggingStream());
		System.setOut(ps);
		System.setErr(ps);
		new TwitterCrawler();
	}

	public TwitterCrawler() {
		meta = loadMetaData();
		if (meta == null)
			meta = new TwitterCrawlerMetaData();
		saveMetaData();
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		System.out.println("Starting the crawler");
		while (true) {
			if (!meta.canMakeCall())
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				try {
					PagableResponseList<User> data = TWITTER.getFollowersList(meta.getTarget(), meta.getCursor(), 200,
							false, true);
					meta.registerCall();
					System.out.printf("Managed to acquire %d user profiles\n", data.size());
					meta.setCursor(data.getNextCursor());
					System.out.println("Saving the current subset of users");
					ArrayList<User> list = new ArrayList<User>();
					for (User u : data)
						list.add(u);
					String fileName = DATA_DIRECTORY.getName() + meta.getSubset() + ".data";
					File newFile = new File(fileName);
					newFile.createNewFile();
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(newFile));
					oos.writeObject(list);
					oos.close();
					System.out.println("Acquired subset #" + meta.getSubset());
					meta.updateSubset();
					saveMetaData();
				} catch (TwitterException e) {
					if (e.getErrorCode() == 88) {
						System.out.println(
								"Twitter is refusing to provide data, possibly due to untraced calls, the software will idle for 5 minutes before trying again.");
						System.out.printf("Error message returned from the Twitter API: '%s'\n", e.getErrorMessage());
						try {
							Thread.sleep(300000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					} else
						e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void saveMetaData() {
		System.out.println("Saving medatada");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(METADATA_FILE));
			oos.writeObject(meta);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TwitterCrawlerMetaData loadMetaData() {
		System.out.println("Loading metadata");
		if (METADATA_FILE.exists())
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(METADATA_FILE));
				Object obj = ois.readObject();
				ois.close();
				if (!(obj instanceof TwitterCrawlerMetaData))
					return null;
				TwitterCrawlerMetaData cmd = (TwitterCrawlerMetaData) obj;
				System.out.println("Successfully loaded metadata:");
				System.out.println(cmd.toString());
				return cmd;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		System.out.println("Failed to load metadata");
		return null;
	}
}