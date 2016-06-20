package io.github.juanjalvarez.socialnetwork.io;

/**
 * An interface representing the structure of a data converter.
 * 
 * @author Juan J. Alvarez <juanalvarez2@mail.usf.edu>
 *
 * @param <X>
 *            The data type of the source data.
 * @param <Y>
 *            The data type of the target data.
 */
public interface DataConverter<X, Y> {

	/**
	 * Yields data of type Y from the source data of type X.
	 * 
	 * @param source
	 *            Source X data to be converted into Y data.
	 * @return The source X data converted into it's Y data equivalent.
	 */
	public abstract Y convert(X source);
}