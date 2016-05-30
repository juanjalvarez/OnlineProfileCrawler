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
	public static final File DATA_FILE = new File("unified.data");
	public static final String DATA_DIRECTORY_STRING = "data_repo/";

	public static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	static {
		File dataDir = new File(DATA_DIRECTORY_STRING);
		if (!dataDir.exists())
			dataDir.mkdir();
	}

	private CrawlerData meta;
	private Thread thread;

	public static void main(String[] args) throws Exception {
		PrintStream ps = new PrintStream(new CrawlerStream());
		System.setOut(ps);
		System.setErr(ps);
		new TwitterCrawler();
	}

	public TwitterCrawler() {
		meta = loadMetaData();
		if (meta == null)
			meta = new CrawlerData();
		saveMetaData();
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		while (true) {
			if (!meta.canMakeCall())
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				try {
					meta.registerCall();
					System.out.println("Attempting to acquire a subset of users");
					PagableResponseList<User> data = TWITTER.getFollowersList(meta.getTarget(), meta.getCursor(), 200,
							false, true);
					System.out.printf("Managed to acquire %d user profiles\n", data.size());
					meta.setCursor(data.getNextCursor());
					System.out.println("Saving the current subset of users");
					ArrayList<User> list = new ArrayList<User>();
					for (User u : data)
						list.add(u);
					String fileName = DATA_DIRECTORY_STRING + meta.getSubset() + ".data";
					File newFile = new File(fileName);
					newFile.createNewFile();
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(newFile));
					oos.writeObject(list);
					oos.close();
					System.out.println("Acquired subset #" + meta.getSubset());
					meta.updateSubset();
					saveMetaData();
				} catch (TwitterException e) {
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

	public CrawlerData loadMetaData() {
		System.out.println("Loading metadata");
		if (METADATA_FILE.exists())
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(METADATA_FILE));
				Object obj = ois.readObject();
				ois.close();
				if (!(obj instanceof CrawlerData))
					return null;
				CrawlerData cd = (CrawlerData) obj;
				System.out.println("Successfully loaded metadata:");
				System.out.println(cd.toString());
				return cd;
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