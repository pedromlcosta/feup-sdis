
package messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extra.Extra;

public class Message {

	static final String GETCHUNK = "GETCHUNK";
	static final String CHUNK = "CHUNK";
	static final String DELETE = "DELETE";
	static final String REMOVED = "REMOVED";
	static final String PUTCHUNK = "PUTCHUNK";
	static final String STORED = "STORED";
	static final String EMPTY_STRING = "";

	public static enum MESSAGE_TYPE {
		GETCHUNK, CHUNK, DELETE, REMOVED, PUTCHUNK, STORED
	}

	public static final String EOL = "\r\n";
	final String VALIDATE_MESSAGE_TYPE = "^(?:\\w+)";
	final String MORE_THAN_1_SPACE = " +";
	final String ZERO_OR_MORE_SPACES = " *";
	final String VALIDATE_VERSION = "(?:\\d\\.\\d)";
	final String MIDDLE_ARGS = "(?:\\w+)";
	final String CHUNK_NUMBER = "\\d{1,6}";
	final String DREGREE_ARG = "(?:\\d)";
	final String MSG_END_WITHOUT_BODY = " *" + EOL + EOL;
	final String MSG_END_WITH_BODY = MSG_END_WITHOUT_BODY + ".*";
	public final static String PATTERN = " ";
	String messageToSend = "";

	// Message attributes
	MESSAGE_TYPE type;
	String version;
	String senderID;
	String fileId;
	int chunkNo;
	int replicationDeg;
	byte[] body;
	String validateRegex;

	// PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg>
	// <CRLF><CRLF><Body>
	// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	// GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	// CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
	// DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
	// REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	/**
	 * 
	 * @param header
	 * @return an array with the values that makeup the header of a message
	 */
	public static synchronized String[] parseHeader(String header) {
		Pattern pattern = Pattern.compile(PATTERN);
		String[] match = pattern.split(header, -2);
		// erases the empty strings from the match array
		return Extra.eraseEmpty(match);
	}

	/**
	 * Empty constructor for message
	 */
	public Message() {
	}

	// Only "daugther" classes should be used to create
	public boolean createMessage(byte data[], String... args) {
		return false;
	}

	/**
	 * 
	 * @return true if the message is valid
	 */
	public boolean createMessageAux() {
		if (validateMsg(messageToSend)) {
			return true;
		} else {
			System.out.println("Mensagem nao valida");
			return false;
		}
	}

	/**
	 * Validates the message
	 * 
	 * @param s
	 * @return true if the message is according to the validateRegex otherwise
	 *         it returns false
	 */
	public boolean validateMsg(String s) {
		Pattern p = Pattern.compile(validateRegex);
		Matcher m = p.matcher(s);
		return m.matches();
	}

	/**
	 * Creates a header for the message
	 * 
	 * @param args
	 * @param nArgs
	 * @param messageType
	 * @return true
	 */
	public boolean createHeader(String[] args, int nArgs, String messageType) {
		if (args.length != nArgs) {
			System.out.println("Args given for this type of message: " + args.length);
			System.out.println("Args expected: " + nArgs);
			return false;
		}
		// clean String
		messageToSend = messageType;
		addArgs(args);
		return true;
	}

	/**
	 * adds EOL to the string
	 * 
	 * @param string
	 * @return
	 */
	public String addEOL(String string) {
		return string.concat(EOL);
	}

	/**
	 * adds EOL to the message
	 */
	public void addEOL() {
		messageToSend = messageToSend.concat(EOL);
	}

	public void addArgs(String[] args) {
		for (String arg : args) {
			messageToSend = messageToSend.concat(" " + arg);
		}
		addEOL();
		addEOL();
	}

	/**
	 * discards the current Message
	 */
	public void discardMessage() {
		this.setMessageToSend(EMPTY_STRING);
	}

	/**
	 * 
	 * @return the data (body) of the message
	 */
	public byte[] getMessageData() {
		return body;
	}

	/**
	 * turns the whole message into an byte[] array
	 * 
	 * @return header+body in a single byte[]
	 */
	public byte[] getMessageBytes() {

		byte[] headerBytes = messageToSend.getBytes();
		byte[] message;
		if (body != null) {
			message = new byte[body.length + headerBytes.length];
			System.arraycopy(headerBytes, 0, message, 0, headerBytes.length);
			System.arraycopy(body, 0, message, headerBytes.length, body.length);
		} else {
			message = new byte[headerBytes.length];
			System.arraycopy(headerBytes, 0, message, 0, headerBytes.length);
		}
		return message;
	}
/**
 * 
 * @return EOL
 */
	public String getEOL() {
		return EOL;
	}

	/**
	 * 
	 * @return PATTERN
	 */
	public String getPATTERN() {
		return PATTERN;
	}

	public String getMessageToSend() {
		return messageToSend;
	}

	public void setMessageToSend(String messageToSend) {
		this.messageToSend = messageToSend;
	}

	public static String getEmptyString() {
		return EMPTY_STRING;
	}

	public static String getGetchunk() {
		return GETCHUNK;
	}

	public static String getChunk() {
		return CHUNK;
	}

	public static String getDelete() {
		return DELETE;
	}

	public static String getRemoved() {
		return REMOVED;
	}

	public static String getPutchunk() {
		return PUTCHUNK;
	}

	public static String getStored() {
		return STORED;
	}

	public MESSAGE_TYPE getType() {
		return type;
	}

	public void setType(MESSAGE_TYPE type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSenderID() {
		return senderID;
	}

	public void setSenderID(String senderID) {
		this.senderID = senderID;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	public int getReplicationDeg() {
		return replicationDeg;
	}

	public void setReplicationDeg(int replicationDeg) {
		this.replicationDeg = replicationDeg;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

}
