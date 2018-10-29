/*
* COMP6231 - Distributed Systems | Fall2018
* Assignment 2 
* Professor - Rajagopalan Jayakumar
* Distributed Course Registration System (DCRS) using Java IDL (CORBA)
*/
package remoteObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import util.Constants;
import util.Department;
import util.Utils;

/**
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana" target='blank'>Profile</a>
 */
public class EnrollmentImpl extends EnrollmentInterfacePOA {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private Department department;

	private ORB orb;

	private ReentrantLock rl;

	// in-memory database
	private HashMap<String, HashMap<String, HashMap<String, Object>>> deptDatabase;

	/**
	 * Constructor
	 * 
	 * @param dept
	 */
	public EnrollmentImpl(String dept, ORB orb) {
		this.department = Department.valueOf(dept);
		deptDatabase = new HashMap<>();
		this.orb = orb;
		this.rl = new ReentrantLock(true); // fair reentrant lock
	}

	/**
	 * Adds course to the department's course list
	 */
	public boolean addCourse(String advisorId, String courseId, String semester, int capacity) {
		boolean status = false;
		String msg = Constants.EMPTYSTRING;

		rl.lock(); // get the lock
		if (deptDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = deptDatabase.get(semester);

			if (courses.containsKey(courseId)) {
				status = false;
				msg = "Course already exists for " + semester + " semester.";
			} else {
				HashMap<String, Object> courseDetails = new HashMap<>();
				courseDetails.put(Constants.CAPACITY, capacity);
				courseDetails.put(Constants.STUDENTS_ENROLLED, 0);
				courseDetails.put(Constants.STUDENT_IDS, new HashSet<String>());
				courses.put(courseId, courseDetails);
				status = true;
				msg = courseId + " Added.";
			}
		} else {
			// semester doesn't exists
			HashMap<String, Object> courseDetails = new HashMap<>();
			courseDetails.put(Constants.CAPACITY, capacity);
			courseDetails.put(Constants.STUDENTS_ENROLLED, 0);
			courseDetails.put(Constants.STUDENT_IDS, new HashSet<String>());
			HashMap<String, HashMap<String, Object>> courses = new HashMap<>();
			courses.put(courseId, courseDetails);

			// synchronizing the write operation to the in-memory database
			this.deptDatabase.put(semester, courses);

			status = true;
			msg = courseId + " Added.";
		}

		rl.unlock(); // release the lock

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_ADD_COURSE,
				Arrays.asList(advisorId, courseId, semester, capacity), status, msg));

		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see remoteObject.EnrollmentInterface#removeCourse(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	/**
	 * Removes a course from the department's course list
	 */
	@Override
	public boolean removeCourse(String advisorId, String courseId, String semester) {

		boolean status = false;
		String msg = Constants.EMPTYSTRING;
		if (deptDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = deptDatabase.get(semester);

			rl.lock(); // accquire the lock
			if (courses.containsKey(courseId)) {

				courses.remove(courseId);
				status = true;
				msg = courseId + " removed";
			} else {
				status = false;
				msg = semester + " semester doesn't offer this course yet.";
			}

			rl.unlock(); // release the lock
		} else {
			status = false;
			msg = semester + " semester doesn't have any courses yet.";
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_REMOVE_COURSE,
				Arrays.asList(advisorId, courseId, semester), status, msg));

		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * remoteObject.EnrollmentInterface#listCourseAvailability(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	/**
	 * Lists the courses available along with the no. of vacant seats for a
	 * particular semester
	 */
	public Any listCourseAvailability(String advisorId, String semester) {

		HashMap<String, Integer> result = new HashMap<>();
		result.putAll(listCourseAvailabilityForThisServer(semester));

		// inquire different departments
		for (Department dept : Department.values()) {
			if (dept != this.department) {
				result.putAll((HashMap<String, Integer>) Utils
						.byteArrayToObject(udpCommunication(dept, semester, Constants.OP_LIST_COURSE_AVAILABILITY)));
			}
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_LIST_COURSE_AVAILABILITY,
				Arrays.asList(advisorId, semester), result != null, result));

		return returnAny(result);
	}

	/**
	 * Lists the courses available along with the no. of vacant seats for a
	 * particular semester on this server(COMP|SOEN|INSE)
	 * 
	 * @param semester
	 * @return
	 */
	private HashMap<String, Integer> listCourseAvailabilityForThisServer(String semester) {
		HashMap<String, Integer> result = new HashMap<>();
		// get courses from the current department
		if (deptDatabase.containsKey(semester)) {
			deptDatabase.get(semester).forEach(
					(course, courseDetails) -> result.put(course, (Integer) courseDetails.get(Constants.CAPACITY)
							- (Integer) courseDetails.get(Constants.STUDENTS_ENROLLED)));
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see remoteObject.EnrollmentInterfaceOperations#enrolCourse(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	/**
	 * Enrols a student in a particular course
	 */
	public Any enrolCourse(String studentId, String courseId, String semester) {

		boolean status = true;
		String msg = null;
		SimpleEntry<Boolean, String> result = null;

		// get student schedule
		Any any = getClassSchedule(studentId);
		HashMap<String, ArrayList<String>> studentSchedule = (HashMap<String, ArrayList<String>>) any.extract_Value();

		// student already enrolled in 3 courses
		if (studentSchedule.containsKey(semester)
				&& studentSchedule.get(semester).size() >= Constants.MAX_COURSE_TAKEN_BY_STUDENT) {
			status = false;
			msg = studentId + " is already enrolled in " + Constants.MAX_COURSE_TAKEN_BY_STUDENT + " courses "
					+ studentSchedule.get(semester) + " for this " + semester + " semester.";
			return returnAny((new SimpleEntry<Boolean, String>(status, msg)));
		}

		List<String> departmentCourses = new ArrayList<>();
		List<String> outOfDepartmentCourses = new ArrayList<>();
		studentSchedule.forEach((sem, courses) -> {
			courses.forEach((course) -> {
				Department dept = Department.valueOf(course.substring(0, 4).toUpperCase());
				if (dept == this.department)
					departmentCourses.add(course);
				else
					outOfDepartmentCourses.add(course);
			});
		});
		Department courseDept = Department.valueOf(courseId.substring(0, 4).toUpperCase());
		// enroll in this department only
		if (department == courseDept) {

			// student already taking this course
			if (departmentCourses.contains(courseId)) {
				status = false;
				msg = studentId + " is already enrolled in " + courseId + ".";
			}
			if (status) {
				result = enrollmentForThisDepartment(studentId, courseId, semester);
			}

		} else {

			// check if student is already enrolled in 2 elective courses
			if (outOfDepartmentCourses.size() >= Constants.MAX_ELECTIVE_COURSES) {
				status = false;
				msg = studentId + " is already enrolled in " + Constants.MAX_ELECTIVE_COURSES
						+ " out-of-department courses.";
			} else {
				// enquire respective department
				for (Department dept : Department.values()) {
					if (dept == courseDept) {
						HashMap<String, String> data = new HashMap<>();
						data.put(Constants.STUDENT_ID, studentId);
						data.put(Constants.COURSE_ID, courseId);
						data.put(Constants.SEMESTER, semester);

						result = (SimpleEntry<Boolean, String>) Utils
								.byteArrayToObject(udpCommunication(courseDept, data, Constants.OP_ENROL_COURSE));
					}
				}
			}

			// status = false;
			// msg = "Department not found.";
		}

		if (result == null)
			result = new SimpleEntry<Boolean, String>(status, msg);

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_ENROL_COURSE,
				Arrays.asList(studentId, courseId, semester), result.getKey(), result.getValue()));

		return returnAny(result);
	}

	private SimpleEntry<Boolean, String> enrollmentForThisDepartment(String studentId, String courseId,
			String semester) {
		boolean status;
		String msg;
		if (deptDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = deptDatabase.get(semester);

			if (courses.containsKey(courseId)) {
				HashMap<String, Object> courseDetails = courses.get(courseId);

				rl.lock(); // acquire the lock
				if (((Integer) courseDetails.get(Constants.CAPACITY)
						- (Integer) courseDetails.get(Constants.STUDENTS_ENROLLED)) > 0) {

					status = ((HashSet<String>) courseDetails.get(Constants.STUDENT_IDS)).add(studentId);
					if (status) {
						courseDetails.put(Constants.STUDENTS_ENROLLED,
								(Integer) courseDetails.get(Constants.STUDENTS_ENROLLED) + 1);
						status = true;
						msg = "Enrollment Successful.";
					} else {
						status = false;
						msg = studentId + " is already enrolled in " + courseId + ".";
					}

				} else {
					status = false;
					msg = courseId + " is full.";
				}
				rl.unlock(); // release the lock
			} else {
				status = false;
				msg = courseId + " is not offered in " + semester + " semester.";
			}
		} else {
			status = false;
			msg = "No Courses avialable for " + semester + " semester.";
		}

		return new SimpleEntry<Boolean, String>(status, msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * remoteObject.EnrollmentInterfaceOperations#getClassSchedule(java.lang.String)
	 */
	@Override
	public Any getClassSchedule(String studentId) {
		HashMap<String, ArrayList<String>> schedule = new HashMap<>();
		schedule.putAll(getClassScheduleThisServer(studentId));

		// inquire different departments
		for (Department dept : Department.values()) {
			if (dept != this.department) {

				HashMap<String, ArrayList<String>> deptSchedule = (HashMap<String, ArrayList<String>>) Utils
						.byteArrayToObject(udpCommunication(dept, studentId, Constants.OP_GET_CLASS_SCHEDULE));

				for (String semester : deptSchedule.keySet()) {
					if (schedule.containsKey(semester)) {
						schedule.get(semester).addAll(deptSchedule.get(semester));
					} else {
						schedule.put(semester, deptSchedule.get(semester));
					}
				}
			}
		}
		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_GET_CLASS_SCHEDULE, Arrays.asList(studentId),
				schedule != null, schedule));
		return returnAny(schedule);
	}

	private HashMap<String, ArrayList<String>> getClassScheduleThisServer(String studentId) {
		HashMap<String, ArrayList<String>> schedule = new HashMap<>();
		deptDatabase.forEach((semester, courses) -> {
			courses.forEach((course, details) -> {
				if (((HashSet<String>) details.get(Constants.STUDENT_IDS)).contains(studentId)) {
					if (schedule.containsKey(semester)) {
						schedule.get(semester).add(course);
					} else {
						ArrayList<String> temp = new ArrayList<>();
						temp.add(course);
						schedule.put(semester, temp);
					}
				}
			});
		});
		return schedule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see remoteObject.EnrollmentInterfaceOperations#dropCourse(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Any dropCourse(String studentId, String courseId) {

		Department courseDept = Department.valueOf(courseId.substring(0, 4).toUpperCase());
		SimpleEntry<Boolean, String> result;
		if (this.department == courseDept) {
			result = dropCourseOnThisServer(studentId, courseId);
		} else {
			HashMap<String, String> data = new HashMap<>();
			data.put(Constants.STUDENT_ID, studentId);
			data.put(Constants.COURSE_ID, courseId);
			result = (SimpleEntry<Boolean, String>) Utils
					.byteArrayToObject(udpCommunication(courseDept, data, Constants.OP_DROP_COURSE));
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_DROP_COURSE, Arrays.asList(studentId, courseId),
				result.getKey(), result.getValue()));
		return returnAny(result);
	}

	private SimpleEntry<Boolean, String> dropCourseOnThisServer(String studentId, String courseId) {
		final Map<Boolean, String> temp = new HashMap<>();
		if (deptDatabase.size() > 0) {
			deptDatabase.forEach((sem, courses) -> {
				if (courses.containsKey(courseId)) {
					courses.forEach((course, courseDetails) -> {

						rl.lock(); // accquire the lock
						if (course.equals(courseId)) {
							boolean status = ((HashSet<String>) courseDetails.get(Constants.STUDENT_IDS))
									.remove(studentId);
							if (status) {
								courseDetails.put(Constants.STUDENTS_ENROLLED,
										((Integer) courseDetails.get(Constants.STUDENTS_ENROLLED) - 1));
								temp.put(true, "success");
							} else {
								temp.put(false, studentId + " isn't enrolled in " + courseId + ".");
							}
						}
						rl.unlock(); // release the lock
					});
				} else {
					temp.put(false, courseId + " isn't offered by the department yet.");
				}
			});
		} else {
			temp.put(false, courseId + " isn't offered by the department yet.");
		}

		if (temp.containsKey(true)) {
			return new SimpleEntry<Boolean, String>(true, "Course Dropped.");
		} else {
			return new SimpleEntry<Boolean, String>(false, temp.get(false));
		}
	}

	@Override
	public Any swapCourse(String studentId, String newCourseId, String oldCourseId) {

		boolean status = true;
		String msg = null;
		String semester = null;
		Any result1;
		SimpleEntry<Boolean, String> result2;

		/* VALIDATIONS */

		// get students class schedule
		Any any = getClassSchedule(studentId);
		HashMap<String, ArrayList<String>> studentSchedule = (HashMap<String, ArrayList<String>>) any.extract_Value();
		Department oldCourseDept = Department.valueOf(oldCourseId.substring(0, 4).toUpperCase());
		Department newCourseDept = Department.valueOf(newCourseId.substring(0, 4).toUpperCase());

		List<String> departmentCourses = new ArrayList<>();
		List<String> outOfDepartmentCourses = new ArrayList<>();
		studentSchedule.forEach((sem, courses) -> {
			courses.forEach((course) -> {
				Department dept = Department.valueOf(course.substring(0, 4).toUpperCase());
				if (dept == this.department)
					departmentCourses.add(course);
				else
					outOfDepartmentCourses.add(course);
			});
		});

		if (!departmentCourses.contains(oldCourseId) && !outOfDepartmentCourses.contains(oldCourseId)) {
			// check if student is enrolled in oldCourse or not
			status = false;
			msg = studentId + " is not enrolled in " + oldCourseId;
		} else if (departmentCourses.contains(newCourseId) || outOfDepartmentCourses.contains(newCourseId)) {
			// check if the student is already enrolled in newCourseId
			status = false;
			msg = studentId + " is already enrolled in " + newCourseId;
		}

		if (!status)
			return returnAny(new SimpleEntry<Boolean, String>(status, msg));

		// get the oldCourseId semester
		outer: for (String sem : studentSchedule.keySet()) {
			for (String c : studentSchedule.get(sem)) {
				if (c.equalsIgnoreCase(oldCourseId)) {
					semester = sem;
					break outer;
				}
			}
		}

		if (newCourseDept == department) {
			// enrolling in this department, dropping elective or this department course
			// check if new course is offered or not

			rl.lock(); // accquire lock
			result2 = checkCouseAvailability(newCourseId, semester);

			if (result2.getKey()) {
				// drop other department course
				result1 = dropCourse(studentId, oldCourseId);
				result2 = (SimpleEntry<Boolean, String>) result1.extract_Value();

				if (result2.getKey()) {
					// enroll in new course
					result2 = enrollmentForThisDepartment(studentId, newCourseId, semester);
					
					if(result2.getKey()) {
						status = true;
						msg = Constants.OP_SWAP_COURSE+" successfully";
					}else {
						//ROLLBACK
						enrolCourse(studentId, oldCourseId, semester);
						status = true;
						msg = Constants.OP_SWAP_COURSE+" successfully";
					}
				}
			}
			rl.unlock();
			// finish

		} else {
			// dropping this dept course, enrolling in elective

			HashMap<String, String> data = new HashMap<>();
			data.put(Constants.STUDENT_ID, studentId);
			data.put(Constants.NEW_COURSE_ID, newCourseId);
			data.put(Constants.OLD_COURSE_ID, oldCourseId);
			data.put(Constants.OLD_COURSE_DEPT, department.toString());
			data.put(Constants.SEMESTER, semester);

			result2 = (SimpleEntry<Boolean, String>) Utils
					.byteArrayToObject(udpCommunication(newCourseDept, data, Constants.OP_SWAP_COURSE));
			status = result2.getKey();
			msg = result2.getValue();
		}

		return returnAny(new SimpleEntry<Boolean, String>(status, msg));
	}

	/**
	 * UDP Server for Inter-Department communication
	 */
	public void UDPServer() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(department.getUdpPort());
			byte[] buffer = new byte[1000];// to stored the received data from the client.
			LOGGER.info(this.department + " UDP Server Started............");
			// non-terminating loop as the server is always in listening mode.
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				// Server waits for the request to come
				socket.receive(request); // request received

				byte[] response = processUDPRequest(request.getData());

				DatagramPacket reply = new DatagramPacket(response, response.length, request.getAddress(),
						request.getPort());// reply packet ready
				socket.send(reply);// reply sent
			}
		} catch (SocketException e) {
			LOGGER.severe("SocketException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IOException : " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
	}

	/**
	 * Handles the UDP request for information
	 * 
	 * @param data
	 * @return
	 */
	private byte[] processUDPRequest(byte[] data) {

		byte[] response = null;
		HashMap<String, Object> request = (HashMap<String, Object>) Utils.byteArrayToObject(data);

		for (String key : request.keySet()) {

			LOGGER.info("Received UDP Socket call for method[" + key + "] with parameters[" + request.get(key) + "]");
			switch (key) {
			case Constants.OP_LIST_COURSE_AVAILABILITY:
				String semester = (String) request.get(key);
				response = Utils.objectToByteArray(listCourseAvailabilityForThisServer(semester));
				break;
			case Constants.OP_ENROL_COURSE:
				HashMap<String, String> info = (HashMap<String, String>) request.get(key);
				response = Utils.objectToByteArray(enrollmentForThisDepartment(info.get(Constants.STUDENT_ID),
						info.get(Constants.COURSE_ID), info.get(Constants.SEMESTER)));
				break;
			case Constants.OP_GET_CLASS_SCHEDULE:
				String studentId = (String) request.get(key);
				response = Utils.objectToByteArray(getClassScheduleThisServer(studentId));
				break;
			case Constants.OP_DROP_COURSE:
				info = (HashMap<String, String>) request.get(key);
				response = Utils.objectToByteArray(
						dropCourseOnThisServer(info.get(Constants.STUDENT_ID), info.get(Constants.COURSE_ID)));
				break;
			case Constants.OP_SWAP_COURSE:
				info = (HashMap<String, String>) request.get(key);
				response = Utils.objectToByteArray(atomicSwapOnThisServer(info.get(Constants.STUDENT_ID),
						info.get(Constants.NEW_COURSE_ID), info.get(Constants.OLD_COURSE_ID),
						info.get(Constants.OLD_COURSE_DEPT), info.get(Constants.SEMESTER)));
				break;
			}
		}

		return response;
	}

	private SimpleEntry<Boolean, String> atomicSwapOnThisServer(String studentId, String newCourseId,
			String oldCourseId, String oldCourseDept, String semester) {

		SimpleEntry<Boolean, String> result;
		boolean status;
		String msg;

		try {
			rl.lock();

			result = checkCouseAvailability(newCourseId, semester);
			if (result.getKey()) {
				HashMap<String, String> data = new HashMap<>();
				data.put(Constants.STUDENT_ID, studentId);
				data.put(Constants.COURSE_ID, oldCourseId);
				result = (SimpleEntry<Boolean, String>) Utils.byteArrayToObject(
						udpCommunication(Department.valueOf(oldCourseDept), data, Constants.OP_DROP_COURSE));

				if (result.getKey()) {
					result = enrollmentForThisDepartment(studentId, newCourseId, semester);
					
					if(result.getKey()) {
						status = true;
						msg = Constants.OP_SWAP_COURSE+" successfully";
					}else {
						//ROLLBACK
						enrolCourse(studentId,oldCourseId,semester);
						status = false;
						msg = Constants.OP_SWAP_COURSE+" unsuccessful";
					}
				} else {
					status = result.getKey();
					msg = result.getValue();
				}
			} else {
				status = result.getKey();
				msg = result.getValue();
			}
			return new SimpleEntry<Boolean, String>(status, msg);
		} finally {
			rl.unlock();
		}
	}

	private SimpleEntry<Boolean, String> checkCouseAvailability(String courseId, String semester) {

		boolean status = true;
		String msg = Constants.EMPTYSTRING;
		if (deptDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = deptDatabase.get(semester);

			if (courses.containsKey(courseId)) {
				HashMap<String, Object> courseDetails = courses.get(courseId);

				if (((Integer) courseDetails.get(Constants.CAPACITY)
						- (Integer) courseDetails.get(Constants.STUDENTS_ENROLLED)) > 0) {
					status = true;

				} else {
					status = false;
					msg = courseId + " is full.";
				}
			} else {
				status = false;
				msg = courseId + " is not offered in " + semester + " semester.";
			}
		} else {
			status = false;
			msg = "No Courses avialable for " + semester + " semester.";
		}

		return new SimpleEntry<Boolean, String>(status, msg);
	}

	/**
	 * Creates & sends the UDP request
	 * 
	 * @param dept
	 * @param info
	 * @param method
	 * @return
	 */
	private byte[] udpCommunication(Department dept, Object info, String method) {

		LOGGER.info("Making UPD Socket Call to " + dept + " Server for method : " + method);

		// UDP SOCKET CALL AS CLIENT
		HashMap<String, Object> data = new HashMap<>();
		byte[] response = null;
		data.put(method, info);
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			byte[] message = Utils.objectToByteArray(data);
			InetAddress remoteUdpHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, message.length, remoteUdpHost, dept.getUdpPort());
			socket.send(request);
			byte[] buffer = new byte[65556];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			socket.receive(reply);
			response = reply.getData();

		} catch (SocketException e) {
			LOGGER.severe("SocketException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IOException : " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}

		return response;
	}

	/**
	 * Cast the java.lang.Object to org.omg.CORBA.Any
	 * 
	 * @param obj Java Object
	 * @return CORBA Any
	 */
	private Any returnAny(Object obj) {
		Any any = orb.create_any();
		any.insert_Value((Serializable) obj);
		return any;
	}
}
