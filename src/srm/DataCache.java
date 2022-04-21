package srm;

import java.net.DatagramPacket;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * A cache of recent DATA/REPAIR messages, keeping arrival time,
 * with a periodic removal of those considered deprecated.
 * Also contains a queue of unconsumed datagram packets for
 * the method ReliableMulticastSocket::receive to fetch from.
 */
public class DataCache extends HashMap<Message, LocalTime>
{
	/** How long a message is kept, in minutes */
	private long ttl;
	private final Timer updater = new Timer();

	/** Queue to feed consumption */
	private final BlockingQueue<DatagramPacket> unconsumed = new LinkedBlockingDeque<>();

	public DataCache(long ttl) {
		this.ttl = ttl;
		updater.schedule(new TimerTask() {
			@Override
			public void run() {
				ReliableMulticastSocket.logger.info("Removing deprecated from cache.");
				forEach((k, v) -> {
					if (ChronoUnit.MINUTES.between(v, LocalTime.now()) > ttl) {
						remove(k);
					}
				});
			}
		}, ttl * 60000, ttl * 60000);
	}

	/**
	 * Cache a DATA/REPAIR message and queue its payload.
	 */
	public void put(Message msg)
	{
		super.put(msg, LocalTime.now());
		if (msg.getType() == Type.DATA || msg.getType() == Type.REPAIR) {
			try {
				unconsumed.put(new DatagramPacket(msg.getPayload(), msg.getPayload().length));
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/** Consume one queued datagram packets. */
	protected DatagramPacket consume() throws InterruptedException {
		return unconsumed.take();
	}

	public Timer getUpdater() {
		return updater;
	}

}
