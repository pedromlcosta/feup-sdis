package messages;

public class PutChunkMsg extends Message {

	private static final int N_ARGS = 5;
	String ChunkNo;
	String ReplicationDeg;

	public PutChunkMsg() {
		type = MESSAGE_TYPE.PUTCHUNK;
	}

	public PutChunkMsg(String[] messageFields) {
		// type+args+body
		if (messageFields.length < (N_ARGS + 2)) {
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
		body = messageFields[6].getBytes();
	}

	// PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg>
	// <CRLF><CRLF><Body>
	public boolean createMessage(byte[] data, String... args) {

		createHeader(args, N_ARGS, getPutchunk());
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + CHUNK_NUMBER + MORE_THAN_1_SPACE + DREGREE_ARG + MSG_END;
		System.out.println(validateRegex);

		return createMessageAux(data);

	}
}
