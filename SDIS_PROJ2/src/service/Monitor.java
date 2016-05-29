package service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.Socket;
import java.net.UnknownHostException;

import tracker.Tracker;

public class Monitor {
	Monitor instance = this;

	private static final int LIMIT_OF_ATTEMPTS = 3;
	static Socket peerSocket;
	static PrintWriter out = null;
	static BufferedReader in = null;
	static boolean connectionAlive = false;
	private static int nTries = 0;
	private static int resAttempts = 0;
	private static boolean peerAlive = false;
	private static boolean peerResurrectedAttempted = false;
	static int serverID;
	static String[] peerMainArgs;
	static String creator;
	static int beepPort;

	public Monitor(int beepServerPort) throws IOException {
		System.out.println("entered monitor constructor");
		try {
			peerSocket = new Socket("localhost", beepServerPort);
			PrintWriter out = new PrintWriter(peerSocket.getOutputStream(),
					true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					peerSocket.getInputStream()));
			connectionAlive = true;
			peerAlive = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Port nr: " + beepServerPort);
	}

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Entered peer " + args[0] + " monitor's main\n");
		creator = args[0];
		System.out.println(creator);
		beepPort = Integer.parseInt(args[1]);
		peerMainArgs = args;
		startBeeping();

	}

	public static void startBeeping() {
		try {
			Socket socket = new Socket("localhost", beepPort);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			connectionAlive = true;
			peerAlive = true;
			String fromUser, fromServer;

			// send 1st msg
			fromUser = "MONITOR_BEEP";
			if (fromUser != null) {
				out.println(fromUser);
				System.out.println("Monitor: " + fromUser);
				Thread.sleep(750);
			}
			boolean first = true;
			System.out.println("here " + connectionAlive);
			beeps(first);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			connectionAlive = false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void beeps(boolean first) {
		try {
			String fromUser, fromServer;
			while (connectionAlive) {
				// Receive
				if (in == null)
					System.out.println("in é null");
				if (in.ready() || first) {
					first = false;
					System.out.println("buffer not empty");
					if ((fromServer = in.readLine()) != null) {
						if (peerResurrectedAttempted) {
							peerResurrectedAttempted = false;
							resAttempts = 0;
						}
						System.out.println("received: " + fromServer);
						fromUser = "MONITOR_BEEP";
						Thread.sleep(1000);
						peerAlive = true;
						out.println(fromUser);
						System.out.println("sent: " + fromUser);
					}
				} else if (peerResurrectedAttempted) {
					nTries = LIMIT_OF_ATTEMPTS + 1;
				} else {
					nTries++;
					peerAlive = false;
					int triesLeft = LIMIT_OF_ATTEMPTS - nTries;
					System.out.println("Trying to reconect " + triesLeft
							+ "more time(s)");
					Thread.sleep(500);
				}
				if (peerAlive) {
					System.out.println("Peer alive");
					nTries = 0;
					peerAlive = false;

				} else {
					if (nTries >= LIMIT_OF_ATTEMPTS) {
						if (resAttempts >= LIMIT_OF_ATTEMPTS) {
							System.out.println("couldn't resurect " + creator
									+ ". Please try manually");
							return;
						}
						peerResurrectedAttempted = true;
						System.out.println("Trying to ressurect Peer/Tracker");
						attemptResurrection();
						resAttempts++;
						return;
					}
				}
				Thread.sleep(1000);
			}
		} catch (IOException e) {
			connectionAlive = false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void attemptResurrection() {
		try {
			createResProcess(peerMainArgs);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void createResProcess(String[] args) throws IOException,
			InterruptedException {
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator
				+ "java";
		String classpath = System.getProperty("java.class.path");
		Class monitorClass;
		String className;
		ProcessBuilder builder = null;
		File log = null;
		// createProcess builder
		System.out.println(creator);
		if (creator.equals("PEER")) {
			System.out.println("INSIDE HERE");
			monitorClass = Peer.class;
			className = monitorClass.getCanonicalName();
			builder = new ProcessBuilder(javaBin, "-cp", classpath, className,
					args[2], args[3], args[4], args[5], args[6], args[7],
					args[8], args[9], args[10], "RESTART	");
			File oldLog = new File("peer_logs\\peer" + args[2] + "_log");
			oldLog.delete();
			log = new File("peer_logs\\peer" + args[2] + "_log");
		} else if (creator.equals("TRACKER")) {
			monitorClass = Tracker.class;
			className = monitorClass.getCanonicalName();
			builder = new ProcessBuilder(javaBin, "-cp", classpath, className,
					args[2]);
			File oldLog = new File("tracker_logs\\tracker_log");
			oldLog.delete();
			log = new File("tracker_logs\\tracker_log");
		}

		builder.redirectErrorStream(true);
		builder.redirectOutput(Redirect.appendTo(log));
		Process process = builder.start();
		assert builder.redirectOutput().file() == log;
		// process.waitFor();
		// return process.exitValue();
	}

}
