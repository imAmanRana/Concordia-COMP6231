/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 1 
 * Distributed Course Registration System (DCRS)
 */
package util;

import java.time.LocalDate;

/**
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public enum Semester {

	FALL, WINTER, SUMMER;

	public static Semester getCurrentSemester(LocalDate d) {
		Semester semester = null;
		if (d.getMonthValue() <= 4) {
			semester = WINTER;
		} else if (d.getMonthValue() >= 9) {
			semester = FALL;
		} else {
			semester = SUMMER;
		}
		return semester;
	}
	
	public static  boolean isValidSemester(String semester) {
		for (Semester s : Semester.values()) {
			if (s.toString().equals(semester))
				return true;
		}
		return false;
	}
}
