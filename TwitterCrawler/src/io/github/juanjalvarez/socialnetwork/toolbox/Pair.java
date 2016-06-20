package io.github.juanjalvarez.socialnetwork.toolbox;

public class Pair<X> {

	private X a;
	private X b;

	public Pair(X a, X b) {
		this.a = a;
		this.b = b;
	}

	public X getA() {
		return a;
	}

	public X getB() {
		return b;
	}

	@Override
	public boolean equals(Object obj) {
		@SuppressWarnings("unchecked")
		Pair<X> p = (Pair<X>) obj;
		if ((p.getA().equals(a) && p.getB().equals(b)) || (p.getA().equals(b) && p.getB().equals(a)))
			return true;
		return false;
	}
}