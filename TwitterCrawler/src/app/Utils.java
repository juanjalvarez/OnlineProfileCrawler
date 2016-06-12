package app;

import java.util.HashSet;

import twitter4j.User;

public class Utils {

	public static String soundex(String s) {
		char[] charArray = s.toUpperCase().toCharArray();
		if (charArray.length == 0)
			return "ZERO";
		char firstChar = charArray[0];
		for (int i = 0; i < charArray.length; i++) {
			switch (charArray[i]) {
			case 'B':
			case 'F':
			case 'P':
			case 'V': {
				charArray[i] = '1';
				break;
			}
			case 'C':
			case 'G':
			case 'J':
			case 'K':
			case 'Q':
			case 'S':
			case 'X':
			case 'Z': {
				charArray[i] = '2';
				break;
			}
			case 'D':
			case 'T': {
				charArray[i] = '3';
				break;
			}

			case 'L': {
				charArray[i] = '4';
				break;
			}
			case 'M':
			case 'N': {
				charArray[i] = '5';
				break;
			}
			case 'R': {
				charArray[i] = '6';
				break;
			}
			default: {
				charArray[i] = '0';
				break;
			}
			}
		}
		StringBuilder sb = new StringBuilder();
		if (Character.isAlphabetic(firstChar))
			sb.append(firstChar);
		else
			sb.append('0');
		for (int i = 1; i < charArray.length; i++)
			if (charArray[i] != charArray[i - 1] && charArray[i] != '0')
				sb.append(charArray[i]);
		String result = sb.toString();
		return result.length() < 4 ? result + strRepeat("0", 4 - result.length())
				: result.length() > 4 ? result.substring(0, 4) : result;
	}

	private static String strRepeat(String s, int rep) {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < rep; x++)
			sb.append(s);
		return sb.toString();
	}

	public static String generateKey(User a, User b) {
		if (b.getId() > a.getId()) {
			User tmp = a;
			a = b;
			b = tmp;
		}
		return a.getId() + "_" + b.getId();
	}

	public static String eliminateRepeatedCharacters(String s) {
		HashSet<Character> map = new HashSet<Character>();
		for (Character c : s.toCharArray())
			map.add(c);
		StringBuilder sb = new StringBuilder();
		for (Character c : map)
			sb.append(c);
		return sb.toString();
	}
}