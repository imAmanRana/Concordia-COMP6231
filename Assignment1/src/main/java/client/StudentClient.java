/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 1 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS)
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

import logging.MyLogger;
import remoteObject.EnrollmentInterface;
import util.Constants;
import util.Semester;
import util.Utils;

/**
 * * The <code>StudentClient</code> class contains the code to handle and perform
 * the operations related to a Student.
 * This class implements <code>Runnable</code> so that each student login can be
 * handled on a separate thread. 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public class StudentClient implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	User user;
	Scanner input;
	EnrollmentInterface stub;

	/**
	 * StudentClient constructor to initialize its object
	 * @param user
	 */
	public StudentClient(User user) {
		this.user = user;
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
			registry = LocateRegistry.getRegistry(null);
			stub = (EnrollmentInterface) registry.lookup(user.getDept().toString());
			performOperations();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void performOperations() throws RemoteException {

		int userSelection = displayMenu();
		String courseId,sem;
		Semester semester;
		SimpleEntry<Boolean, String> result;
		while (userSelection != 4) {

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
				}else {
					semester = Semester.valueOf(sem.toUpperCase());
				}
				result = stub.enrolCourse(user.toString(), courseId, semester.toString());

				LOGGER.info(String.format(Constants.LOG_MSG, "enrolCourse", Arrays.asList(user, courseId, semester),
						result.getKey(), result.getValue()));
				if (result.getKey())
					System.out.println("SUCCESS - " + result.getValue());
				else
					System.out.println("FAILURE - " + result.getValue());

				break;

			case 2:
				HashMap<String, ArrayList<String>> courseList = stub.getClassSchedule(user.toString());

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
				result = stub.dropCourse(user.toString(), courseId);

				LOGGER.info(String.format(Constants.LOG_MSG, "dropCourse", Arrays.asList(user, courseId),
						result.getKey(), result.getValue()));
				if (result.getKey())
					System.out.println("SUCCESS -" + result.getValue());
				else
					System.out.println("FAILURE - " + result.getValue());

				break;

			case 4:
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
	 * @return
	 */
	private int displayMenu() {
		System.out.println("--------------------------------");
		System.out.println("|	Available Operations 	|");
		System.out.println("--------------------------------");
		System.out.println("|1| Enroll in Course.");
		System.out.println("|2| Get Class Schedule.");
		System.out.println("|3| Drop a Course.");
		System.out.println("|4| Quit.");
		System.out.print("Input your operation number : ");
		return input.nextInt();
	}

	/**
	 * Configures the logger
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
