 /*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 2 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Java IDL (CORBA)
 */
package client;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
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
 * * The <code>StudentClient</code> class contains the code to handle and
 * perform the operations related to a Student. This class implements
 * <code>Runnable</code> so that each student login can be handled on a separate
 * thread.
 * 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public class StudentClient implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	User user;
	Scanner input;
	String[] args;
	EnrollmentInterface stub;

	/**
	 * StudentClient constructor to initialize its object
	 * 
	 * @param user
	 */
	public StudentClient(String[] args, User user) {
		this.user = user;
		this.args = args;
		input = new Scanner(System.in);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		// get the registry
		Registry registry;

		try {
			setupLogging();
			LOGGER.info("STUDENT LOGIN(" + user + ")");
			ORB orb = ORB.init(args, null);
			Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			stub = (EnrollmentInterface) EnrollmentInterfaceHelper.narrow(ncRef.resolve_str(user.getDept().toString()));

			performOperations();
		} catch (RemoteException e) {
			LOGGER.severe("RemoteException Exception : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IO Exception : " + e.getMessage());
			e.printStackTrace();
		} catch (InvalidName e) {
			LOGGER.severe("InvalidName : " + e.getMessage());
			e.printStackTrace();
		} catch (NotFound e) {
			LOGGER.severe("NotFound : " + e.getMessage());
			e.printStackTrace();
		} catch (CannotProceed e) {
			LOGGER.severe("CannotProceed : " + e.getMessage());
			e.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			LOGGER.severe("org.omg.CosNaming.NamingContextPackage.InvalidName : " + e.getMessage());
			e.printStackTrace();
		}

	}

	private void performOperations() throws RemoteException {

		int userSelection = displayMenu();
		String courseId, sem;
		Semester semester;
		SimpleEntry<Boolean, String> result;
		Any any;
		while (userSelection != 5) {

			switch (userSelection) {
			case 1:
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
				} else {
					semester = Semester.valueOf(sem.toUpperCase());
				}
				any = stub.enrolCourse(user.toString(), courseId, semester.toString());
				result = (SimpleEntry<Boolean, String>) any.extract_Value();

				LOGGER.info(String.format(Constants.LOG_MSG, "enrolCourse", Arrays.asList(user, courseId, semester),
						result.getKey(), result.getValue()));
				if (result.getKey())
					System.out.println("SUCCESS - " + result.getValue());
				else
					System.out.println("FAILURE - " + result.getValue());

				break;

			case 2:
				HashMap<String, ArrayList<String>> courseList;
				any = stub.getClassSchedule(user.toString());
				courseList = (HashMap<String, ArrayList<String>>) any.extract_Value();

				LOGGER.info(String.format(Constants.LOG_MSG, "getClassSchedule", Arrays.asList(user),
						courseList != null, courseList));
				if (courseList != null)
					System.out.println(courseList);
				else
					System.out.println("There was some problem in getting the class schedule. Please try again later.");

				break;
			case 3:
				System.out.print("Enter the Course ID to drop(eg. COMP2342,SOEN2345,...) : ");
				courseId = input.next().toUpperCase();
				result = Utils.validateCourse(courseId.trim());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				any = stub.dropCourse(user.toString(), courseId);
				result = (SimpleEntry<Boolean, String>) any.extract_Value();

				LOGGER.info(String.format(Constants.LOG_MSG, "dropCourse", Arrays.asList(user, courseId),
						result.getKey(), result.getValue()));
				if (result.getKey())
					System.out.println("SUCCESS -" + result.getValue());
				else
					System.out.println("FAILURE - " + result.getValue());

				break;

			case 4:
				
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
				
				
				any = stub.swapCourse(user.toString(), newCourseId, oldCourseId);
				result = (SimpleEntry<Boolean, String>) any.extract_Value();
				
				LOGGER.info(String.format(Constants.LOG_MSG, "swapCourse",Arrays.asList(user.toString(),newCourseId,oldCourseId),result.getKey(),result.getValue()));
				if(result.getKey())
					System.out.println("SUCCESS - Course successfully swapped for "+user.toString()+".");
				else
					System.out.println("FAILURE - "+result.getValue());
			case 5:
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
	 * Display menu to the Student
	 * 
	 * @return
	 */
	private int displayMenu() {
		System.out.println("--------------------------------");
		System.out.println("|	Available Operations 	|");
		System.out.println("--------------------------------");
		System.out.println("|1| Enroll in Course.");
		System.out.println("|2| Get Class Schedule.");
		System.out.println("|3| Drop a Course.");
		System.out.println("|4| Swap a Course.");
		System.out.println("|5| Quit.");
		System.out.print("Input your operation number : ");
		return input.nextInt();
	}

	/**
	 * Configures the logger
	 * 
	 * @throws IOException
	 */
	private void setupLogging() throws IOException {
		File files = new File(Constants.STUDENT_LOG_DIRECTORY);
		if (!files.exists())
			files.mkdirs();
		files = new File(Constants.STUDENT_LOG_DIRECTORY + user + ".log");
		if (!files.exists())
			files.createNewFile();
		MyLogger.setup(files.getAbsolutePath());
	}

}
