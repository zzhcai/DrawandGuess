package srm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A container of request/repair timers.
 */
public class RequestRepairPool
{
	private final ReliableMulticastSocket socket;

	/** A size-adaptive thread pool */
	private final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

	protected final Map<String, Runnable> requestTasks = new HashMap<>();
	protected final Map<String, Runnable> repairTasks = new HashMap<>();

	public RequestRepairPool(ReliableMulticastSocket socket) {
		this.socket = socket;
	}

	/**
	 * Schedule a request timer.
	 *
	 * @param whose_seq what data to request
	 */
	protected void request(String whose_seq)
	{
		Message request = new Message(socket.sequencer, socket.getFrom(), Type.REQUEST, whose_seq.getBytes());
		byte[] out = ReliableMulticastSocket.gson.toJson(request).getBytes();
		DatagramPacket p = new DatagramPacket(out, out.length, socket.getGroup(), socket.getLocalPort());
		// Timer
		Runnable task = () -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				socket._send(p);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		};
		requestTasks.put(whose_seq, task);
		pool.execute(task);
	}

	/**
	 * Schedule a repair timer.
	 *
	 * @param whose_seq what data to repair
	 */
	protected void repair(String whose_seq)
	{
		Message repair = new Message(socket.sequencer, socket.getFrom(), Type.REPAIR,
				ReliableMulticastSocket.gson.toJson(new Message.RepairBody(
						whose_seq, socket.cache.get(whose_seq).getKey())).getBytes());
		byte[] out = ReliableMulticastSocket.gson.toJson(repair).getBytes();
		DatagramPacket p = new DatagramPacket(out, out.length, socket.getGroup(), socket.getLocalPort());
		// Timer
		Runnable task = () -> {
			try {
				Thread.sleep(5000);
				socket._send(p);
			}
			catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		};
		repairTasks.put(whose_seq, task);
		pool.execute(task);
	}

	/**
	 * Cancel a request timer if it is present.
	 */
	protected void cancelRequest(String whose_seq) {
		Runnable task = requestTasks.get(whose_seq);
		if (task != null && pool.remove(task)) {
			requestTasks.remove(whose_seq);
			ReliableMulticastSocket.logger.info("Request timer "+whose_seq+" is cancelled.");
		}
	}

	/**
	 * Cancel a repair timer if it is present.
	 */
	protected void cancelRepair(String whose_seq) {
		Runnable task = repairTasks.get(whose_seq);
		if (task != null && pool.remove(task)) {
			repairTasks.remove(whose_seq);
			ReliableMulticastSocket.logger.info("Repair timer "+whose_seq+" is cancelled.");
		}
	}

	/**
	 * Initiates an orderly shutdown in which previously submitted tasks are executed,
	 * but no new tasks will be accepted.
	 */
	protected void close() {
		pool.shutdown();
	}

}
