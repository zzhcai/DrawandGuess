package srm;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.logging.Level;

/**
 * A background thread which receives all datagram packet from the socket,
 * creates and dispatches tasks to handle them differently.
 */
public class ReceiverDispatcher extends Thread
{
	private final ReliableMulticastSocket socket;

	public ReceiverDispatcher(ReliableMulticastSocket socket) {
		this.socket = socket;
	}

	public void run() {
		while (!interrupted()) {
			Message msg = receive();
			if (msg != null) dispatch(msg);
		}
	}

	/**
	 * Receive the next message in the valid format.
	 *
	 * @return message; null if IOException occurs
	 */
	private Message receive()
	{
		// A UDP datagram is carried in a single IP packet, and is hence
		// limited to a maximum payload of 65,507 bytes for IPv4 and 65,527 bytes for IPv6.
		DatagramPacket p = new DatagramPacket(new byte[65507], 65507);
		Message msg;
		try {
			socket._receive(p);
			msg = ReliableMulticastSocket.gson.fromJson(
					new String(p.getData(), 0, p.getLength()), Message.class);
			if (msg == null || msg.getFrom() == null || msg.getType() == null
					|| msg.getPayload() == null) {
				throw new JsonSyntaxException("Null in message.");
			}
			ReliableMulticastSocket.logger.info("Received "+msg.getType().name()+".");
			return msg;
		}
		catch (IOException e) {
			ReliableMulticastSocket.logger.log(Level.SEVERE, "Socket cannot receive.", e);
			currentThread().interrupt();
			return null;
		}
		// If in bad format, receives the next
		catch (JsonSyntaxException ignored) {
			return receive();
		}
	}

	/**
	 * Dispatch different tasks corresponding to the message type.
	 *
	 * @param msg the received message
	 */
	private void dispatch(Message msg)
	{
		switch (msg.getType()) {
		// 1. Update states
		// 2. Put cache
		case DATA -> {
			socket.states.putIfGreater(msg.getFrom(),
					new AbstractMap.SimpleEntry<>(msg.getSeq(), LocalTime.now()));
			socket.cache.put(msg);
		}
		case SESSION -> {

		}
		case REQUEST -> {
			// TODO 1
		}
		case REPAIR -> {
			// TODO 2
		}
		}
	}

}
