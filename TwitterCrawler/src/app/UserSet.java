package app;

import twitter4j.User;

class UserSet {

	private String id;
	private User a;
	private User b;

	public UserSet(User a, User b) {
		this.a = a;
		this.b = b;
		id = Utils.generateKey(a, b);
	}

	public User getA() {
		return a;
	}

	public User getB() {
		return b;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserSet) {
			UserSet us = (UserSet) obj;
			return us.getId().equals(id);
		}
		return false;
	}
}