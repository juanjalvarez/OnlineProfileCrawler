import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import twitter4j.User;

public class Main {

	public static final File DATA_DIRECTORY = new File("data_repo/");
	public static final File OUTPUT_FILE = new File("output.data");
	public static final File[] DATA_FILE_LIST = DATA_DIRECTORY.listFiles();

	public static ArrayList<User> userList;

	public static void main(String[] arguments) throws Exception {
		if (arguments.length == 0) {
			System.out.println(
					"Execution failed! No arguments were received! Valid arguments are either 'crawl' or a combination of {repetitions, report, compile, cleanup}");
			return;
		}
		if (arguments.length == 1 && arguments[0].equals("crawl"))
			TwitterCrawler.crawl();
		else {
			load();
			for (String s : arguments) {
				long start = System.currentTimeMillis();
				if (s.equals("repetitions"))
					repetitions();
				else if (s.equals("report"))
					report();
				else if (s.equals("compile"))
					compile();
				else if (s.equals("cleanup"))
					cleanup();
				else {
					System.out.printf(
							"'%s' is an invalid argument, valid arguments are either 'crawl' or a combination of {repetitions, report, compile, cleanup}\n",
							s);
				}
				System.out.println(
						String.format("\nOperation took %d seconds", (System.currentTimeMillis() - start) / 1000));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void load() throws Exception {
		System.out.println("\n====================\nLOADING\n====================\n");
		if (!DATA_DIRECTORY.exists()) {
			System.out.println("There is no data");
			return;
		}
		ObjectInputStream ois;
		ArrayList<User> global = new ArrayList<User>(), tmp;
		if (OUTPUT_FILE.exists()) {
			System.out.println("Loading past data");
			ois = new ObjectInputStream(new FileInputStream(OUTPUT_FILE));
			long start = System.currentTimeMillis();
			Object obj = ois.readObject();
			System.out
					.println("Done reading data, it took " + (System.currentTimeMillis() - start) / 1000 + " seconds");
			tmp = (ArrayList<User>) obj;
			global = tmp;
			System.out.println(String.format("Loaded %d users", global.size()));
			ois.close();
		}
		System.out.println("Loading new data");
		File[] dataFileList = DATA_DIRECTORY.listFiles();
		System.out.println(String.format("%d data files identified", dataFileList.length));
		for (File f : dataFileList) {
			System.out.println("Transfering old data from " + f.getName());
			ois = new ObjectInputStream(new FileInputStream(f));
			Object obj = ois.readObject();
			tmp = (ArrayList<User>) obj;
			System.out.println(String.format("%d records transferred", tmp.size()));
			for (User u : tmp)
				global.add(u);
		}
		userList = global;
	}

	public static void compile() throws Exception {
		System.out.println("\n====================\nCOMPILE\n====================\n");
		System.out.println(String.format("Saving %d users", userList.size()));
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(OUTPUT_FILE));
		oos.writeObject(userList);
		oos.close();
	}

	public static void cleanup() throws Exception {
		System.out.println("\n====================\nCLEANUP\n====================\n");
		System.out.println("Deleting old data");
		for (File f : DATA_FILE_LIST) {
			System.out.println("Deleting " + f.getName());
			f.delete();
		}
		System.out.println("Finished deleting old data");
	}

	public static void repetitions() throws Exception {
		System.out.println("\n====================\nREPETITIONS\n====================\n");
		System.out.println("Searching and eliminating repetitions");
		System.out.printf("The list started out with %d users\n", userList.size());
		HashSet<User> list = new HashSet<User>();
		list.addAll(userList);
		Iterator<User> i = list.iterator();
		ArrayList<User> result = new ArrayList<User>();
		while (i.hasNext())
			result.add(i.next());
		System.out.printf("The list ended up with %d users\n", result.size());
		System.out.printf("%d repetitions have been identified\n", userList.size() - result.size());
		userList = result;
	}

	public static void report() throws Exception {
		System.out.println("\n====================\nREPORT\n====================\n");
		PrintWriter pw = new PrintWriter(new File("log.txt"));
		System.out.println("Starting to generate a report");
		double percentage = 0.0;
		User u;
		String tmp;
		int pastPercent = 0;
		for (int x = 0; x < userList.size(); x++) {
			u = userList.get(x);
			percentage = (double) x / (double) userList.size() * 100.0;
			if ((int) percentage != pastPercent)
				System.out.printf("%d%% done\n", (int) percentage);
			pastPercent = (int) percentage;
			tmp = userToString(u);
			pw.println(tmp);
			pw.println();
		}
		System.out.println();
	}

	private static String userToString(User u) {
		if (u == null)
			return "NULL USER";
		StringBuilder sb = new StringBuilder();
		String[] arr = { "ID: " + u.getId(), "\tUsername: " + u.getName(), "\tScreen name: " + u.getScreenName(),
				"\tDescription: " + u.getDescription(), "\tFavourites: " + u.getFavouritesCount(),
				"\tFollowers: " + u.getFollowersCount(), "\tFriends: " + u.getFriendsCount(),
				"\tLanguage: " + u.getLang(), "\tLocation: " + u.getLocation(), "\tStatuses: " + u.getStatusesCount(),
				"\tTimezone: " + u.getTimeZone(), "\tURL: " + u.getURL(),
				"\tDate created: " + (u.getCreatedAt() == null ? "null" : u.getCreatedAt().toString()),
				"\tLatest status: " + (u.getStatus() == null ? "null" : u.getStatus().getText()) };
		for (String s : arr)
			sb.append(s).append("\n");
		return sb.toString();
	}
}