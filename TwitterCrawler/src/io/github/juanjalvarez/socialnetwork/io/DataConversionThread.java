package io.github.juanjalvarez.socialnetwork.io;

import io.github.juanjalvarez.socialnetwork.toolbox.Listing;

/**
 * A class meant to house a DataConverter and automatically fetch source data
 * and convert it into target data.
 * 
 * @author Juan J. Alvarez <juanalvarez2@mail.usf.edu>
 *
 * @param <X>
 *            The data type of the source data
 * @param <Y>
 *            The data type of the target data
 */
public final class DataConversionThread<X, Y> implements Runnable {

	private DataConverter<X, Y> converter;
	private Listing<X> source;
	private Listing<Y> target;
	private int start;
	private int end;
	private int index;
	private Thread thread;

	public DataConversionThread(DataConverter<X, Y> converterInterface, Listing<X> sourceListing,
			Listing<Y> targetListing, int startIndex, int endIndex) {
		converter = converterInterface;
		source = sourceListing;
		target = targetListing;
		start = startIndex;
		end = endIndex;
		index = start;
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public boolean isAlive() {
		return thread.isAlive();
	}

	private synchronized int idx() {
		return index;
	}

	private synchronized void incIdx() {
		index++;
	}

	public synchronized double progress() {
		return (double) (idx() - start) / (double) (end - start);
	}

	@Override
	public void run() {
		for (; idx() < end; incIdx())
			target.set(idx(), converter.convert(source.get(idx())));
	}
}