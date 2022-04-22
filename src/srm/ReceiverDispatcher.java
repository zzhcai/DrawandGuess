package srm;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
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
	 * Skip all SESSION/REQUEST/REPAIR loopback.
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
			if (msg == null || msg.from() == null || msg.type() == null
					|| msg.body() == null) {
				throw new JsonSyntaxException("Null in message.");
			}
			switch (msg.type()) {
			case SESSION, REQUEST, REPAIR -> {
				if (msg.from().equals(socket.getFrom())) throw new LoopbackException();
			}
			}
			ReliableMulticastSocket.logger.info("Received "+msg.type().name()+".");
			return msg;
		}
		catch (IOException e) {
			ReliableMulticastSocket.logger.log(Level.SEVERE, "Socket cannot receive.", e);
			currentThread().interrupt();
			return null;
		}
		// Receives the next
		catch (JsonSyntaxException | LoopbackException e) {
			return receive();
		}
	}

	private static class LoopbackException extends Exception {
		LoopbackException() { super(); }
	}

	/**
	 * Dispatch different tasks corresponding to the message type.
	 *
	 * @param msg the received message
	 */
	@SuppressWarnings("unchecked")
	private void dispatch(Message msg)
	{
		switch (msg.type()) {
		// 1. Put cache
		// 2. Update states
		// 3. Fix a request in pool if there is
		// 4. If any loss detected, submit request via pool
		case DATA -> {
			socket.cache.put(msg);
			Long oldSeq = socket.states.update(msg.from(), msg.seq(), null);
			// TODO: 3
			if (oldSeq != null) {
				for (long i = oldSeq + 1; i < msg.seq(); i++) {
					socket.pool.request(msg.from(), i);
				}
			}
		}
		// 1. Estimate one-way distances to other active sources
		// 2. Compare view with states and update states
		// 3. If any loss detected, submit request via pool
		case SESSION -> {
			long dist;   // t34
			Map<String, Long[]> view;
			try {
				AbstractMap.SimpleEntry<LocalTime, Map<String, Long[]>> pair =
						(AbstractMap.SimpleEntry<LocalTime, Map<String, Long[]>>)
								ReliableMulticastSocket.gson.fromJson(new String(msg.body()), Map.class);
				if (pair != null && pair.getKey() != null && pair.getValue() != null) {
					dist = ChronoUnit.SECONDS.between(pair.getKey(), LocalTime.now());
					view = pair.getValue();
				}
				else return;
			}
			catch (JsonSyntaxException e) { return; }

			for (var v : view.entrySet()) {
				String from = v.getKey();
				if (from != null && v.getValue() != null) {
					Long seq = v.getValue()[0];
					if (seq != null) {
						Long _dist = null;   // t12
						if (from.equals(msg.from())) _dist = v.getValue()[1];
						if (_dist != null) _dist = (_dist + dist) / 2;
						Long oldSeq = socket.states.update(msg.from(), seq, _dist);
						if (oldSeq != null) {
							for (long i = oldSeq + 1; i <= seq; i++) {
								socket.pool.request(v.getKey(), i);
							}
						}
					}
				}
			}
		}
		//
		case REQUEST -> {
			// TODO REQUEST
		}
		//
		case REPAIR -> {
			// TODO REPAIR
		}
		}
	}

}
