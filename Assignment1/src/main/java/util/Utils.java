/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 1 
 * Distributed Course Registration System (DCRS)
 */
package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;

/**
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public class Utils {

	public static SimpleEntry<Boolean, String> validateUser(final String id,final Role userRole,final Department department) {
		String  dept, role, value;
		// string length !=9
		if (id.length() != 9)
			return new SimpleEntry<Boolean, String>(false, "Seems to be an invalid id(length not equal to 9).");

		dept = id.substring(0, 4);
		role = id.substring(4, 5);
		value = id.substring(5);

		// validate department
		if (!dept.matches("COMP|SOEN|INSE"))
			return new SimpleEntry<Boolean, String>(false, "The department('" + dept + "') isn't recognized.");
		else if(department!=null && department!=Department.valueOf(dept)) 
			return new SimpleEntry<Boolean, String>(false, "You are not authorized for this department('" + dept + "').");
		else if (!role.matches(userRole.toString()))
			return new SimpleEntry<Boolean, String>(false, "The role('" + role + "') isn't correct.");

		try {
			Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return new SimpleEntry<Boolean, String>(false, "The id('" + value + "') isn't correct.");
		}
		
		return new SimpleEntry<Boolean, String>(true, "valid");
	}
	
	
	public static SimpleEntry<Boolean,String> validateCourse(final String courseId){
		if (courseId.length() != 8)
			return new SimpleEntry<Boolean, String>(false, "Seems to be an invalid course(length not equal to 8).");
		String dept,value;
		
		dept = courseId.substring(0, 4);
		value = courseId.substring(4);
		
		if (!dept.matches("COMP|SOEN|INSE"))
			return new SimpleEntry<Boolean, String>(false, "Your department('" + dept + "') isn't recognized.");
		
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return new SimpleEntry<Boolean, String>(false, "Course id('" + value + "') isn't valid.");
		}
		
		return new SimpleEntry<Boolean, String>(true, "valid");
		
	}
	
	public static SimpleEntry<Boolean,String> validateCourse(final String courseId,Department department){
		
		if (courseId.length() != 8)
			return new SimpleEntry<Boolean, String>(false, "Seems to be an invalid course(length not equal to 8).");
		String dept,value;
		
		dept = courseId.substring(0, 4);
		value = courseId.substring(4);
		
		if (!dept.matches("COMP|SOEN|INSE"))
			return new SimpleEntry<Boolean, String>(false, "The department('" + dept + "') isn't recognized.");
		else if(department!=null && department!=Department.valueOf(dept)) 
			return new SimpleEntry<Boolean, String>(false, "You are not authorized for this department('" + dept + "').");
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return new SimpleEntry<Boolean, String>(false, "Course id('" + value + "') isn't valid.");
		}
		
		return new SimpleEntry<Boolean, String>(true, "valid");
	}
	
	public static SimpleEntry<Boolean, String> validateSemester(String semester) {
		boolean status = Semester.isValidSemester(semester);
		String msg=null;
		if(!status)
			msg = semester+" isn't valid semester.";
		return new SimpleEntry<Boolean, String>(status, msg);
	}
	
	
	public static byte[] objectToByteArray(Object obj) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(byteOut);
			out.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return byteOut.toByteArray();
	}
	
	
	public static Object byteArrayToObject(byte[] data){
		ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
		Object result=null;
	    ObjectInputStream in;
		try {
			in = new ObjectInputStream(byteIn);
			result = (Object) in.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return result;
	}
}
