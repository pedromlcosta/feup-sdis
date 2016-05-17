package monitor;

import messages.Message;

public class monitorProcessor extends Thread {

	private static final int MAX_WAIT = 400;
	private Message msg;
	private byte[] messageBody = null;
	private String[] messageFields = null;
	private String messageString;

	/**
	 * Processor constructor
	 * 
	 * @param messageString
	 *            string received, corresponding to a message
	 */
	public monitorProcessor(String messageString) {
		this.messageString = messageString;
	}

	// Criado por causa das mudanças no receiver e evitar fazer o
	// parseHeader 2x
	/**
	 * Processor constructor
	 * 
	 * @param headerArgs
	 *            arguments of the header of the message
	 * @param body
	 *            body of the message
	 */
	public monitorProcessor(String[] headerArgs, byte[] body) {
		this.messageFields = headerArgs;
		this.messageBody = body;
	}

	/**
	 * Processor constructor
	 * 
	 * @param header
	 *            String that represents the header of the message
	 * @param body
	 *            body of the message
	 */
	public monitorProcessor(String header, byte[] body) {
		this.messageString = header;
		this.messageBody = body;
	}

	/**
	 * Main processor thread. After the constructor is called, this method
	 * accesses the message that was stored in the datamembers and calls the
	 * adequate handlers
	 */
	public void run() {
		// HANDLE MESSAGES HERE
		// TODO msg != null &&
		if (messageString != null || messageFields != null) {
			// legacy reasons (in case some function uses this one)
			if (messageFields == null)
				messageFields = Message.parseHeader(messageString);
			messageString = ""; // Empty, so as not to fill unnecessary
								// space

			// System.out.println(messageFields[0]);

			switch (messageFields[0]) {

			default:
				break;
			}
		}
	}

}