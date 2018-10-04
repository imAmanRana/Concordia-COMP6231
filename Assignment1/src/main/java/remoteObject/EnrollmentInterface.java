/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 1 
 * Distributed Course Registration System (DCRS)
 */
package remoteObject;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public interface EnrollmentInterface extends Remote {

	/* Advisor Operations */

	boolean addCourse(String advisorId, String courseId, String semester, int capacity) throws RemoteException;

	boolean removeCourse(String advisorId, String courseId, String semester) throws RemoteException;

	HashMap<String, Integer> listCourseAvailability(String advisorId, String semester) throws RemoteException;

	/* Student Operations */

	SimpleEntry<Boolean, String> enrolCourse(String studentId, String courseId, String semester) throws RemoteException;

	HashMap<String, ArrayList<String>> getClassSchedule(String studentId) throws RemoteException;

	SimpleEntry<Boolean, String> dropCourse(String studentId, String courseId) throws RemoteException;

}
