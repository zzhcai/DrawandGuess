package srm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.Random;

/**
 * A container of request/repair timers.
 */
public class RequestRepairPool
{
	private final ReliableMulticastSocket socket;
	/** A size-adaptive thread pool */
	private final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

	protected final Map<String, requestTask> requestTasks = new HashMap<>();
	protected final Map<String, repairTask> repairTasks = new HashMap<>();
	protected final Map<String, Future<?>> requestFutures = new HashMap<>();
	protected final Map<String, Future<?>> repairFutures = new HashMap<>();

	public RequestRepairPool(ReliableMulticastSocket socket) {
		this.socket = socket;
	}

	protected int C1 = 2;
	protected int C2 = 2;
	protected int D1 = 0;
	protected int D2 = 0;

	class requestTask implements Runnable {
        private final Random random = new Random();
        private int delay = random.nextInt(C1) + C2;
        private DatagramPacket p;
        private boolean doneFlag = false;

        public requestTask(DatagramPacket p){
            this.p = p;
        }

        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while(true) {
                try {
                    Thread.sleep(delay);
                    delay *= 2;
                } catch (InterruptedException e) {
					e.printStackTrace();
                    if(doneFlag) Thread.currentThread().interrupt();
                    else delay *= 2;
                }
                try {
                    socket._send(p);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void setDoneFlag(boolean doneFlag) {
            this.doneFlag = doneFlag;
        }
    }

    class repairTask implements Runnable {
        private final Random random = new Random();
        private int delay = random.nextInt(D1) + D2;
        private DatagramPacket p;

        public repairTask(DatagramPacket p){
            this.p = p;
        }

        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while(true){
				try {
					Thread.sleep(delay);
					delay *= 2;
					socket._send(p);
				}
				catch (InterruptedException | IOException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
            }
        }
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
        requestTask task = new requestTask(p);
        requestTasks.put(whose_seq, task);
		Future<?> future = pool.submit(task);
        requestFutures.put(whose_seq, future);
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

		D1 = (int)StrictMath.log(socket.states.getViewingPage().size());
		D2 = (int)StrictMath.log(socket.states.getViewingPage().size());
		repairTask task = new repairTask(p);

		repairTasks.put(whose_seq, task);
		Future<?> future = pool.submit(task);
		repairFutures.put(whose_seq, future);
	}

	/**
	 * Cancel a request timer if it is present.
	 */
	protected void cancelRequest(String whose_seq) {
		Future<?> future = requestFutures.get(whose_seq);
		requestTask task = requestTasks.get(whose_seq);
		if (future != null && task != null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			future.cancel(true);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			requestFutures.remove(whose_seq);
			requestTasks.remove(whose_seq);
			ReliableMulticastSocket.logger.info("Request timer "+whose_seq+" is cancelled.");
		}
	}

	/**
	 * Cancel a repair timer if it is present.
	 */
	protected void cancelRepair(String whose_seq) {
		repairTask task = repairTasks.get(whose_seq);
		Future<?> future = repairFutures.get(whose_seq);
		if (task != null && future != null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			future.cancel(true);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			repairFutures.remove(whose_seq);
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
