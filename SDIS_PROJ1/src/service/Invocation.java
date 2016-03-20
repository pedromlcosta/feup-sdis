package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Invocation extends Remote{
	// Example: String exampleFunc(String exampleArg) throws RemoteException;
	String testX(String exampleArg) throws RemoteException;
	String testY(String exampleArg) throws RemoteException;
}
