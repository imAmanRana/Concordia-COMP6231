/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 1 
 * Distributed Course Registration System (DCRS)
 */
package client;

import java.util.Scanner;

import util.Department;
import util.Role;

/**
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
			System.out.println("Login Successful : "+user);
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

	private static String validateUser(final String id, User user) {
		String returnValue = null, dept, role, value;
		int userId;
		// string length !=9
		if (id.length() != 9)
			return "Seems to be an invalid id(length not equal to 9).";

		dept = id.substring(0, 4);
		role = id.substring(4, 5);
		value = id.substring(5);

		// validate department
		if (!dept.matches("COMP|SOEN|INSE"))
			return "Your department('" + dept + "') isn't recognized.";
		else if (!role.matches("A|S"))
			return "Your role('" + role + "') isn't recognized.";

		try {
			userId = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return "Your id('" + value + "') isn't recognized.";
		}
		returnValue = "success";
		user.setDept(Department.valueOf(dept));
		user.setRole(Role.fromString(role));
		user.setId(userId);
		return returnValue;
	}

}
