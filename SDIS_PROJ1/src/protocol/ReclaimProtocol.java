package protocol;

public class ReclaimProtocol extends Thread {

	private int reclaimSpace;

	public ReclaimProtocol(int reclaimSpace) {
		this.reclaimSpace = reclaimSpace;
	}

}
