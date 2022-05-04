package srm;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.Future;
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
			if (msg == null || msg.getFrom() == null || msg.getType() == null
					|| msg.getBody() == null) {
				throw new JsonSyntaxException("Null in message.");
			}
			switch (msg.getType()) {
			case SESSION, REQUEST, REPAIR -> {
				if (msg.getFrom().equals(socket.getFrom())) throw new LoopbackException();
			}
			}
			ReliableMulticastSocket.logger.info("Received "+msg.getType().name()+".");
			return msg;
		}
		catch (IOException e) {
			ReliableMulticastSocket.logger.log(Level.WARNING, "Socket cannot receive.", e);
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
	private void dispatch(Message msg)
	{
		switch (msg.getType())
		{
		// 1. Update states
		// 2. Cancel a request in pool if there is
		// 3. If any loss detected, submit REQUEST via pool
		// 4. Put cache if DATA payload was never received
		case DATA -> {
			Long oldSeq = socket.states.update(msg.getFrom(), msg.getSeq(), null);
			String whose_seq = msg.getFrom()+"-"+msg.getSeq();
			if (oldSeq != null) {
				if (socket.pool.requests.containsKey(whose_seq)) {
					socket.pool.cancelRequest(whose_seq);
				}
				else if (msg.getSeq() <= oldSeq) return;
				for (long i = oldSeq + 1; i < msg.getSeq(); i++) {
					socket.pool.request(msg.getFrom()+"-"+i);
				}
			}
			socket.cache.put(whose_seq, msg.getBody());
		}

		// 1. Estimate one-way distances to other active sources
		// 2. Compare view with states and update states
		// 3. If any loss detected, submit REQUEST via pool
		case SESSION -> {
			long dist;   // t34
			Map<String, Long[]> view;
			try {
				Message.SessionBody body = ReliableMulticastSocket.gson.fromJson(
						new String(msg.getBody()), Message.SessionBody.class);
				if (body != null && body.t != null && body.view != null) {
					dist = ChronoUnit.MILLIS.between(LocalTime.parse(body.t), LocalTime.now());
					view = body.view;
				}
				else return;
			}
			catch (JsonSyntaxException e) { return; }
			for (var v : view.entrySet())
			{
				String from = v.getKey();
				if (from != null && v.getValue() != null) {
					Long seq = v.getValue()[0];
					if (seq != null) {
						Long _dist = null;   // t12
						if (from.equals(socket.getFrom())) _dist = v.getValue()[1];
						if (_dist != null) _dist = (_dist + dist) / 2;
						Long oldSeq = socket.states.update(from, seq, _dist);
						if (oldSeq != null) {
							for (long i = oldSeq + 1; i <= seq; i++) {
								socket.pool.request(v.getKey()+"-"+i);
							}
						}
					}
				}
			}
			// New t12 at the first time receiving x's SESSION.
			// - Case 1: never heard about x from others, i.e. no states, then inserts new;
			// - Case 2: heard from others, or received its DATA, i.e. seq already set up, then sets distance only.
			// Either can be handled by StateTable::update.
			socket.states.update(msg.getFrom(), 0, dist);
		}

		// 1. If repair in pool, stop and do nothing
		// 2. Attempt to postpone a request in pool if there is; and update dup count and closest distance
		// 3. Otherwise, if DATA payload found in cache, submit REPAIR via pool
		case REQUEST -> {
			String whose_seq;
			Long distToSrc;
			try {
				Message.RequestBody body = ReliableMulticastSocket.gson.fromJson(
						new String(msg.getBody()), Message.RequestBody.class);
				if (body != null && body.whose_seq != null) {
					whose_seq = body.whose_seq;
					distToSrc = body.distToSrc;
				}
				else return;
			}
			catch (JsonSyntaxException e) { return; }

			if (!socket.pool.repairs.containsKey(whose_seq))
			{
				if (socket.pool.requests.containsKey(whose_seq)) {
					socket.pool.postponeRequest(whose_seq);
					AbstractMap.SimpleEntry<RequestRepairPool.RequestTask, Future<?>> pair =
							socket.pool.requests.get(whose_seq);
					if (pair != null) {
						pair.getKey().req_dup ++;   // Tell duplicates
						if (distToSrc != null) {    // Update closest distance
							pair.getKey().min_dist = Math.min(pair.getKey().min_dist, distToSrc);
						}
					}
				}
				else socket.pool.repair(whose_seq);
			}
		}

		// 1. Cancel a request in pool if there is, then put cache
		// 2. Cancel a repair in pool if there is
		case REPAIR -> {
			String whose_seq;
			byte[] payload;
			try {
				Message.RepairBody body = ReliableMulticastSocket.gson.fromJson(
						new String(msg.getBody()), Message.RepairBody.class);
				if (body != null && body.whose_seq != null && body.payload != null) {
					whose_seq = body.whose_seq;
					payload = body.payload;
				} else return;
			}
			catch (JsonSyntaxException e) { return; }

			if (socket.pool.requests.containsKey(whose_seq)) {
				socket.cache.put(whose_seq, payload);
			}
			socket.pool.cancelRequest(whose_seq);
			socket.pool.cancelRepair(whose_seq);
		}
		}
	}

}
