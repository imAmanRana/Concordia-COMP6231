/*
* COMP6231 - Distributed Systems | Fall2018
* Assignment 3 
* Professor - Rajagopalan Jayakumar
* Distributed Course Registration System (DCRS) using Web Services
*/
package client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import logging.MyLogger;
import mapper.hashMap.MapResponse_String_StringArray;
import mapper.simpleEntry.EntryResponse_Boolean_String;
import remoteObject.EnrollmentInterface;
import util.Constants;
import util.Semester;
import util.Utils;

/**
 * * The <code>StudentClient</code> class contains the code to handle and
 * perform the operations related to a Student. This class implements
 * <code>Runnable</code> so that each student login can be handled on a separate
 * thread.
 * 
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana" target="_blank">Profile</a>
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
	/**
	 * Runs each student login on a separate thread.
	 */
	@Override
	public void run() {

		try {
			setupLogging();
			LOGGER.info("STUDENT LOGIN(" + user + ")");

			URL url = new URL("http://localhost:"+user.getDept().getWebServicePort()+"/" + user.getDept().toString().toLowerCase() + "?wsdl");
			QName qName = new QName("http://remoteObject/", "EnrollmentImplService");
			Service service = Service.create(url, qName);

			stub = service.getPort(EnrollmentInterface.class);

			performOperations();
		} catch (RemoteException e) {
			LOGGER.severe("RemoteException Exception : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IO Exception : " + e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Performs Student related operations
	 * @throws RemoteException
	 */
	private void performOperations() throws RemoteException {

		int userSelection = displayMenu();
		String courseId, sem;
		Semester semester;
		EntryResponse_Boolean_String response1;
		MapResponse_String_StringArray response2;
		SimpleEntry<Boolean, String> result;
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
				response1 = stub.enrolCourse(user.toString(), courseId, semester.toString());
				result = response1.getResponse();

				LOGGER.info(String.format(Constants.LOG_MSG, "enrolCourse", Arrays.asList(user, courseId, semester),
						result.getKey(), result.getValue()));
				if (result.getKey())
					System.out.println("SUCCESS - " + result.getValue());
				else
					System.out.println("FAILURE - " + result.getValue());

				break;

			case 2:
				Map<String, ArrayList<String>> courseList;
				response2 = stub.getClassSchedule(user.toString());
				courseList = Utils.convertToHashMap_String_ArrayList(response2.getClassSchedule());

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
				response1 = stub.dropCourse(user.toString(), courseId);
				result = response1.getResponse();

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

				response1 = stub.swapCourse(user.toString(), newCourseId, oldCourseId);
				result = response1.getResponse();

				LOGGER.info(String.format(Constants.LOG_MSG, "swapCourse",
						Arrays.asList(user.toString(), newCourseId, oldCourseId), result.getKey(), result.getValue()));
				if (result.getKey())
					System.out.println("SUCCESS - Course successfully swapped for " + user.toString() + ".");
				else
					System.out.println("FAILURE - " + result.getValue());
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
