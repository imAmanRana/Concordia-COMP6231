/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 2 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Java IDL (CORBA)
 */
package client;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import logging.MyLogger;
import remoteObject.EnrollmentInterface;
import remoteObject.EnrollmentInterfaceHelper;
import util.Constants;
import util.Role;
import util.Semester;
import util.Utils;

/**
 * The <code>AdvisorClient</code> class contains the code to handle and perform
 * the operations related to the Advisor.
 * This class implements <code>Runnable</code> so that each advisor login can be
 * handled on a separate thread. 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 */
public class AdvisorClient implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	User user;
	Scanner input;
	EnrollmentInterface stub;
	String[] args;
	
	/**
	 * Constructor to initialize an Advisor object
	 * @param user <code>User</code> class object
	 */
	public AdvisorClient(String[] args,User user) {
		this.user = user;
		this.args = args;
		input = new Scanner(System.in);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		try {
			setupLogging();
			LOGGER.info("ADVISOR LOGIN("+user+")");
			ORB orb = ORB.init(args, null);
			Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			stub = (EnrollmentInterface) EnrollmentInterfaceHelper.narrow(ncRef.resolve_str(user.getDept().toString()));

			performOperations();
		} catch (RemoteException e) {
			LOGGER.severe("RemoteException Exception : "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IO Exception : "+e.getMessage());
			e.printStackTrace();
		} catch (InvalidName e) {
			LOGGER.severe("InvalidName : "+e.getMessage());
			e.printStackTrace();
		} catch (NotFound e) {
			LOGGER.severe("NotFound : "+e.getMessage());
			e.printStackTrace();
		} catch (CannotProceed e) {
			LOGGER.severe("CannotProceed : "+e.getMessage());
			e.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			LOGGER.severe("org.omg.CosNaming.NamingContextPackage.InvalidName : "+e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * This method performs the advisor related operations.
	 * @throws RemoteException
	 */
	@SuppressWarnings("unchecked")
	private void performOperations() throws RemoteException {

		int userSelection = displayMenu();
		String studentId, courseId,sem;
		Semester semester;
		int courseCapacity = 0;
		SimpleEntry<Boolean, String> result;
		HashMap<String, Integer> courseMap;
		HashMap<String, ArrayList<String>> courseList;
		boolean status;
		Any any;
		
		/* Executes the loop until the advisor quits the application i.e. presses 7
		 * 
		 */
		while (userSelection != 8) {
			switch (userSelection) {
			case 1:
				System.out.print("Enter the course ID : ");
				courseId = input.next().toUpperCase();
				result = Utils.validateCourse(courseId.trim(), this.user.getDept());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}

				System.out.print("Course Capacity : ");
				courseCapacity = input.nextInt();
				if(courseCapacity<1) {
					System.out.println("Course Capacity needs to be atleast 1.");
					break;
				}

				System.out.print("Enter the semester for the course(FALL|WINTER|SUMMER) : ");
				sem = input.next();
				result = Utils.validateSemester(sem.trim());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}else {
					semester = Semester.valueOf(sem.toUpperCase());
				}

				status = stub.addCourse(user.toString(), courseId, semester.toString(), courseCapacity);
				LOGGER.info(String.format(Constants.LOG_MSG, "addCourse",Arrays.asList(user,courseId,semester,courseCapacity),status,Constants.EMPTYSTRING));
				if(status)
					System.out.println("SUCCESS - Course Added Successfully");
				else
					System.out.println("FAILURE = "+courseId+" is already offered in "+semester+" semester.");
				break;
				
			case 2:
				System.out.print("Enter the course ID : ");
				courseId = input.next().toUpperCase();
				result = Utils.validateCourse(courseId.trim(), this.user.getDept());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}

				System.out.print("Enter the semester for the course(FALL|WINTER|SUMMER) : ");
				sem = input.next();
				result = Utils.validateSemester(sem.trim());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}else {
					semester = Semester.valueOf(sem.toUpperCase());
				}

				status = stub.removeCourse(user.toString(), courseId, semester.toString());
				LOGGER.info(String.format(Constants.LOG_MSG, "removeCourse",Arrays.asList(user,courseId,semester),status,Constants.EMPTYSTRING));
				if(status)
					System.out.println("SUCCESS - "+courseId+" removed successfully for "+semester+" semester.");
				else
					System.out.println("FAILURE - "+courseId+" is not offered in  "+semester+" semester.");
				break;
				
			case 3:
				System.out.print("Enter the semester for course schedule(FALL|WINTER|SUMMER) : ");
				sem = input.next();
				result = Utils.validateSemester(sem.trim());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}else {
					semester = Semester.valueOf(sem.toUpperCase());
				}

				any = stub.listCourseAvailability(user.toString(), semester.toString());
				courseMap = (HashMap<String, Integer>) any.extract_Value();
				StringBuilder sb = new StringBuilder();
				sb.append(semester).append(" - ");
				courseMap.forEach((k,v)-> sb.append(k).append(" ").append(v).append(", "));
				if(courseMap.size()>0)
					sb.replace(sb.length()-2, sb.length()-1, ".");
				
				LOGGER.info(String.format(Constants.LOG_MSG, "listCourseAvailability",Arrays.asList(user,semester),courseMap!=null,courseMap));
				if(courseMap!=null)
					System.out.println(sb);
				else
					System.out.println("There was some problem in getting the course schedule. Please try again later.");
				
				break;
			case 4:
				System.out.print("Enter the Student ID(eg. COMPS1111) : ");
				studentId = input.next().toUpperCase();
				result = Utils.validateUser(studentId.trim(), Role.STUDENT,this.user.getDept());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				System.out.print("Enter the Course ID (eg. COMP2342,SOEN2345,...) : ");
				courseId = input.next().toUpperCase();
				result = Utils.validateCourse(courseId.trim());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				System.out.print("Enter Semester(FALL|WINTER|SUMMER) : ");
				sem = input.next();
				result = Utils.validateSemester(sem.trim());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}else {
					semester = Semester.valueOf(sem.toUpperCase());
				}
				
				any = stub.enrolCourse(studentId, courseId, semester.toString());
				result = (SimpleEntry<Boolean, String>) any.extract_Value();
				
				LOGGER.info(String.format(Constants.LOG_MSG, "enrolCourse",Arrays.asList(studentId,courseId,semester),result.getKey(),result.getValue()));
				if(result.getKey())
					System.out.println("SUCCESS - "+studentId+" successfully enrolled in "+courseId+".");
				else
					System.out.println("FAILURE - "+result.getValue());
				
				break;

			case 5:
				System.out.print("Enter the Student ID(eg. COMPS1111) : ");
				studentId = input.next().toUpperCase();
				result = Utils.validateUser(studentId.trim(), Role.STUDENT,this.user.getDept());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}

				any = stub.getClassSchedule(studentId);
				courseList = (HashMap<String, ArrayList<String>>) any.extract_Value();
				
				LOGGER.info(String.format(Constants.LOG_MSG, "getClassSchedule",Arrays.asList(studentId),courseList!=null,courseList));
				if(courseList!=null)
					System.out.println(courseList);
				else
					System.out.println("There was some problem in getting the class schedule. Please try again later.");
				
				break;
			case 6:
				System.out.print("Enter the Student ID(eg. COMPS1111) : ");
				studentId = input.next().toUpperCase();
				result = Utils.validateUser(studentId.trim(), Role.STUDENT,this.user.getDept());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				System.out.print("Enter the Course ID to drop(eg. COMP2342,SOEN2345,...) : ");
				courseId = input.next().toUpperCase();
				result = Utils.validateCourse(courseId.trim(), null);
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				any = stub.dropCourse(studentId, courseId);
				result = (SimpleEntry<Boolean, String>) any.extract_Value();
				LOGGER.info(String.format(Constants.LOG_MSG, "dropCourse",Arrays.asList(studentId,courseId),result.getKey(),result.getValue()));
				if(result.getKey())
					System.out.println("SUCCESS - Course successfully dropped for "+studentId+".");
				else
					System.out.println("FAILURE - "+result.getValue());
				
				break;
			case 7:
				//swap course
				
				System.out.print("Enter the Student ID(eg. COMPS1111) : ");
				studentId = input.next().toUpperCase();
				result = Utils.validateUser(studentId.trim(), Role.STUDENT,this.user.getDept());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				System.out.print("Enter the Course ID to drop (eg. COMP2342,SOEN2345,...) : ");
				String oldCourseId = input.next().toUpperCase();
				result = Utils.validateCourse(oldCourseId.trim());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				System.out.print("Enter the Course ID to enroll (eg. COMP2342,SOEN2345,...) : ");
				String newCourseId = input.next().toUpperCase();
				result = Utils.validateCourse(newCourseId.trim());
				
				
				any = stub.swapCourse(studentId, newCourseId, oldCourseId);
				result = (SimpleEntry<Boolean, String>) any.extract_Value();
				
				LOGGER.info(String.format(Constants.LOG_MSG, "swapCourse",Arrays.asList(studentId,newCourseId,oldCourseId),result.getKey(),result.getValue()));
				if(result.getKey())
					System.out.println("SUCCESS - Course successfully swapped for "+studentId+".");
				else
					System.out.println("FAILURE - "+result.getValue());
				
				break;
			case 8:
				break;
			default:
				System.out.println("Please select a valid operation.");
				break;

			}

			System.out.println("\n\n");
			userSelection = displayMenu();
		}
		System.out.println("HAVE A NICE DAY!");
	}

	
	/**
	 * Display menu to the logged in Advisor
	 * @return
	 */
	private int displayMenu() {
		System.out.println("--------------------------------");
		System.out.println("|	Available Operations 	|");
		System.out.println("--------------------------------");
		System.out.println("|1| Add a course.");
		System.out.println("|2| Remove a course.");
		System.out.println("|3| List Courses Availability.");
		System.out.println("|4| Enroll in Course.");
		System.out.println("|5| Get Class Schedule.");
		System.out.println("|6| Drop a Course.");
		System.out.println("|7| Swap a Course.");
		System.out.println("|8| Quit.");
		System.out.print("Input your operation number : ");
		
		return input.nextInt();
	}
	
	/**
	 * Configures the logger
	 * @throws IOException
	 */
	private void setupLogging() throws IOException {
		File files = new File(Constants.ADVISOR_LOG_DIRECTORY);
        if (!files.exists()) 
            files.mkdirs(); 
        files = new File(Constants.ADVISOR_LOG_DIRECTORY+user+".log");
        if(!files.exists())
        	files.createNewFile();
        MyLogger.setup(files.getAbsolutePath());
	}

}
