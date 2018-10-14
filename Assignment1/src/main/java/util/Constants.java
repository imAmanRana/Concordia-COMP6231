/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 1 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS)
 */
package util;

/**
 * Class of Constants
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public class Constants {

	public static final String ADVISOR_LOG_DIRECTORY = "./src/main/resources/logs/advisor/";
	public static final String STUDENT_LOG_DIRECTORY = "./src/main/resources/logs/student/";
	public static final String SERVER_LOG_DIRECTORY = "./src/main/resources/logs/server/";
	public static final String UNDERSCORE = "_";
	public static final String EMPTYSTRING = "";
	public static final String CAPACITY = "capacity";
	public static final String STUDENTS_ENROLLED = "studentsEnrolled";
	public static final String STUDENT_IDS = "studentIds";
	public static final String STUDENT_ID = "studentId";
	public static final String COURSE_ID = "courseId";
	public static final String SEMESTER = "semester";
	public static final String LOG_MSG = "METHOD[%s]; PARAMETERS%s; STATUS[%s]; SERVER_MESSAGE[%s]";
	public static final String OP_ADD_COURSE = "addCourse";
	public static final String OP_REMOVE_COURSE = "removeCourse";
	public static final String OP_LIST_COURSE_AVAILABILITY = "listCourseAvailability";
	public static final String OP_ENROL_COURSE = "enrolCourse";
	public static final String OP_GET_CLASS_SCHEDULE = "getClassSchedule";
	public static final String OP_DROP_COURSE = "dropCourse";
	public static int MAX_COURSE_TAKEN_BY_STUDENT = 3;
	public static int MAX_ELECTIVE_COURSES = 2;
}
