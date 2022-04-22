package srm;

import com.google.gson.JsonSyntaxException;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * A cache of recent DATA/REPAIR messages, keeping arrival time,
 * with a periodic removal of those considered deprecated.
 * Also contains a queue of unconsumed datagram payload for
 * the method ReliableMulticastSocket::receive to fetch from.
 */
public class DataCache extends ConcurrentHashMap<Message, LocalTime>
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
	@SuppressWarnings("unchecked")
	protected void put(Message msg)
	{
		byte[] payload;
		switch (msg.getType()) {
		case DATA -> payload = msg.getBody();
		case REPAIR -> {
			try {
				AbstractMap.SimpleEntry<String, byte[]> pair = (AbstractMap.SimpleEntry<String, byte[]>)
						ReliableMulticastSocket.gson.fromJson(new String(msg.getBody()), Map.class);
				if (pair != null && pair.getValue() != null) payload = pair.getValue();   // valid?
				else return;
			}
			catch (JsonSyntaxException e) { return; }
		}
		default -> {
			return;
		}
		}
		try {
			unconsumed.put(payload);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.put(msg, LocalTime.now());
	}

	/**
	 * Consume one queued datagram payload.
	 */
	protected byte[] consume() throws InterruptedException {
		ReliableMulticastSocket.logger.info("Consuming from cache.");
		return unconsumed.take();
	}

	protected Timer getUpdater() {
		return updater;
	}

}
