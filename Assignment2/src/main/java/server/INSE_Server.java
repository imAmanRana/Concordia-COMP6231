/*
 * COMP6231 - Distributed Systems | Fall2018
 * Assignment 2 
 * Professor - Rajagopalan Jayakumar
 * Distributed Course Registration System (DCRS) using Java IDL (CORBA)
 */
package server;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import logging.MyLogger;
import remoteObject.EnrollmentImpl;
import remoteObject.EnrollmentInterface;
import remoteObject.EnrollmentInterfaceHelper;
import util.Constants;
import util.Department;

/**
 * <b>Information System Security Department Server</b> <br>
 * INSE
 * 
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

		EnrollmentImpl stub;
		try {
			// setup logging
			setupLogging();

			// create & initialize the ORB;
			// get reference to rootPOA & activate POAManager
			ORB orb = ORB.init(args, null);
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			stub = new EnrollmentImpl("INSE", orb);

			// get object reference from the servant
			Object ref = rootpoa.servant_to_reference(stub);
			EnrollmentInterface href = EnrollmentInterfaceHelper.narrow(ref);

			Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			NameComponent path[] = ncRef.to_name(Department.INSE.toString());
			ncRef.rebind(path, href);

			// start the department's UDP server for inter-department communication
			// the UDP server is started on a new thread
			new Thread(() -> {
				((EnrollmentImpl) stub).UDPServer();
			}).start();
			LOGGER.info("INSE Server ready and waiting.....");
			for (;;) {
				orb.run();
			}

		} catch (Exception e) {
			// TODO - catch only the specific exception
			e.printStackTrace();
		}
	}

	private static void setupLogging() throws IOException {
		File files = new File(Constants.SERVER_LOG_DIRECTORY);
		if (!files.exists())
			files.mkdirs();
		files = new File(Constants.SERVER_LOG_DIRECTORY + "INSE_Server.log");
		if (!files.exists())
			files.createNewFile();
		MyLogger.setup(files.getAbsolutePath());
	}

}
