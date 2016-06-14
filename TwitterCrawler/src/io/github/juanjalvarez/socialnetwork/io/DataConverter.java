package io.github.juanjalvarez.socialnetwork.io;

public interface DataConverter<X, Y> {

	public abstract Y loadResource(X source);
}