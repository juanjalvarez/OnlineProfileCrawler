import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import twitter4j.User;

public class Main {

	public static final File DATA_DIRECTORY = new File("data_repo/");
	public static final File OUTPUT_FILE = new File("output.data");

	@SuppressWarnings("unchecked")
	public static void main(String[] arguments) throws Exception {
		if (!DATA_DIRECTORY.exists()) {
			System.out.println("There is no data");
			return;
		}
		ObjectInputStream ois;
		ArrayList<User> global = new ArrayList<User>(), tmp;
		if (OUTPUT_FILE.exists()) {
			System.out.println("Loading past data");
			ois = new ObjectInputStream(new FileInputStream(OUTPUT_FILE));
			Object obj = ois.readObject();
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
		System.out.println("Searching and eliminating repetitions");
		Set<Long> set = new HashSet<Long>();
		ArrayList<User> result = new ArrayList<User>();
		int reps = 0;
		for (User u : global) {
			if (set.contains(u.getId()))
				reps++;
			else{
				set.add(u.getId());
				result.add(u);
			}
		}
		System.out.println(reps + " repetitions found");
		System.out.println(String.format("Saving %d users", result.size()));
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(OUTPUT_FILE));
		oos.writeObject(result);
		oos.close();
		System.out.println("Deleting old data");
		for(File f : dataFileList){
			f.delete();
		}
	}
}