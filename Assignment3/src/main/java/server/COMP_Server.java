/*
* COMP6231 - Distributed Systems | Fall2018
* Assignment 3 
* Professor - Rajagopalan Jayakumar
* Distributed Course Registration System (DCRS) using Web Services
*/
package server;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import logging.MyLogger;
import remoteObject.EnrollmentImpl;
import remoteObject.EnrollmentInterface;
import util.Constants;
import util.Department;

/**
 * @author Amandeep Singh
 * @see <a href='https://www.linkedin.com/in/imamanrana/' target=
 *      "_blank">Profile</a>
 */
public class COMP_Server {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static EnrollmentInterface skelton;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		try {
			setupLogging();
			Department comp = Department.COMP;
			skelton = new EnrollmentImpl(comp.toString());
			Endpoint endpoint = Endpoint.publish("http://localhost:"+comp.getWebServicePort()+"/" + comp.toString().toLowerCase(), skelton);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// start the department's UDP server for inter-department communication
		// the UDP server is started on a new thread
		new Thread(() -> {
			((EnrollmentImpl) skelton).UDPServer();
		}).start();

	}

	/**
	 * Logging setup for COMP server
	 * 
	 * @throws IOException
	 */
	private static void setupLogging() throws IOException {
		File files = new File(Constants.SERVER_LOG_DIRECTORY);
		if (!files.exists())
			files.mkdirs();
		files = new File(Constants.SERVER_LOG_DIRECTORY + "COMP_Server.log");
		if (!files.exists())
			files.createNewFile();
		MyLogger.setup(files.getAbsolutePath());
	}

}
