package io.github.juanjalvarez.socialnetwork.io;

import java.util.ArrayList;

import io.github.juanjalvarez.socialnetwork.toolbox.Listing;

public final class DataConversionManager<X, Y> {

	private ArrayList<DataConversionThread<X, Y>> thread;
	private DataConverter<X, Y> loader;

	public DataConversionManager(DataConverter<X, Y> l) {
		thread = new ArrayList<DataConversionThread<X, Y>>();
		loader = l;
	}

	public Y[] convert(X[] data, boolean progress, int waitTime) {
		if (waitTime < 10)
			waitTime = 10;
		long start = System.currentTimeMillis();
		int x, cpus = Runtime.getRuntime().availableProcessors(), div = data.length / cpus, rem = data.length % cpus,
				amountOfData, pointer = 0, alive = 1;
		double avgProgress;
		Listing<X> source = new Listing<X>(data);
		Listing<Y> target = new Listing<Y>(data.length);
		for (x = 0; x < cpus; x++) {
			amountOfData = div;
			if (rem > 0) {
				amountOfData++;
				rem--;
			}
			thread.add(new DataConversionThread<X, Y>(loader, source, target, pointer, pointer + amountOfData));
			pointer += amountOfData;
		}
		for (DataConversionThread<X, Y> l : thread)
			l.start();
		while (alive > 0) {
			alive = thread.size();
			for (x = 0; x < thread.size(); x++)
				if (!thread.get(x).isAlive())
					alive--;
			avgProgress = 0.0;
			for (DataConversionThread<X, Y> l : thread)
				avgProgress += l.progress();
			avgProgress /= (double) thread.size();
			if (progress)
				System.out.print(String.format("\rData conversion: %.2f%%", avgProgress * 100));
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println();
		if (progress) {
			System.out.println(
					String.format("Finished converting in %d seconds", (System.currentTimeMillis() - start) / 1000));
		}
		return target.getArray();
	}
}
