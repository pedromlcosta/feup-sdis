package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Invocation extends Remote{
	// Example: String exampleFunc(String exampleArg) throws RemoteException;
	String backup(String exampleArg) throws RemoteException;
	String restore(String exampleArg) throws RemoteException;
	String delete(String exampleArg) throws RemoteException;
	String reclaim(String exampleArg) throws RemoteException;
}
