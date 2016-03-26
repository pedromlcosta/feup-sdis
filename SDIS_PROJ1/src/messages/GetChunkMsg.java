package messages;

public class GetChunkMsg extends Message {

	private static final int N_ARGS = 4;
	String ChunkNo;

	public GetChunkMsg() {
		type = MESSAGE_TYPE.GETCHUNK;
	}

	public GetChunkMsg(String[] messageFields) {
		// args+body
		if (messageFields.length < (N_ARGS + 1)) {
			System.out.println("Failed creating GetChunk message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
	}

	// GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public boolean createMessage(byte[] data, String... args) {

		createHeader(args, N_ARGS, getGetchunk());
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + CHUNK_NUMBER + MSG_END;
		return createMessageAux(data);
	}
}
