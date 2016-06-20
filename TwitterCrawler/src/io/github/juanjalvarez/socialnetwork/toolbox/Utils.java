package io.github.juanjalvarez.socialnetwork.toolbox;

import java.io.File;
import java.util.HashSet;

/**
 * A collection of utilities used throughout the applciation.
 * 
 * @author Juan J. Alvarez <juanalvarez2@mail.usf.edu>
 *
 */
public class Utils {

	/**
	 * Creates a string repeating the target string rep amount of times.
	 * 
	 * @param target
	 *            String to be repeated.
	 * @param rep
	 *            Amount of times to repeat the string.
	 * @return String with rep amount of repetitions of the target string.
	 */
	public static String strRepeat(String target, int rep) {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < rep; x++)
			sb.append(target);
		return sb.toString();
	}

	/**
	 * Yields the name of a file without it's extension.
	 * 
	 * @param f
	 *            File which to get the name from.
	 * @return Name of the file given without it's extension.
	 */
	public static String getFileName(File f) {
		if (f == null)
			return null;
		String name = f.getName();
		return name.split("\\.")[0];
	}

	/**
	 * Yields the extension of a file without the period or the name.
	 * 
	 * @param f
	 *            File which to get the extension from.
	 * @return Extension of the given file f without the period or the name.
	 */
	public static String getFileExtension(File f) {
		if (f == null)
			return null;
		String[] name = f.getName().split("\\.");
		return name.length > 1 ? name[1] : name[0];
	}

	/**
	 * Yields a list representing the unique values in the given list e. This
	 * method is generic and therefore is compatible with any kind of Array.
	 * 
	 * @param e
	 *            List which to yield the unique values from.
	 * @return List containing the unique values from e.
	 */
	@SuppressWarnings("unchecked")
	public static <E> E[] getSet(E[] e) {
		HashSet<E> set = new HashSet<E>();
		for (E tmp : e)
			set.add(tmp);
		return set.toArray((E[]) new Object[0]);
	}
}