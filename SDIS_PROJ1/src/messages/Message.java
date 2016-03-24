
package messages;

import java.util.regex.Matcher;
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

	protected final String EOL = "\u0013\u0010";
	protected final String VALIDATE_MSG_Part1 = "^(?:(?:\\w)* +){";
	protected final String VALIDATE_MSG_Part2 = "}\\w+ *" + EOL + ".*";
	protected final String PATTERN = " |" + EOL;
	protected String messageToSend = "";
	
	//Message attributes
	MESSAGE_TYPE type;
	String Version;
	String senderID;
	String fileId;
	

	// PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg>
	// <CRLF><CRLF><Body>
	// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	// GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	// CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
	// DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
	// REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public synchronized String[] parseMessage(String Message) {
		Pattern pattern = Pattern.compile(PATTERN);
		String[] match = pattern.split(Message, -2);
		//for (String a : match)
			//System.out.println("Print: " + a);
		return match;
	}

	public boolean createMessage(MESSAGE_TYPE type, String args[], byte data[]) {
		createHeader(type, args);
		if (data != null){
			//System.out.println("Message to send before data: " + messageToSend);
			messageToSend = messageToSend.concat(new String(data));
		}
		if (validateMsg(messageToSend, args.length))
			return true;
		else {
			System.out.println("Mensagem nao valida");
			//messageToSend = EMPTY_STRING;
			return false;
		}
	}

	public boolean createHeader(MESSAGE_TYPE type, String... args) {
		boolean created = false;
		
		if (args.length > 0)
						
			switch (type) {
			case GETCHUNK:
				created = createHeaderAux(args, 4, Message.GETCHUNK);
				break;
			case CHUNK:
				created = createHeaderAux(args, 4, CHUNK);
				break;
			case DELETE:
				created = createHeaderAux(args, 3, DELETE);
				break;
			case REMOVED:
				created = createHeaderAux(args, 4, REMOVED);
				break;
			case PUTCHUNK:
				created = createHeaderAux(args, 5, PUTCHUNK);
				break;
			case STORED:
				created = createHeaderAux(args, 4, STORED);
				break;
			default:
				break;
			}
		return created;
	}

	public boolean validateMsg(String s, int nArgs) {
		String validateRegex = new String(VALIDATE_MSG_Part1 + (nArgs) + VALIDATE_MSG_Part2);
		Pattern p = Pattern.compile(validateRegex);
		Matcher m = p.matcher(s);
		return m.matches();
	}

	public boolean createHeaderAux(String[] args, int nArgs, String messageType) {
		if (args.length != nArgs){
			System.out.println("Args given for this type of message: " + args.length);
			System.out.println("Args expected: " + nArgs);
			return false;
		}
		// clean String
		messageToSend = messageType;
		System.out.println("Entrou aqui");
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
	public byte[] getMessageData() {
		Pattern pattern = Pattern.compile(EOL);
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

	public String getVALIDATE_MSG_Part1() {
		return VALIDATE_MSG_Part1;
	}

	public String getVALIDATE_MSG_Part2() {
		return VALIDATE_MSG_Part2;
	}

}
