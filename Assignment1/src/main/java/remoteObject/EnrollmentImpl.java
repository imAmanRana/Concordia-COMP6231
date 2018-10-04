/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 1 
 * Distributed Course Registration System (DCRS)
 */
package remoteObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import util.Constants;
import util.Department;
import util.Utils;

/**
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public class EnrollmentImpl extends UnicastRemoteObject implements EnrollmentInterface {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private static final long serialVersionUID = 1L;

	private Department department;

	private HashMap<String, HashMap<String, HashMap<String, Object>>> deptDatabase;

	protected EnrollmentImpl() throws RemoteException {
		super();
	}

	public EnrollmentImpl(String dept) throws RemoteException {
		this.department = Department.valueOf(dept);
		deptDatabase = new HashMap<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see remoteObject.EnrollmentInterface#addCourse(java.lang.String,
	 * java.lang.String, java.lang.String, int)
	 */
	@Override
	public boolean addCourse(String advisorId, String courseId, String semester, int capacity) throws RemoteException {
		boolean status = false;
		String msg = Constants.EMPTYSTRING;
		if (deptDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = deptDatabase.get(semester);

			if (courses.containsKey(courseId)) {
				status = false;
				msg = "Course already exists for "+semester+" semester.";
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
			HashMap<String, Object> courseDetails = new HashMap<>();
			courseDetails.put(Constants.CAPACITY, capacity);
			courseDetails.put(Constants.STUDENTS_ENROLLED, 0);
			courseDetails.put(Constants.STUDENT_IDS, new HashSet<String>());
			HashMap<String, HashMap<String, Object>> courses = new HashMap<>();
			courses.put(courseId, courseDetails);
			this.deptDatabase.put(semester, courses);
			status = true;
			msg = courseId + " Added.";
		}

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
	@Override
	public boolean removeCourse(String advisorId, String courseId, String semester) throws RemoteException {

		boolean status = false;
		String msg = Constants.EMPTYSTRING;
		if (deptDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = deptDatabase.get(semester);

			if (courses.containsKey(courseId)) {
				courses.remove(courseId);
				status = true;
				msg = courseId + " removed";
			} else {
				status = false;
				msg = semester + " semester doesn't offer this course yet.";
			}
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
	public HashMap<String, Integer> listCourseAvailability(String advisorId, String semester) throws RemoteException {

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

		return result;
	}

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
	 * @see remoteObject.EnrollmentInterface#enrolCourse(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public SimpleEntry<Boolean, String> enrolCourse(String studentId, String courseId, String semester)
			throws RemoteException {

		boolean status = true;
		String msg = null;
		SimpleEntry<Boolean, String> result = null;

		// get student schedule
		HashMap<String, ArrayList<String>> studentSchedule = getClassSchedule(studentId);
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

			// student already enrolled in 3 courses
			if (studentSchedule.containsKey(semester)
					&& studentSchedule.get(semester).size() >= Constants.MAX_COURSE_TAKEN_BY_STUDENT) {
				status = false;
				msg = studentId + " is already enrolled in " + Constants.MAX_COURSE_TAKEN_BY_STUDENT + " courses "
						+ studentSchedule.get(semester) + " for this " + semester + " semester.";
			}

			// student already taking this course
			if (departmentCourses.contains(courseId)) {
				status = false;
				msg = studentId + " is already enrolled in "+courseId+".";
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

			status = false;
			msg = "Department not found.";
		}

		if (result == null)
			result = new SimpleEntry<Boolean, String>(status, msg);

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_ENROL_COURSE,
				Arrays.asList(studentId, courseId, semester), result.getKey(), result.getValue()));

		return result;
	}

	private SimpleEntry<Boolean, String> enrollmentForThisDepartment(String studentId, String courseId,
			String semester) {
		boolean status;
		String msg;
		if (deptDatabase.containsKey(semester)) {
			HashMap<String, HashMap<String, Object>> courses = deptDatabase.get(semester);

			if (courses.containsKey(courseId)) {
				HashMap<String, Object> courseDetails = courses.get(courseId);

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
						msg = studentId + " is already enrolled in "+courseId+".";
					}
				} else {
					status = false;
					msg = courseId + " is full.";
				}
			} else {
				status = false;
				msg = courseId + " is not offered in "+semester+" semester.";
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
	 * @see remoteObject.EnrollmentInterface#getClassSchedule(java.lang.String)
	 */
	@Override
	public HashMap<String, ArrayList<String>> getClassSchedule(String studentId) throws RemoteException {
		HashMap<String, ArrayList<String>> schedule = new HashMap<>();
		schedule.putAll(getClassScheduleThisServer(studentId));

		// inquire different departments
		for (Department dept : Department.values()) {
			if (dept != this.department) {
				schedule.putAll((HashMap<String, ArrayList<String>>) Utils
						.byteArrayToObject(udpCommunication(dept, studentId, Constants.OP_GET_CLASS_SCHEDULE)));
			}
		}
		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_GET_CLASS_SCHEDULE, Arrays.asList(studentId),
				schedule != null, schedule));
		return schedule;
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
	 * @see remoteObject.EnrollmentInterface#dropCourse(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public SimpleEntry<Boolean, String> dropCourse(String studentId, String courseId) throws RemoteException {

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
		return result;
	}

	private SimpleEntry<Boolean, String> dropCourseOnThisServer(String studentId, String courseId) {
		final Map<Boolean, String> temp = new HashMap<>();
		if (deptDatabase.size() > 0) {
			deptDatabase.forEach((sem, courses) -> {
				if (courses.containsKey(courseId)) {
					courses.forEach((course, courseDetails) -> {
						if (course.equals(courseId)) {
							boolean status = ((HashSet<String>) courseDetails.get(Constants.STUDENT_IDS))
									.remove(studentId);
							if (status) {
								courseDetails.put(Constants.STUDENTS_ENROLLED,
										((Integer) courseDetails.get(Constants.STUDENTS_ENROLLED) - 1));
								temp.put(true, "success");
							} else {
								temp.put(false, studentId + " isn't enrolled in "+courseId+".");
							}
						}

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
				response = Utils
						.objectToByteArray(dropCourseOnThisServer(info.get(Constants.STUDENT_ID), info.get(Constants.COURSE_ID)));
				break;
			}
		}

		return response;
	}

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

}
