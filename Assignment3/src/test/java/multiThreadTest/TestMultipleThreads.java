/*
* COMP6231 - Distributed Systems | Fall2018
* Assignment 3 
* Professor - Rajagopalan Jayakumar
* Distributed Course Registration System (DCRS) using Web Services
*/
package multiThreadTest;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import client.User;
import mapper.hashMap.MapResponse_String_Integer;
import remoteObject.EnrollmentInterface;
import util.Department;
import util.Role;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target=
 *      "_blank">Profile</a>
 */
public class TestMultipleThreads {

	static AwareUser comp_advisor = new AwareUser(Department.COMP, Role.ADVISOR, 1234);
	static AwareUser comp_student = new AwareUser(Department.COMP, Role.STUDENT, 4444);

	static AwareUser soen_advisor = new AwareUser(Department.SOEN, Role.ADVISOR, 1234);
	static AwareUser soen_student = new AwareUser(Department.SOEN, Role.STUDENT, 4444);

	static AwareUser inse_advisor = new AwareUser(Department.INSE, Role.ADVISOR, 1234);
	static AwareUser inse_student = new AwareUser(Department.INSE, Role.STUDENT, 4444);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		

		// ADD THE COURSES
		// TASK 1
		addCourses();
		
		
		//enroll in courses
		//TASK2
		enroll();
		
		
		//TASK 3
		//SWAP COURSES
		swap();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("\n\n\nCourse Availability");
		listCourses("FALL");
		listCourses("WINTER");
		listCourses("SUMMER");
		
		
		
		

	}
	
	
	static void addCourses() {
		Thread comp = new Thread(Task.addCourseTask(comp_advisor));
		Thread soen = new Thread(Task.addCourseTask(soen_advisor));
		Thread inse = new Thread(Task.addCourseTask(inse_advisor));

		comp.start();
		soen.start();
		inse.start();
		
	}
	
	static void enroll() {
		Thread comp_A = new Thread(Task.enrollInCourses(comp_advisor, comp_student, Arrays.asList("INSE6231","COMP6221"), "FALL"));
		Thread comp_S = new Thread(Task.enrollInCourses(comp_student, comp_student, Arrays.asList("COMP6231","COMP6221"), "FALL"));
		
		Thread soen_A = new Thread(Task.enrollInCourses(soen_advisor, soen_student, Arrays.asList("COMP6231","SOEN6221"), "FALL"));
		Thread soen_S = new Thread(Task.enrollInCourses(soen_student, soen_student, Arrays.asList("SOEN6231","SOEN6221"), "FALL"));
		
		Thread inse_A = new Thread(Task.enrollInCourses(inse_advisor, inse_student, Arrays.asList("INSE6231","SOEN6231"), "FALL"));
		Thread inse_S = new Thread(Task.enrollInCourses(inse_student, inse_student, Arrays.asList("INSE6231","INSE6221"), "FALL"));
		
		comp_A.start();
		comp_S.start();
		soen_S.start();
		soen_A.start();
		inse_S.start();
		inse_A.start();
		
		
	}
	
	static void swap() {
		Thread comp_S = new Thread(Task.swapCourse(comp_student, comp_student, "SOEN6221", "COMP6221"));
		Thread soen_S = new Thread(Task.swapCourse(soen_student, soen_student, "SOEN6221", "SOEN6231"));
		Thread inse_S = new Thread(Task.swapCourse(inse_student, inse_student, "SOEN6221", "INSE6221"));
		
		comp_S.start();
		soen_S.start();
		inse_S.start();
		
	}
	
	
	static void listCourses(String semester) {
		try {
			MapResponse_String_Integer response1 = comp_advisor.getStub().listCourseAvailability(comp_advisor.toString(),semester);
			Map<String, Integer>  courseMap = response1.getCourseList();
			StringBuilder sb = new StringBuilder();
			sb.append(semester).append(" - ");
			courseMap.forEach((k, v) -> sb.append(k).append(" ").append(v).append(", "));
			if (courseMap.size() > 0)
				sb.replace(sb.length() - 2, sb.length() - 1, ".");

			System.out.println(sb);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}

class Task {

	public static Runnable addCourseTask(AwareUser user) {
		return () -> {
			try {
				System.out.println(user.toString()+" add "+user.getDept() + "6231, FALL : "+user.getStub().addCourse(user.toString(), user.getDept() + "6231", "FALL", 2));
				System.out.println(user.toString()+" add "+user.getDept() + "6231, WINTER : "+user.getStub().addCourse(user.toString(), user.getDept() + "6231", "WINTER", 2));
				System.out.println(user.toString()+" add "+user.getDept() + "6231, SUMMER : "+user.getStub().addCourse(user.toString(), user.getDept() + "6231", "SUMMER", 2));

				System.out.println(user.toString()+" add "+user.getDept() + "6221, FALL : "+user.getStub().addCourse(user.toString(), user.getDept() + "6221", "FALL", 1));
				System.out.println(user.toString()+" add "+user.getDept() + "6221, WINTER : "+user.getStub().addCourse(user.toString(), user.getDept() + "6221", "WINTER", 1));
				System.out.println(user.toString()+" add "+user.getDept() + "6221, SUMMER : "+user.getStub().addCourse(user.toString(), user.getDept() + "6221", "SUMMER", 1));

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		};
	}

	public static Runnable enrollInCourses(AwareUser user, AwareUser studentUser, List<String> courseList, String semester) {
		return () -> {
			try {
				for (String courseId : courseList) {

					System.out.println(user.toString()+" enroll "+courseList+" "+semester+" : "+user.getStub().enrolCourse(studentUser.toString(), courseId, semester));
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		};
	}
	
	
	public static Runnable swapCourse(AwareUser user, AwareUser studentUser, String newCourseId,String oldCourseId) {
		return () ->{
			try {
				System.out.println(user.toString()+" swap "+oldCourseId+" with "+newCourseId+" : "+user.getStub().swapCourse(studentUser.toString(), newCourseId, oldCourseId));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		};
	}
}

class AwareUser extends User {

	EnrollmentInterface stub;

	AwareUser(Department department, Role role, int id) {
		super(department, role, id);

		URL url;
		try {
			url = new URL("http://localhost:" + getDept().getWebServicePort() + "/" + getDept().toString().toLowerCase()
					+ "?wsdl");
			QName qName = new QName("http://remoteObject/", "EnrollmentImplService");
			Service service = Service.create(url, qName);
			stub = service.getPort(EnrollmentInterface.class);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	EnrollmentInterface getStub() {
		return stub;
	}

}