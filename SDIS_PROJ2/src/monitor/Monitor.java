package monitor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import channels.MulticastServer;
import channels.UDPConnection;
import messages.Message;

public class Monitor {
	private UDPConnection peerConnection;
	private MulticastServer trackerConnection;
	private Integer peerID;
	private boolean peerAlive = false;
	private final int TIME_LIMIT = 30;
	private Timer timer = new Timer();
	private Random randomGenerator = new Random();
	private boolean peerResurrectedAttempted = false;
	private int nTries = 0;
	private final int LIMIT_OF_ATTEMPTS = 3;

	public Monitor(InetAddress addrUDP, int portUDP, InetAddress addrMC, int portMC) throws IOException {
		this.peerConnection = new UDPConnection(addrUDP, portUDP);
		this.trackerConnection = new MulticastServer(false, addrMC, portMC);
		this.trackerConnection.createSocket();
		this.trackerConnection.joinMulticastGroup();
	 
	}

	public class beepTask extends TimerTask {
		@Override
		public void run() {
			task();
		}
	}

	private void task() {
		if (peerAlive) {
			nTries = 0;
			peerAlive = false;
			// create msg all well
			// send beep to tracker
		} else {
			if (peerResurrectedAttempted) {
				peerResurrectedAttempted = false;
				System.out.println("failed To Ressurect Peer");
				// Action to take??

			} else {
				if (nTries >= LIMIT_OF_ATTEMPTS) {
					peerResurrectedAttempted = true;
					attemptResurrection();
				}
				nTries++;
			}
			// send beep peer dead

		}
		// send msg
		timer.schedule(new beepTask(), randomGenerator.nextInt(TIME_LIMIT) * 1000);
	}

	private void attemptResurrection() {

	}

	public UDPConnection getPeerConnection() {
		return peerConnection;
	}

	public void setPeerConnection(UDPConnection peerConnection) {
		this.peerConnection = peerConnection;
	}

	public MulticastServer getTrackerConnection() {
		return trackerConnection;
	}

	public void setTrackerConnection(MulticastServer trackerConnection) {
		this.trackerConnection = trackerConnection;
	}

	public Integer getPeerID() {
		return peerID;
	}

	public void setPeerID(Integer peerID) {
		this.peerID = peerID;
	}

	public boolean isPeerAlive() {
		return peerAlive;
	}

	public void setPeerAlive(boolean peerAlive) {
		this.peerAlive = peerAlive;
	}

	public int getTIME_LIMIT() {
		return TIME_LIMIT;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public Random getRandomGenerator() {
		return randomGenerator;
	}

	public void setRandomGenerator(Random randomGenerator) {
		this.randomGenerator = randomGenerator;
	}

}
