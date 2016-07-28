package io.github.juanjalvarez.socialnetwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class FamousTwitterCrawler implements Runnable {

	public static final Twitter TWITTER = TwitterFactory.getSingleton();
	public static final File DATA_DIRECTORY = new File("twitter_profiles_f/");
	public static final BufferedReader KEYBOARD = new BufferedReader(new InputStreamReader(System.in));

	static {
		if (!DATA_DIRECTORY.exists())
			DATA_DIRECTORY.mkdir();
	}

	private Thread thread;

	public static void crawl() throws Exception {
		System.out.println("\n====================\nCRAWLER\n====================\n");
		PrintStream ps = new PrintStream(new LoggingStream());
		System.setOut(ps);
		System.setErr(ps);
		new FamousTwitterCrawler();
	}

	public FamousTwitterCrawler() {
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		System.out.println("Starting the crawler");
		while (true) {
			try {
				System.out.println("Please enter the name of the user to search for ('end' to stop the process)");
				String line = KEYBOARD.readLine();
				String fileName = DATA_DIRECTORY.getName() + "\\" + line + ".data";
				if (line.equals("end"))
					break;
				line = line.replaceAll(" ", "%20");
				System.out.println(
						"Please enter the number of users you would like to search for (whatever you enter multiplied by 20)");
				int amount = Integer.parseInt(KEYBOARD.readLine()), x;
				ResponseList<User> data = null, tmp;
				for (x = 0; x < amount; x++) {
					tmp = TWITTER.searchUsers(line, x);
					if (data == null)
						data = tmp;
					else
						data.addAll(tmp);
				}
				System.out.printf("Managed to acquire %d user profiles\n", data.size());
				System.out.println("Saving the current subset of users");
				ArrayList<User> list = new ArrayList<User>();
				for (User u : data) {
					list.add(u);
					System.out.println(u.getScreenName() + " ~ " + u.getName());
				}
				File newFile = new File(fileName);
				newFile.createNewFile();
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(newFile));
				oos.writeObject(list);
				oos.close();
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

	public static void main(String[] arguments) throws Exception {
		FamousTwitterCrawler.crawl();
	}
}