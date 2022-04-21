package srm;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
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
					|| msg.getBody() == null) {
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
		catch (JsonSyntaxException e) {
			return receive();
		}
	}

	/**
	 * Dispatch different tasks corresponding to the message type.
	 *
	 * @param msg the received message
	 */
	@SuppressWarnings("unchecked")
	private void dispatch(Message msg)
	{
		switch (msg.getType()) {
		// 1. Update states
		// 2. Put cache
		case DATA -> {
			socket.states.putIfAbsentOrGreater(msg.getFrom(),
					new SimpleEntry<>(msg.getSeq(), LocalTime.now()));
			socket.cache.put(msg);
		}
		// 1. Estimate one-way distances to other active sources
		// 2. Compare view with states and update states
		// 3. If any loss detected, submit request via pool
		case SESSION -> {
			LocalTime t;
			Map<String, Long> view;
			try {
				SimpleEntry<LocalTime, String> pair = (SimpleEntry<LocalTime, String>)
						ReliableMulticastSocket.gson.fromJson(
								new String(msg.getBody()), Map.class);
				if (pair != null && pair.getKey() != null && pair.getValue() != null) {   // valid?
					t = pair.getKey();
					view = ReliableMulticastSocket.gson.fromJson(pair.getValue(), Map.class);
					if (view == null) return;
				}
				else return;
			}
			catch (JsonSyntaxException ignored) { return; }

			for (var v : view.entrySet()) {
				Long oldSeq = socket.states.putIfAbsentOrGreater(v.getKey(),
						new SimpleEntry<>(v.getValue(), LocalTime.now()));
				// Loss
				if (oldSeq != null) {
					for (long i = oldSeq + 1; i <= v.getValue(); i++) {
						socket.pool.request(v.getKey(), i);
					}
				}
			}
		}
		//
		case REQUEST -> {
			// TODO 1
		}
		//
		case REPAIR -> {
			// TODO 2
		}
		}
	}

}
