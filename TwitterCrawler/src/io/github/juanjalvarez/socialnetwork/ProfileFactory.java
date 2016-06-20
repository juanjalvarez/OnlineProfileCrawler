package io.github.juanjalvarez.socialnetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import io.github.juanjalvarez.socialnetwork.io.DataConversionManager;
import io.github.juanjalvarez.socialnetwork.io.DataConverter;
import io.github.juanjalvarez.socialnetwork.toolbox.Utils;
import twitter4j.User;

public class ProfileFactory {

	public static void convertFromTwitter() {
		if (!CommonProfile.DATA_DIRECTORY.exists())
			CommonProfile.DATA_DIRECTORY.mkdir();
		DataConverter<File, CommonProfile[]> dc = new DataConverter<File, CommonProfile[]>() {
			@Override
			public CommonProfile[] convert(File source) {
				ObjectInputStream ois;
				try {
					ois = new ObjectInputStream(new FileInputStream(source));
					@SuppressWarnings("unchecked")
					ArrayList<User> list = (ArrayList<User>) ois.readObject();
					ois.close();
					ArrayList<CommonProfile> newList = new ArrayList<CommonProfile>();
					for (int x = 0; x < list.size(); x++)
						newList.add((CommonProfile.createFromTwitter(list.get(x))));
					ObjectOutputStream oos = new ObjectOutputStream(
							new FileOutputStream(new File(CommonProfile.DATA_DIRECTORY.getName() + "/twitter-"
									+ Utils.getFileName(source) + ".pclu")));
					oos.writeObject(newList);
					oos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		DataConversionManager<File, CommonProfile[]> dcm = new DataConversionManager<File, CommonProfile[]>(dc);
		File[] sourceList = TwitterCrawler.DATA_DIRECTORY.listFiles();
		dcm.convert(sourceList, true, 0);
	}

	public static void main(String[] a) {
		convertFromTwitter();
	}
}