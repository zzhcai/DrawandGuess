package srm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
public class RequestRepairPool
{
	private final ThreadPoolExecutor pool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
	private final ReliableMulticastSocket socket;
	protected ConcurrentHashMap<String, Runnable> requestTasks = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Runnable> repairTasks = new ConcurrentHashMap<>();
	private static Gson gson = new GsonBuilder().serializeNulls().create();

	public RequestRepairPool(ReliableMulticastSocket socket) {
		this.socket = socket;
	}

	protected synchronized void request(String whose, long seq) {
		String taskID = whose + '-' + seq;
		Runnable task = new Runnable() {
			@Override
			public void run() {
				while (socket.group == null) Thread.onSpinWait();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Message request = new Message(socket.sequencer, socket.getFrom(), Type.REQUEST, taskID.getBytes());
				byte[] out = gson.toJson(request).getBytes();
				DatagramPacket p = new DatagramPacket(out, out.length, socket.group, socket.getLocalPort());
				try {
					socket._send(p);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		requestTasks.put(taskID, task);
		pool.execute(task);
	}

	protected synchronized void repair(String whose, long seq, byte[] body) {
		String taskID = whose + '-' + seq;
		Runnable task = new Runnable() {
			@Override
			public void run() {
				while (socket.group == null) Thread.onSpinWait();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Message repair = new Message(socket.sequencer, socket.getFrom(), Type.REPAIR,
						gson.toJson(new Message.RepairBody(taskID, body)).getBytes());
				byte[] out = gson.toJson(repair).getBytes();
				DatagramPacket p = new DatagramPacket(out, out.length, socket.group, socket.getLocalPort());
				try {
					socket._send(p);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		repairTasks.put(taskID, task);
		pool.execute(task);
	}

	protected synchronized void cancelRequest(String whose, long seq) {
		String taskID = whose + '-' + seq;
		if(requestTasks.containsKey(taskID)) {
			Runnable task = requestTasks.get(taskID);
			pool.remove(task);
			requestTasks.remove(taskID);
		}
	}

	protected synchronized void cancelRepair(String whose, long seq) {
		String taskID = whose + '-' + seq;
		if(repairTasks.containsKey(taskID)) {
			Runnable task = repairTasks.get(taskID);
			pool.remove(task);
			repairTasks.remove(taskID);
		}
	}

	protected void close() {
		pool.shutdown();
	}
}
