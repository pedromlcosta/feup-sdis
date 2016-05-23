package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Invocation extends Remote {
	// Example: String exampleFunc(String exampleArg) throws RemoteException;

	/**
	 * RMI call for the backup protocol
	 * 
	 * @param filePath
	 *            relative path of the file to backup
	 * @param desiredRepDeg
	 *            desired degree of replication for the files chunks
	 * @return String indicating that the protocol has finished
	 * @throws RemoteException
	 */
	String backup(String filePath, int desiredRepDeg) throws RemoteException;

	/**
	 * RMI call for the restore protocol
	 * 
	 * @param filePath
	 *            path of the file we want to restore. Must be the same path as
	 *            of when the file was backed up
	 * 
	 * @return String indicating that the protocol has finished
	 * @throws RemoteException
	 */
	String restore(String filePath) throws RemoteException;

	/**
	 * RMI call for the delete protocol
	 * 
	 * @param filePath
	 *            relative path of the file we want to delete. Must be same path
	 *            as of when the file was backed up
	 * 
	 * @return String indicating that the protocol has finished
	 * @throws RemoteException
	 */
	String delete(String filePath) throws RemoteException;

	/**
	 * RMI call for the reclaim protocol
	 * 
	 * @param reclaimSpace
	 *            total space to try to reclaim for the Peer
	 * @return String indicating that the protocol has finished
	 * @throws RemoteException
	 */
	String reclaim(int reclaimSpace) throws RemoteException;

	String wakeUp() throws RemoteException;

	String checkChunks() throws RemoteException;

	String testTCP() throws RemoteException;
}
