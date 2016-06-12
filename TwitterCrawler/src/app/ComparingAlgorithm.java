package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import twitter4j.User;

/**
 * A structure that represents an algorithm meant for comparing any two users
 * from any two potentially different social networks.
 * 
 * @author Juan J. Alvarez <juanalvarez2@mail.usf.edu>
 *
 */
public abstract class ComparingAlgorithm {

	private String name;

	/**
	 * Initializes the user comparison algorithm with it's name.
	 * 
	 * @param name
	 *            Name of the comparison algorithm.
	 */
	public ComparingAlgorithm(String name) {
		this.name = name;
	}

	public abstract double compare(User a, User b);

	/**
	 * Given a list of users, this algorithm will compare every user with every
	 * other user with every user comparison algorithm available. The algorithm
	 * wil also add the proven relation given from the comparison algorithms to
	 * a relation score between those two users.
	 * 
	 * @param list
	 *            List of users to cross reference.
	 */
	public void match(User[] list) {
		int x, y;
		long start = System.currentTimeMillis();
		double hit = 0.0;
		User a, b;
		for (x = 0; x < list.length; x++)
			for (y = x; y < list.length; y++)
				if (list[x].getId() != list[y].getId()) {
					a = list[x];
					b = list[y];
					hit = compare(list[x], list[y]);
					proveRelation(a, b, hit);
				}
		System.out.println(String.format("Comparison took %d seconds with the '%s' algorithm",
				(System.currentTimeMillis() - start) / 1000, name));
	}

	/**
	 * Contains algorithms for comparing any two users.
	 */
	public static ComparingAlgorithm[] algorithmList = {

			new ComparingAlgorithm("Soundex test") {
				/**
				 * Converts the lowercase version of both user's first name into
				 * a soundex code, returns 1.0 if they are equal, 0.0 otherwise.
				 */
				@Override
				public double compare(User a, User b) {
					String aName = a.getName().split(" ")[0].toLowerCase();
					String bName = b.getName().split(" ")[0].toLowerCase();
					String aSoundex = Utils.soundex(aName);
					String bSoundex = Utils.soundex(bName);
					if (aSoundex.equals(bSoundex))
						return 1.0;
					return 0.0;
				}
			}, new ComparingAlgorithm("Equality test") {
				/**
				 * Compares both user's first names in their lower case
				 * equivalent forms and returns 1.0 if they are equal, 0.0
				 * otherwise.
				 */
				@Override
				public double compare(User a, User b) {
					String firstA = a.getName().split(" ")[0].toLowerCase();
					String firstB = a.getName().split(" ")[0].toLowerCase();
					if (firstA.equals(firstB))
						return 1.0;
					return 0.0;
				}
			}, new ComparingAlgorithm("Character set counter") {
				/**
				 * Compares the amount of similar characters between both users'
				 * real names, returns the amount of matching characters divided
				 * by the total amount of different characters in both names.
				 */
				@Override
				public double compare(User a, User b) {
					HashSet<Character> set = new HashSet<Character>();
					for (Character c : Utils.eliminateRepeatedCharacters(a.getName().toLowerCase()).toCharArray())
						set.add(c);
					double hits = 0.0;
					for (Character c : Utils.eliminateRepeatedCharacters(b.getName().toLowerCase()).toCharArray()) {
						if (set.contains(c)) {
							hits += 1.0;
						}
						set.add(c);
					}
					double score = hits / (double) set.size();
					return score;
				}
			}

	};

	/**
	 * List of loaded users.
	 */
	public static User[] userArr;

	/**
	 * A map that stores the relation score between any two users, the String
	 * key is generated with the Utils.generateKey method.
	 */
	public static HashMap<String, Double> relationMap = new HashMap<String, Double>();

	/**
	 * Yields the relation score between any two users.
	 * 
	 * @param a
	 *            The first user to yield the relation score from.
	 * @param b
	 *            The second user to yield the relation score from.
	 * @return A double representing the relation score between the two given
	 *         users.
	 */
	public static double getRelation(User a, User b) {
		String key = Utils.generateKey(a, b);
		Double d = relationMap.get(key);
		if (d == null) {
			d = 0.0;
			relationMap.put(key, d);
		}
		return d.doubleValue();
	}

	/**
	 * Furthermore proves the relation between the two given users by the given
	 * score.
	 * 
	 * @param a
	 *            First user to prove the relation score for.
	 * @param b
	 *            Second user to prove the relation score for.
	 * @param percent
	 *            Score to be added to the relation score between the two given
	 *            users.
	 */
	public static void proveRelation(User a, User b, double percent) {
		String key = Utils.generateKey(a, b);
		Double d = relationMap.get(key);
		if (d == null)
			d = percent;
		else
			d = d.doubleValue() + percent;
		relationMap.put(key, d);
	}

	/**
	 * Entry point of the software.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             When there is an issue reading from the data_repo directory.
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		PrintStream ps = new PrintStream(new CrawlerLogStream());
		System.setOut(ps);
		System.setErr(ps);
		File data_dir = new File("data_repo");
		File[] dataFile = data_dir.listFiles();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.printf("How many multiples of 200 users would you like to load? (%d sets are available)\n",
				dataFile.length);
		int amountOfDataToLoad = Integer.parseInt(br.readLine()), x, y;
		userArr = new User[200 * amountOfDataToLoad];
		ObjectInputStream ois;
		ArrayList<User> tmpUserList;
		for (x = 0; x < amountOfDataToLoad; x++) {
			ois = new ObjectInputStream(new FileInputStream(dataFile[x]));
			tmpUserList = (ArrayList<User>) ois.readObject();
			for (y = 0; y < tmpUserList.size(); y++)
				userArr[200 * x + y] = tmpUserList.get(y);
			ois.close();
		}
		System.out.println(userArr.length + " users loaded");
		for (x = 0; x < algorithmList.length; x++)
			algorithmList[x].match(userArr);
		int[] maxIdx = new int[userArr.length];
		double curRelation, maxRelation;
		for (x = 0; x < userArr.length; x++) {
			maxIdx[x] = x;
			for (y = 0; y < userArr.length; y++) {
				if (x == y)
					continue;
				maxRelation = getRelation(userArr[x], userArr[maxIdx[x]]);
				curRelation = getRelation(userArr[x], userArr[y]);
				if (curRelation > maxRelation)
					maxIdx[x] = y;
			}
		}
		HashMap<User, User> matchMap = new HashMap<User, User>();
		User a, b, tmp;
		for (x = 0; x < userArr.length; x++)
			for (y = 0; y < userArr.length; y++) {
				if (maxIdx[x] == y && maxIdx[y] == x) {
					a = userArr[x];
					b = userArr[y];
					if (b.getId() < a.getId()) {
						tmp = a;
						a = b;
						b = tmp;
					}
					matchMap.put(a, b);
				}
			}
		for (User u : matchMap.keySet()) {
			a = u;
			b = matchMap.get(u);
			System.out.println(String.format("'%s'@'%s' matched '%s'@'%s' with a score of %.2f", a.getName(),
					a.getScreenName(), b.getName(), b.getScreenName(), getRelation(a, b)));
		}
		System.out.println(String.format("A total of %d entity matches were identified", matchMap.keySet().size()));
		br.close();
	}
}