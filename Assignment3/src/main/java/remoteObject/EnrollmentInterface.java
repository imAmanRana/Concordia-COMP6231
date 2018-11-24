/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 3 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Web Services
 */
package remoteObject;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import mapper.hashMap.MapResponse_String_StringArray;
import mapper.hashMap.MapResponse_String_Integer;
import mapper.simpleEntry.EntryResponse_Boolean_String;

/**
 * Interface contract for the Distributed Course Registration System
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana" target="_blank">Profile</a>
 *
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface EnrollmentInterface extends Remote {

	/* Advisor Operations */

	boolean addCourse(String advisorId, String courseId, String semester, int capacity) throws RemoteException;

	boolean removeCourse(String advisorId, String courseId, String semester) throws RemoteException;

	MapResponse_String_Integer listCourseAvailability(String advisorId, String semester) throws RemoteException;

	/* Student Operations */

	EntryResponse_Boolean_String enrolCourse(String studentId, String courseId, String semester) throws RemoteException;

	MapResponse_String_StringArray getClassSchedule(String studentId) throws RemoteException;

	EntryResponse_Boolean_String dropCourse(String studentId, String courseId) throws RemoteException;
	
	EntryResponse_Boolean_String swapCourse(String studentId,String newCourseId,String oldCourseId) throws RemoteException;

}
