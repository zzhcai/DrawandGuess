package srm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A container of request/repair back-off timers.
 */
public class RequestRepairPool
{
	private final ReliableMulticastSocket socket;

	/** A size-adaptive thread pool */
	private final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

	protected final Map<String, SimpleEntry<RequestTask, Future<?>>> requests = new HashMap<>();
	protected final Map<String, SimpleEntry<RepairTask, Future<?>>> repairs = new HashMap<>();

	public RequestRepairPool(ReliableMulticastSocket socket) {
		this.socket = socket;
	}

	// TODO: adaptive
	private double C1 = 2;
	private double C2 = 2;
	private double D1, D2;

	private class RequestTask implements Runnable
	{
		/** Turned on before stopping the thread via interrupt */
		boolean doneFlag = false;
		boolean startedFlag = false;
		LocalTime start;
		long expire;   // in milliseconds
		long i = 0;
		final DatagramPacket p;
		final String whose;

		public RequestTask(DatagramPacket p, String whose) {
			this.p = p;
			this.whose = whose;
		}

		@Override
		public void run()
		{
			start = LocalTime.now();
			startedFlag = true;
			while (true) {
				try {
					StateTable.State s = socket.states.get(whose);
					if (s != null) expire = (long) (Math.pow(2, i++) * (C1 + Math.random() * C2) * s.dist());
					else expire = 1000;
					//noinspection BusyWait
					Thread.sleep(expire);
					socket._send(p);
					ReliableMulticastSocket.logger.info("Multicasting REQUEST.");
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				catch (InterruptedException e) {
					if (doneFlag) break;
				}
			}
		}
	}

	private class RepairTask implements Runnable
	{
		static boolean firstRepair = true;
		boolean startedFlag = false;
		final DatagramPacket p;
		final String whose;

		public RepairTask(DatagramPacket p, String whose){
			this.p = p;
			this.whose = whose;
		}

		@Override
		public void run()
		{
			startedFlag = true;
			long expire;   // in milliseconds
			try {
				StateTable.State s = socket.states.get(whose);
				if (s != null) expire = (long) ((D1 + Math.random() * D2) * s.dist());
				else expire = 1000;
				Thread.sleep(expire);
				socket._send(p);
				ReliableMulticastSocket.logger.info("Multicasting REPAIR.");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (InterruptedException ignored) {
			}
		}
	}

	/**
	 * Schedule a request timer.
	 */
	protected void request(String whose_seq)
	{
		String whose = whose_seq.split("-")[0];
		Message request = new Message(socket.sequencer, socket.getFrom(), Type.REQUEST, whose_seq.getBytes());
		byte[] out = ReliableMulticastSocket.gson.toJson(request).getBytes();
		DatagramPacket p = new DatagramPacket(out, out.length, socket.getGroup(), socket.getLocalPort());

		RequestTask task = new RequestTask(p, whose);
		Future<?> f = pool.submit(task);
		requests.put(whose_seq, new SimpleEntry<>(task, f));
		ReliableMulticastSocket.logger.info("Request timer <"+whose_seq+"> is up.");
	}

	/**
	 * Schedule a repair timer.
	 */
	protected void repair(String whose_seq)
	{
		// Initialize D1, D2 with group size G
		if (RepairTask.firstRepair) {
			RepairTask.firstRepair = false;
			D1 = Math.log(socket.states.getViewingPage().size());
			D2 = D1;
		}

		String whose = whose_seq.split("-")[0];
		Message repair = new Message(socket.sequencer, socket.getFrom(), Type.REPAIR,
				ReliableMulticastSocket.gson.toJson(new Message.RepairBody(
						whose_seq, socket.cache.get(whose_seq).getKey())).getBytes());
		byte[] out = ReliableMulticastSocket.gson.toJson(repair).getBytes();
		DatagramPacket p = new DatagramPacket(out, out.length, socket.getGroup(), socket.getLocalPort());

		RepairTask task = new RepairTask(p, whose);
		Future<?> f = pool.submit(task);
		repairs.put(whose_seq, new SimpleEntry<>(task, f));
		ReliableMulticastSocket.logger.info("Repair timer <"+whose_seq+"> is up.");
	}

	/**
	 * Postpone a request timer if it is present.
	 */
	protected void postponeRequest(String whose_seq)
	{
		RequestTask task = requests.get(whose_seq).getKey();
		Future<?> f = requests.get(whose_seq).getValue();
		// Do not postpone for requests that belong to the same iteration of loss recovery,
		// where we set this ignore-backoff time to halfway task expiration time.
		if (task != null && f != null && task.startedFlag &&
				ChronoUnit.MILLIS.between(task.start, LocalTime.now()) > task.expire / 2) {
			f.cancel(true);   // with done set false
			ReliableMulticastSocket.logger.info("Request timer <"+whose_seq+"> is postponed.");
		}
	}

	/**
	 * Cancel a request timer if it is present.
	 */
	protected void cancelRequest(String whose_seq)
	{
		RequestTask task = requests.get(whose_seq).getKey();
		Future<?> f = requests.get(whose_seq).getValue();
		if (task != null && f != null) {
			task.doneFlag = true;
			while (!task.startedFlag) Thread.onSpinWait();
			f.cancel(true);
			requests.remove(whose_seq);
			ReliableMulticastSocket.logger.info("Request timer <"+whose_seq+"> is cancelled.");
		}
	}

	/**
	 * Cancel a repair timer if it is present.
	 */
	protected void cancelRepair(String whose_seq)
	{
		RepairTask task = repairs.get(whose_seq).getKey();
		Future<?> f = repairs.get(whose_seq).getValue();
		if (task != null && f != null) {
			while (!task.startedFlag) Thread.onSpinWait();
			f.cancel(true);
			repairs.remove(whose_seq);
			ReliableMulticastSocket.logger.info("Repair timer <"+whose_seq+"> is cancelled.");
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
