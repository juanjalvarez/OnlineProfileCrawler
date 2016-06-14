package io.github.juanjalvarez.socialnetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import twitter4j.User;

@SuppressWarnings("serial")
public class CommonProfile implements Serializable {

	/**
	 * An empty set representing an empty field.
	 */
	public static final String[] NO_DATA = { "NO_DATA" };

	/**
	 * Reference to the directory that will store all of the processed common
	 * profiles.
	 */
	public static final File DATA_DIRECTORY = new File("common_profiles/");

	/**
	 * Keys to access profile information.
	 */
	public static final String TWITTER_ID = "twitter_id";
	public static final String USERNAME = "username";
	public static final String REALNAME = "realname";
	public static final String LOCATION = "location";
	public static final String TIMEZONE = "timezone";
	public static final String UTF_OFFSET = "utc_offset";
	public static final String LANGUAGE = "language";
	public static final String COUNTRIES_VISITED = "countries_visited";
	public static final String DATE_OF_BIRTH = "date_of_birth";
	public static final String DESCRIPTION = "description";

	/**
	 * Keys to access cached profile information.
	 */
	public static final String SOUNDEX_USERNAME = "soundex_username";
	public static final String SOUNDEX_REALNAME = "soundex_realname";
	public static final String CHARACTER_SET_USERNAME = "character_set_username";
	public static final String CHARACTER_SET_REALNAME = "character_set_realname";
	public static final String NAME_SPLIT = "name_split";

	private Map<String, ArrayList<String>> data;

	/**
	 * Initializes an empty data map for the profile.
	 */
	private CommonProfile() {
		data = new HashMap<String, ArrayList<String>>();
	}

	/**
	 * Adds the given value to the list of data associated with the given key.
	 * 
	 * @param key
	 *            Key of the associated list of data.
	 * @param value
	 *            Value to be added to the profile.
	 */
	public void add(String key, String value) {
		ArrayList<String> s = data.get(key);
		if (s == null)
			s = new ArrayList<String>();
		s.add(value);
		data.put(key, s);
	}

	/**
	 * Yields the list of data associated with the given key.
	 * 
	 * @param key
	 *            Key of the list of data to be obtained.
	 * @return List of data associated with the list of data associated with the
	 *         given key.
	 */
	public String[] getList(String key) {
		ArrayList<String> s = data.get(key);
		if (s == null)
			return NO_DATA;
		return s.<String> toArray(NO_DATA);
	}

	/**
	 * Yields the list of unique values associated with the given key.
	 * 
	 * @param key
	 *            Key associated with the list of data to be obtained.
	 * @return List of unique values associated with the given key.
	 */
	public String[] getSet(String key) {
		HashSet<String> set = new HashSet<String>();
		ArrayList<String> al = data.get(key);
		if (al == null)
			return NO_DATA;
		set.addAll(data.get(key));
		return set.<String> toArray(new String[0]);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TWITTER ID: ").append(Arrays.toString(getSet(TWITTER_ID))).append("\n");
		sb.append("\tUSERNAME: ").append(Arrays.toString(getSet(USERNAME))).append("\n");
		sb.append("\tREALNAME: ").append(Arrays.toString(getSet(REALNAME))).append("\n");
		sb.append("\tLOCATION: ").append(Arrays.toString(getSet(LOCATION))).append("\n");
		sb.append("\tTIMEZONE: ").append(Arrays.toString(getSet(TIMEZONE))).append("\n");
		sb.append("\tLANGUAGE: ").append(Arrays.toString(getSet(LANGUAGE))).append("\n");
		sb.append("\tCOUNTRIES: ").append(Arrays.toString(getSet(COUNTRIES_VISITED))).append("\n");
		return sb.toString();
	}

	/**
	 * Forms a profile from the data in a Twitter user.
	 * 
	 * @param u
	 *            Twitter profile to harvest data from.
	 * @return A profile based on the data from the Twitter user.
	 */
	public static CommonProfile createFromTwitter(User u) {
		CommonProfile p = new CommonProfile();
		p.add(USERNAME, u.getScreenName());
		p.add(REALNAME, u.getName());
		p.add(LANGUAGE, u.getLang());
		p.add(LOCATION, u.getLocation());
		p.add(TIMEZONE, u.getTimeZone());
		p.add(UTF_OFFSET, Integer.toString(u.getUtcOffset()));
		p.add(TWITTER_ID, Long.toString(u.getId()));
		p.add(DESCRIPTION, u.getDescription());
		if (u.getWithheldInCountries() != null)
			for (String s : u.getWithheldInCountries())
				p.add(COUNTRIES_VISITED, s);
		return p;
	}

	public static void main(String[] a) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("data_repo/1.data")));
		@SuppressWarnings("unchecked")
		ArrayList<User> list = (ArrayList<User>) ois.readObject();
		for (User u : list)
			System.out.println(createFromTwitter(u).toString());
		ois.close();
	}
}