 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 2 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Java IDL (CORBA)
 */
package util;

/**
 * Roles Enumeration
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public enum Role {

	STUDENT("S"), ADVISOR("A");

	private String value;

	Role(final String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return value;
	}

	public static Role fromString(String text) {
		for (Role b : Role.values()) {
			if (b.value.equalsIgnoreCase(text)) {
				return b;
			}
		}
		throw new IllegalArgumentException(text);
	}
}
