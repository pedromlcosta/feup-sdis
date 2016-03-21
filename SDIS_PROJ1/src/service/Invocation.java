package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Invocation extends Remote{
	// Example: String exampleFunc(String exampleArg) throws RemoteException;
	String backup(String filePath, int desiredRepDeg) throws RemoteException;
	String restore(String filePath) throws RemoteException;
	String delete(String filePath) throws RemoteException;
	String reclaim(int reclaimSpace) throws RemoteException;
}
