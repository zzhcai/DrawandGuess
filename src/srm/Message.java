package srm;

import java.util.Arrays;
import java.util.Map;

/**
 * A wrapper class of a single datagram packet, which
 * specifies a type on which the packet can be handled accordingly.
 */
public class Message
{
	/** The sequence number of message */
	private final long seq;
	/** Whom the datagram is being sent from */
	private final String from;
	/** Either one of those in enum class Type */
	private final Type type;

	/** Body of the message:
	 *  - DATA: byte(payload)
	 *  - SESSION: byte(toJson([t, StateTable::getViewingPage]))
	 *  - REQUEST: byte(${whose}-${seq})
	 *  - REPAIR: byte(toJson([${whose}-${seq}, byte(payload)])) */
	private final byte[] body;

	protected static class SessionBody
	{
		String t;   // LocalTime.toString()
		Map<String, Long[]> view;

		public SessionBody(String t, Map<String, Long[]> view) {
			this.t = t;
			this.view = view;
		}
	}

	protected static class RepairBody
	{
		/** ${whose}-${seq} */
		String whose_seq;
		byte[] payload;

		public RepairBody(String whose_seq, byte[] payload) {
			this.whose_seq = whose_seq;
			this.payload = payload;
		}
	}

	public Message(long seq, String from, Type type, byte[] body) {
		this.seq = seq;
		this.from = from;
		this.type = type;
		this.body = body;
	}

	protected long getSeq() {
		return seq;
	}

	protected String getFrom() {
		return from;
	}

	protected Type getType() {
		return type;
	}

	protected byte[] getBody() {
		return body;
	}

	@Override
	public String toString() {
		return "Message{" +
				"seq=" + seq +
				", from='" + from + '\'' +
				", type=" + type +
				", body=" + Arrays.toString(body) +
				'}';
	}

}
