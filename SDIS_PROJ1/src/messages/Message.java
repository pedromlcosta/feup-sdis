
package messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extra.Extra;

public class Message {

	protected static final String GETCHUNK = "GETCHUNK";
	protected static final String CHUNK = "CHUNK";
	protected static final String DELETE = "DELETE";
	protected static final String REMOVED = "REMOVE";
	protected static final String PUTCHUNK = "PUTCHUNK";
	protected static final String STORED = "STORED";
	protected static final String EMPTY_STRING = "";

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
	// protected final String VALIDATE_MSG_Part1 = "(?:\\w+ +){";

	// protected final String VALIDATE_MSG_Part2 = "}\\w+ *" + EOL + EOL + ".*";
	public final static String PATTERN = " *";
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

	// TODO check body with string and non ascii
	// PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg>
	// <CRLF><CRLF><Body>
	// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	// GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	// CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
	// DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
	// REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public synchronized String[] parseHeader(String header) {
		Pattern pattern = Pattern.compile(PATTERN);
		String[] match = pattern.split(header, -2);

		// for (String a : match) {
		// System.out.println("Print: " + a);
		//
		// }

		return Extra.eraseEmpty(match);
	}

	public Message() {

	}

	// Only "daugther" classes should be used to create
	public boolean createMessage(byte data[], String... args) {
		return false;
	}

	public boolean createMessageAux(byte data[]) {
		System.out.println(messageToSend);
		if (validateMsg(messageToSend)) {
			System.out.println("Message Valid");
			if (data != null) {
				// String s = new String(data);
				// System.out.println(data.length);
				// System.out.println(s);
				messageToSend = messageToSend.concat(new String(data));
			}
			return true;
		} else {
			System.out.println("Mensagem nao valida");
			// messageToSend = EMPTY_STRING;
			return false;
		}
	}
	// public boolean createHeader(MESSAGE_TYPE type, String... args) {
	//
	// if (args.length > 0) {
	//
	// switch (type) {
	// case GETCHUNK:
	// return createHeaderAux(args, 4, GETCHUNK);
	// case CHUNK:
	// return createHeaderAux(args, 4, CHUNK);
	// case DELETE:
	// return createHeaderAux(args, 3, DELETE);
	// case REMOVED:
	// return createHeaderAux(args, 4, REMOVED);
	// case PUTCHUNK:
	// return createHeaderAux(args, 5, PUTCHUNK);
	// case STORED:
	// return createHeaderAux(args, 4, STORED);
	//
	// }
	// }
	// return false;
	// }

	public boolean validateMsg(String s) {

		System.out.println(validateRegex);
		Pattern p = Pattern.compile(validateRegex);
		Matcher m = p.matcher(s);
		return m.matches();
	}

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

	public String addEOL(String string) {
		return string.concat(EOL);
	}

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

	public void discardMessage() {
		this.setMessageToSend("");
	}

	// From here there are only gets and sets
	// TODO 2 EOL are needed
	public byte[] getMessageData() {
		Pattern pattern = Pattern.compile(EOL + EOL);
		String[] match = pattern.split(getMessageToSend(), -2);

		return match[1].getBytes();

	}

	public byte[] getMessageBytes() {

		if (messageToSend != null)
			return messageToSend.getBytes();
		else {
			System.out.println("MESSAGE NULL");
			System.out.println(messageToSend);
			return null;
		}
	}

	public String getEOL() {
		return EOL;
	}

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
