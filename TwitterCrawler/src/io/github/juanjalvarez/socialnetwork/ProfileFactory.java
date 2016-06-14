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
import twitter4j.User;

public class ProfileFactory {

	public static void convertFromTwitter() {
		if (!CommonProfile.DATA_DIRECTORY.exists())
			CommonProfile.DATA_DIRECTORY.mkdir();
		DataConverter<File, CommonProfile[]> dc = new DataConverter<File, CommonProfile[]>() {
			@Override
			public CommonProfile[] loadResource(File source) {
				ObjectInputStream ois;
				try {
					ois = new ObjectInputStream(new FileInputStream(source));
					@SuppressWarnings("unchecked")
					ArrayList<User> list = (ArrayList<User>) ois.readObject();
					ois.close();
					CommonProfile[] arr = new CommonProfile[list.size()];
					for (int x = 0; x < arr.length; x++)
						arr[x] = CommonProfile.createFromTwitter(list.get(x));
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
							new File(CommonProfile.DATA_DIRECTORY.getName() + "/" + source.getName())));
					oos.writeObject(arr);
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
		dcm.convert(sourceList);
	}

	public static void main(String[] a) {
		convertFromTwitter();
	}
}