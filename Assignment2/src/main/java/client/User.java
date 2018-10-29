 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 2 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Java IDL (CORBA)
 */
package client;

import util.Constants;
import util.Department;
import util.Role;

/**
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public class User {

	private Department dept;
	private Role role;
	private int id;

	User() {
		
	}
	User(Department dept, Role role, int id) {
		this.dept = dept;
		this.role = role;
		this.id = id;
	}
	
	/**
	 * @return the dept
	 */
	public Department getDept() {
		return dept;
	}

	/**
	 * @return the role
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param dept the dept to set
	 */
	public void setDept(Department dept) {
		this.dept = dept;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return dept + Constants.EMPTYSTRING + role + Constants.EMPTYSTRING + id;
	}
	
}
