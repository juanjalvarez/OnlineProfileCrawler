package io.github.juanjalvarez.socialnetwork.io;

import java.util.Stack;

public final class DataConversionThread<X, Y> implements Runnable {

	private DataConverter<X, Y> loader;
	private Stack<X> dataStack;
	private Stack<Y> resultStack;
	private Thread thread;

	public DataConversionThread(DataConverter<X, Y> l, Stack<X> dStack, Stack<Y> rStack) {
		loader = l;
		dataStack = dStack;
		resultStack = rStack;
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public boolean isAlive() {
		return thread.isAlive();
	}

	@Override
	public synchronized void run() {
		while (!dataStack.isEmpty())
			resultStack.add(loader.loadResource(dataStack.pop()));
	}
}