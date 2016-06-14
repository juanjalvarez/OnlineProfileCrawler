package io.github.juanjalvarez.socialnetwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.juanjalvarez.socialnetwork.io.LoggingStream;
import io.github.juanjalvarez.socialnetwork.toolbox.Algorithms;
import twitter4j.User;

/**
 * A structure that represents an algorithm meant for comparing any two users
 * from any two potentially different social networks.
 * 
 * @author Juan J. Alvarez <juanalvarez2@mail.usf.edu>
 *
 */
public abstract class ComparingAlgorithm implements Runnable {

	private String name;
	private Thread thread;

	/**
	 * Initializes the user comparison algorithm with it's name.
	 * 
	 * @param name
	 *            Name of the comparison algorithm.
	 */
	public ComparingAlgorithm(String name) {
		this.name = name;
	}

	/**
	 * Compares the two given users and yields a weight value that represents
	 * the probability of the two users being the same.
	 * 
	 * @param a
	 *            First user.
	 * @param b
	 *            Second user.
	 * @return Weight value representing the probability of both users being the
	 *         same entity.
	 */
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
	public void match() {
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Proves all possible relations within the current set of users using the
	 * current algorithm in a separate thread.
	 */
	@Override
	public void run() {
		int x, y;
		long start = System.currentTimeMillis();
		double hit = 0.0;
		User a, b;
		for (x = 0; x < userArr.length; x++)
			for (y = x; y < userArr.length; y++)
				if (userArr[x].getId() != userArr[y].getId()) {
					a = userArr[x];
					b = userArr[y];
					hit = compare(userArr[x], userArr[y]);
					proveRelation(a, b, hit);
				}
		System.out.println(String.format("Comparison took %d seconds with the '%s' algorithm",
				(System.currentTimeMillis() - start) / 1000, name));
		finishAlgorithm();
	}

	/**
	 * Contains algorithms for comparing any two users.
	 */
	public static ComparingAlgorithm[] algorithmList = {

			new ComparingAlgorithm("Soundex similarity test") {
				/**
				 * Converts the uppercase version of both users' names into
				 * soundex codes, returns the equality ratio of the names.
				 */
				@Override
				public double compare(User a, User b) {
					String[] aName = a.getName().split(" ");
					String[] bName = a.getName().split(" ");
					int x;
					for (x = 0; x < aName.length; x++)
						aName[x] = Algorithms.soundex(aName[x].toUpperCase());
					for (x = 0; x < bName.length; x++)
						bName[x] = Algorithms.soundex(bName[x].toUpperCase());
					int min = Math.min(aName.length, bName.length), score = 0;
					for (x = 0; x < min; x++)
						if (aName[x].equals(bName[x]))
							score++;
					return (double) score / (double) min;
				}
			}, new ComparingAlgorithm("Soundex difference test") {
				/**
				 * Converts the uppercase version of both users' full names into
				 * soundex codes, then it will return the equality ratio of the
				 * soundex codes, 0.0 if they are completely unequal.
				 */
				@Override
				public double compare(User a, User b) {
					String[] aName = a.getName().split(" ");
					String[] bName = a.getName().split(" ");
					int x, y;
					for (x = 0; x < aName.length; x++)
						aName[x] = Algorithms.soundex(aName[x].toUpperCase());
					for (x = 0; x < bName.length; x++)
						bName[x] = Algorithms.soundex(bName[x].toUpperCase());
					int min = Math.min(aName.length, bName.length), score = 0;
					for (x = 0; x < min; x++)
						for (y = 0; y < 4; y++)
							if (aName[x].charAt(y) == bName[x].charAt(y))
								score++;
					return (double) score / ((double) min * 4);
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
				 * real names and usernames, returns the amount of matching
				 * characters divided by the total amount of different
				 * characters in both names.
				 */
				@Override
				public double compare(User a, User b) {
					double weight = 0.0;
					weight += Algorithms.<Character> listSimilarityTest(
							Algorithms.primitiveArrayToWrapperArray(a.getName().toCharArray()),
							Algorithms.primitiveArrayToWrapperArray(b.getName().toCharArray()));
					weight += Algorithms.<Character> listSimilarityTest(
							Algorithms.primitiveArrayToWrapperArray(a.getScreenName().toCharArray()),
							Algorithms.primitiveArrayToWrapperArray(b.getScreenName().toCharArray()));
					return weight;
				}
			}, new ComparingAlgorithm("LCS Name Similarity") {
				/**
				 * Continuously removes the longest common substring between the
				 * two users' real names and user-names until there are no
				 * common substrings left, and then it yields a value equal to
				 * one minus the division of the sum of the lengths of both
				 * resulting strings and the average of the two original
				 * strings.
				 */
				@Override
				public double compare(User a, User b) {
					double weight = 0.0;
					weight += Algorithms.LCSStringSimilarity(a.getName(), b.getName());
					weight += Algorithms.LCSStringSimilarity(a.getScreenName(), b.getScreenName());
					return weight;
				}
			}, new ComparingAlgorithm("Language comparison") {
				/**
				 * Compares the languages of the two users and yields a weight
				 * value of 1.0 if they are equal, 0.0 otherwise.
				 */
				@Override
				public double compare(User a, User b) {
					if (a.getLang().equals(b.getLang()))
						return 1.0;
					return 0.0;
				}
			}, new ComparingAlgorithm("Timezone comparison") {
				/**
				 * Compares the timezones of both users, yields a weight value
				 * of 1.0 if their timezones match, 0.0 otherwise.
				 */
				@Override
				public double compare(User a, User b) {
					String za = a.getTimeZone();
					String zb = b.getTimeZone();
					if (za == null || zb == null)
						return 0.0;
					if (a.getTimeZone().equals(b.getTimeZone()))
						return 1.0;
					return 0.0;
				}
			}, new ComparingAlgorithm("Country presence") {
				/**
				 * Compares the list of countries that both users have been to
				 * and yields the match ratio.
				 */
				@Override
				public double compare(User a, User b) {
					return Algorithms.<String> listSimilarityTest(a.getWithheldInCountries(),
							b.getWithheldInCountries());
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
	 * Represents the amount of algorithms that have finished computing.
	 */
	public static int finishedAlgorithms = 0;

	/**
	 * Increments the amount of algorithms that have finished computing.
	 */
	public static synchronized void finishAlgorithm() {
		finishedAlgorithms++;
	}

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
	public static synchronized double getRelation(User a, User b) {
		String key = Algorithms.generateKey(a, b);
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
	public static synchronized void proveRelation(User a, User b, double percent) {
		String key = Algorithms.generateKey(a, b);
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
		PrintStream ps = new PrintStream(new LoggingStream());
		System.setOut(ps);
		System.setErr(ps);
		File[] dataFile = TwitterCrawler.DATA_DIRECTORY.listFiles();
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
		long start = System.currentTimeMillis();
		for (x = 0; x < algorithmList.length; x++)
			algorithmList[x].match();
		while (finishedAlgorithms != algorithmList.length)
			Thread.sleep(100);
		System.out.println(
				String.format("It took %d seconds to finish all tasks", (System.currentTimeMillis() - start) / 1000));
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