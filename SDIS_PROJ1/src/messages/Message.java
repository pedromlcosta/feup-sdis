
package messages;

import java.util.regex.Pattern;

//TODO devo mudar quase tudo o que está aqui so ignore this for now
//TODO stringbuilder
public class Message {

	private static final String GETCHUNK = "GETCHUNK";
	private static final String CHUNK = "CHUNK";
	private static final String DELETE = "DELETE";
	private static final String REMOVED = "REMOVE";
	private static final String PUTCHUNK = "PUTCHUNK";
	private static final String STORED = "STORED";
	private static final String EMPTY_STRING = "";

	public static enum MESSAGE_TYPE {
		GETCHUNK, CHUNK, DELETE, REMOVED, PUTCHUNK, STORED
	}

	private final String EOL = "\u0013\u0010";
	private final String PATTERN = " |" + EOL;
	private String messageToSend = "";

	// PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg>
	// <CRLF><CRLF><Body>
	// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	// GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	// CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
	// DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
	// REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public String[] parseMessage(String Message) {
		Pattern pattern = Pattern.compile(PATTERN);
		String[] match = pattern.split(Message, -2);
		for (String a : match)
			System.out.println("Print: " + a);
		return match;
	}

	public boolean createHeader(MESSAGE_TYPE type, String... args) {
		if (args.length > 0)
			switch (type) {
			case GETCHUNK:
				return createMessage(args, 3, Message.GETCHUNK);
			case CHUNK:
				return createMessage(args, 3, CHUNK);
			case DELETE:
				return createMessage(args, 2, DELETE);
			case REMOVED:
				return createMessage(args, 3, REMOVED);
			case PUTCHUNK:
				return createMessage(args, 4, PUTCHUNK);
			case STORED:
				return createMessage(args, 3, STORED);
			default:
				return false;
			}
		return false;
	}

	public byte[] getMessageBytes() {
		return messageToSend.getBytes();
	}

	public boolean createMessage(String[] args, int nArgs, String messageType) {
		if (args.length != nArgs)
			return false;
		// clean String
		messageToSend = messageType;
		addArgs(args);
		return true;
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

	public void addData(byte[] data) {
		messageToSend.concat(data.toString());
	}

	public String addData(String string, byte[] data) {
		return string.concat(data.toString());
	}

	public String addEOL(String string) {
		return string.concat(EOL);
	}

	public void discardMessage() {
		this.setMessageToSend("");
	}

	public void addEOL() {
		messageToSend.concat(EOL);
	}

	public void addArgs(String[] args) {
		for (String arg : args) {
			messageToSend.concat(" " + arg);
		}
		addEOL();
	}
}
