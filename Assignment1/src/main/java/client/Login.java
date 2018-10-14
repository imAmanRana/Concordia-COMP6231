/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 1 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS)
 */
package client;

import java.util.Scanner;

import util.Department;
import util.Role;
import util.Utils;

/**
 * The starting point for the client interaction. The user login is validated
 * here, and accordingly, an advisor or a client thread is started.
 * 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public class Login {

	static Scanner input = new Scanner(System.in);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("WELCOME TO DISTRIBUTED COURSE REGISTRATION SYSTEM");
		System.out.print("Please enter your ID : ");
		String id = input.next();

		User user = new User();
		String value = validateUser(id, user);

		switch (value) {

		case "success":
			System.out.println("Login Successful : " + user);
			Thread t = null;
			if (user.getRole() == Role.STUDENT) {
				t = new Thread(new StudentClient(user));
			} else {
				t = new Thread(new AdvisorClient(user));
			}
			t.start();
			break;
		default:
			System.out.println(value);
			break;
		}

	}

	/**
	 * Performs validation on user input. Its case insensitive.
	 * 
	 * @param id   user id
	 * @param user an empty user object
	 * @return string message
	 */
	private static String validateUser(final String id, final User user) {
		String returnValue = null, dept, role, value;
		int userId;
		// string length !=9
		if (id.length() != 9)
			return "Seems to be an invalid id(length not equal to 9).";

		dept = id.substring(0, 4);
		role = id.substring(4, 5);
		value = id.substring(5);

		// validate department
		if (!Utils.departmentMatch(dept))
			return "Your department('" + dept + "') isn't recognized.";
		// validate role
		else if (!Utils.roleMatch(role))
			return "Your role('" + role + "') isn't recognized.";

		try {
			// validate user id (integer value)
			userId = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return "Your id('" + value + "') isn't recognized.";
		}
		returnValue = "success";
		user.setDept(Department.valueOf(dept.toUpperCase()));
		user.setRole(Role.fromString(role.toUpperCase()));
		user.setId(userId);
		return returnValue;
	}

}
