package srm;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * A cache of recent DATA/REPAIR payload to feed REPAIR, from each data source.
 * Keeps the receiving or last repairing time,
 * with a periodic removal of those considered deprecated.
 * Also contains a queue of unconsumed datagram payload for
 * the method ReliableMulticastSocket::receive to fetch from.
 */
public class DataCache extends ConcurrentHashMap<String, SimpleEntry<byte[], LocalTime>>
{
	/** How long a message is kept, in minutes */
	private final long ttl;
	private final Timer updater = new Timer();

	/** Queue to feed consumption */
	private final BlockingQueue<byte[]> unconsumed = new LinkedBlockingDeque<>();

	public DataCache(long ttl)
	{
		this.ttl = ttl;
		updater.schedule(new TimerTask() {
			@Override
			public void run() {
				ReliableMulticastSocket.logger.info("Removing deprecated from cache.");
				forEach((k, v) -> {
					if (ChronoUnit.MINUTES.between(v.getValue(), LocalTime.now()) > getTtl()) {
						remove(k);
					}
				});
			}
		}, ttl * 60000, ttl * 60000);
	}

	/**
	 * Queue and cache a DATA/REPAIR payload.
	 */
	protected void put(String whose_seq, byte[] payload) {
		try {
			unconsumed.put(payload);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.put(whose_seq, new SimpleEntry<>(payload, LocalTime.now()));
	}

	/**
	 * Consume one queued datagram payload.
	 */
	protected byte[] consume() throws InterruptedException {
		ReliableMulticastSocket.logger.info("Consuming from cache.");
		return unconsumed.take();
	}

	protected long getTtl() {
		return ttl;
	}

	protected Timer getUpdater() {
		return updater;
	}

}
