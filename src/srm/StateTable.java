package srm;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks the highest sequence number and arrival time,
 * received from each active source (including socket itself).
 * There is a periodic removal of those deprecated states
 * whose sequence number hasn't changed for a while.
 */
public class StateTable extends ConcurrentHashMap<String,
		AbstractMap.SimpleEntry<Long, LocalTime>>
{
	/** How long a state is kept, in minutes */
	private long ttl;
	/** How recent the currently-viewing page filters states by, in minutes */
	private long tView;
	private final Timer updater = new Timer();

	public StateTable(long ttl, long tView) {
		this.ttl = ttl;
		this.tView = tView;
		updater.schedule(new TimerTask() {
			@Override
			public void run() {
				ReliableMulticastSocket.logger.info("Removing deprecated from states.");
				forEach((k, v) -> {
					if (ChronoUnit.MINUTES.between(v.getValue(), LocalTime.now()) > ttl) {
						remove(k);
					}
				});
			}
		}, ttl * 60000, ttl * 60000);
	}

	/**
	 * Returns the currently-viewing page on states.
	 */
	protected Map<String, Long> getViewingPage() {
		return entrySet().stream()
				.filter(c -> ChronoUnit.MINUTES.between(
						c.getValue().getValue(), LocalTime.now()) <= tView)
				.collect(Collectors.toMap(
						Map.Entry::getKey, c -> c.getValue().getKey()));
	}

	/**
	 * Thread-safe, put if the given sequence number is greater.
	 */
	protected void putIfGreater(String k, AbstractMap.SimpleEntry<Long, LocalTime> v) {
		AbstractMap.SimpleEntry<Long, LocalTime> curr;
		while (true) {
			curr = get(k);
			if (curr == null) {
				if (putIfAbsent(k, v) == null) return;
			}
			else if (v.getKey() <= curr.getKey() || replace(k, curr, v)) return;
		}
	}

	protected Timer getUpdater() {
		return updater;
	}

}
