/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 1 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS)
 */
package server;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

import logging.MyLogger;
import remoteObject.EnrollmentImpl;
import remoteObject.EnrollmentInterface;
import util.Constants;
import util.Department;

/**
 * <b>Information System Security Department Server</b> <br>
 * INSE
 * @author Amandeep Singh
 * @see <a href="www.linkedin.com/in/imamanrana">Profile</a>
 *
 */
public class INSE_Server {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	/**
	 * @param args
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException {
		EnrollmentInterface stub = new EnrollmentImpl("INSE");
		try {
			setupLogging();
			// bind the remote object in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(Department.INSE.toString(), stub);

		} catch (Exception e) {
			// TODO - catch only the specific exception
			e.printStackTrace();
		}

		// start the department's UDP server for inter-department communication
		// the UDP server is started on a new thread
		new Thread(() -> {
			((EnrollmentImpl) stub).UDPServer();
		}).start();

	}
	
	private static void setupLogging() throws IOException {
		File files = new File(Constants.SERVER_LOG_DIRECTORY);
        if (!files.exists()) 
            files.mkdirs(); 
        files = new File(Constants.SERVER_LOG_DIRECTORY+"INSE_Server.log");
        if(!files.exists())
        	files.createNewFile();
        MyLogger.setup(files.getAbsolutePath());
	}

}
