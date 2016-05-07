package messages;

public class WakeMsg extends Message {

	private static final int N_ARGS = 4;
	String ChunkNo;

	/**
	 * Constructor that just fills the type of Message
	 */
	public WakeMsg() {
		type = MESSAGE_TYPE.WAKEUP;
	}

	/**
	 * Constructor for the Stored Msg that receives the messageFields and the
	 * messageFields: Version SenderID FileID ChunkNo body (Chunk data)
	 * 
	 * @param messageFields
	 * @param data
	 *            -> there shouldn't be any data in a delete message
	 */
	public WakeMsg(String[] messageFields, byte[] data) {
		// type+args
		if (messageFields.length < (N_ARGS)) {
			System.out.println("Failed creating WakeUp message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
		body = null;
	}

	// TODO there are 2 types of wakeUp messages that only differ on 1 argument
	// and the handler
	// TODO no this is poorly done,must think about it
	// Need to split in two phases, phase 1 check the chunks we have stored
	// phase 2 check the chunks of our files, second step re-send if needed to
	// get repDegree, will it happen own it´s own or will it need to be forced?
	//Or to the File ones we get STORED replay for the chunk they own
	// Own -> is a 0/1 field that says the SenderID owns the chunk or is trying to find out if the file is still in the system
	// to do that we could change the message and just be and <Answer> would be like 0.0/1.0 depending if the sender is answering or not
	// WAKEUP <Version> <SenderId> <FileId> <CRLF><CRLF>
	
	// WAKEUP <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	/**
	 * creates a message with the data and the args but also validates the
	 * created message
	 */
	public boolean createMessage(byte[] data, String... args) {

		// Does not have a body
		if (data != null) {
			System.out.println("Mensagem nao valida");
			return false;
		}
		// Creates the message header
		createHeader(args, N_ARGS, getStored());
		// Regex that validates the message
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + CHUNK_NUMBER
				+ MSG_END_WITHOUT_BODY;

		return createMessageAux();

	}

}
