package messages;

public class PutChunkMsg extends Message {

	private static final int N_ARGS = 5;
	String ChunkNo;
	String ReplicationDeg;

	/**
	 * Constructor that just fills the type of Message
	 */
	public PutChunkMsg() {
		type = MESSAGE_TYPE.PUTCHUNK;
	}

	/**
	 * Constructor for the Chunk Msg that receives the messageFields and the
	 * messageFields: Version SenderID FileID ChunkNo body (Chunk data)
	 * 
	 * @param messageFields
	 * @param data
	 */
	public PutChunkMsg(String[] messageFields, byte[] data) {
		// type+args+body
		if (messageFields.length != (N_ARGS + 1)) {
			System.out.println("Failed creating PutChunk message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3]; // process fileID back to normal SHA here? //
									// without the SHA it means nothing? it´s
									// just name+path+Last Modification FDate
		chunkNo = Integer.parseInt(messageFields[4]);
		replicationDeg = Integer.parseInt(messageFields[5]);
		body = data;
	}

	// PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg>
	// <CRLF><CRLF><Body>
	/**
	 * creates a message with the data and the args but also validates the
	 * created message
	 */
	public boolean createMessage(byte[] data, String... args) {
		// Data must not be null,sending a chunk, we must have the its body
		if (data == null) {
			System.out.println("Missing body");
			return false;
		}
		this.body = data;
		// Creates the message header
		createHeader(args, N_ARGS, getPutchunk());
		// Regex that validates the message
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + CHUNK_NUMBER
				+ MORE_THAN_1_SPACE + DREGREE_ARG + MSG_END_WITH_BODY;

		return createMessageAux();
	}

	public static int getnArgs() {
		return N_ARGS;
	}
	
}
