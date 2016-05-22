package monitor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import channels.MulticastServer;
import channels.UDPConnection;

public class Monitor {
//	private UDPConnection peerConnection;
//	private MulticastServer trackerConnection;
//	private Integer peerID;
//	private boolean peerAlive = false;
//	private final int TIME_LIMIT = 30;
//	private Timer timer = new Timer();
//	private Random randomGenerator = new Random();
//	private boolean peerResurrectedAttempted = false;
//	private int nTries = 0;
//	private final int LIMIT_OF_ATTEMPTS = 3;
	
	int peerPort;
	int monitorPort;

	public Monitor(int peerPort, int monitorPort){
		this.peerPort=peerPort;
		this.monitorPort=monitorPort;
	}

	public class beepTask extends TimerTask {
		@Override
		public void run() {
			
		}
	}


	

}
