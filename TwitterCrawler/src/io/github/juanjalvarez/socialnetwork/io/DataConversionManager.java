package io.github.juanjalvarez.socialnetwork.io;

import java.util.ArrayList;
import java.util.Stack;

public final class DataConversionManager<X, Y> {

	private ArrayList<DataConversionThread<X, Y>> thread;
	private DataConverter<X, Y> loader;

	public DataConversionManager(DataConverter<X, Y> l) {
		thread = new ArrayList<DataConversionThread<X, Y>>();
		loader = l;
	}

	public Y[] convert(X[] data) {
		long start = System.currentTimeMillis();
		int x, cpus = Runtime.getRuntime().availableProcessors();
		Stack<X> dStack = new Stack<X>();
		for (X tmp : data)
			dStack.push(tmp);
		Stack<Y> rStack = new Stack<Y>();
		for (x = 0; x < cpus; x++)
			thread.add(new DataConversionThread<X, Y>(loader, dStack, rStack));
		for (DataConversionThread<X, Y> l : thread)
			l.start();
		int alive = 1;
		while (alive > 0) {
			alive = thread.size();
			for (x = 0; x < thread.size(); x++)
				if (!thread.get(x).isAlive())
					alive--;
			System.out.print(String.format("\nData conversion: %.2f%% (%d/%d)",
					100.0 * (double) rStack.size() / (double) data.length, rStack.size(), data.length));
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(
				String.format("Finished converting in %d seconds", (System.currentTimeMillis() - start) / 1000));
		System.out.println();
		@SuppressWarnings("unchecked")
		Y[] arr = (Y[]) new Object[data.length];
		for (x = 0; x < data.length; x++)
			arr[x] = rStack.pop();
		return arr;
	}
}
