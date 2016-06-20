package io.github.juanjalvarez.socialnetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import twitter4j.User;

public class ProfileLoader {

	public static <X> File getProfileDirectory(Class<X> c) {
		if (User.class.isAssignableFrom(c))
			return TwitterCrawler.DATA_DIRECTORY;
		if (CommonProfile.class.isAssignableFrom(c))
			return CommonProfile.DATA_DIRECTORY;
		return new File("");
	}

	@SuppressWarnings("unchecked")
	public static <X> ArrayList<X> loadProfiles(Class<X> c, int n) {
		ArrayList<X> list = new ArrayList<X>(), tmpList;
		File directory = new File("");
		if (User.class.isAssignableFrom(c))
			directory = TwitterCrawler.DATA_DIRECTORY;
		else if (CommonProfile.class.isAssignableFrom(c))
			directory = CommonProfile.DATA_DIRECTORY;
		File[] fList = directory.listFiles();
		int index = 0;
		ObjectInputStream ois;
		try {
			while (index < fList.length && list.size() < n) {
				ois = new ObjectInputStream(new FileInputStream(fList[index]));
				tmpList = (ArrayList<X>) ois.readObject();
				for (X x : tmpList)
					list.add(x);
				index++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (list.size() > n)
			list.remove(list.size() - 1);
		return list;
	}
}