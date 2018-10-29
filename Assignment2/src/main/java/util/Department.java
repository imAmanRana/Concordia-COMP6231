 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 2 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Java IDL (CORBA)
 */
package util;

/**
 * Department Enumeration
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public enum Department {

	COMP(4444), SOEN(5555), INSE(6666);
	int udpPort;

	private Department(int udpPort) {
		this.udpPort = udpPort;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public static  boolean departmentExist(String dept) {
		for (Department d : Department.values()) {
			if (d.toString().equals(dept.toUpperCase()))
				return true;
		}
		return false;
	}
}
