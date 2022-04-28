package srm;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks states received from each active source (including self).
 */
public class StateTable extends ConcurrentHashMap<String, StateTable.State>
{
	/**
	 * @param seq the highest sequence number
	 * @param t when seq was incremented
	 * @param dist estimated one-way distance, in milliseconds;
	 *  		   null if distance is unknown, in particular for self state.
	 *  		   Note that this estimate does not assume synchronized clocks,
	 * 			   while it does assume that paths are roughly symmetric.
	 */
	protected record State(long seq, Long dist, LocalTime t) {
	}

	/** How recent the currently-viewing page filters states by, in minutes */
	private final long tView;

	public StateTable(long tView) {
		this.tView = tView;
	}

	/**
	 * Returns the currently-viewing page on states.
	 *
	 * @return {from: [seq, dist]}
	 */
	protected Map<String, Long[]> getViewingPage() {
		return entrySet().stream()
				.filter(c -> ChronoUnit.MINUTES.between(
						c.getValue().t, LocalTime.now()) <= tView)
				.collect(Collectors.toMap(Map.Entry::getKey,
						c -> new Long[]{c.getValue().seq, c.getValue().dist}));
	}

	/**
	 * Thread-safe, update the state of one active source.
	 *
	 * @param seq update if either absent or greater
	 * @param dist new one-way distance, in milliseconds; won't update if null
	 * @return old seq associated with from; null if absent
	 */
	public Long update(String from, long seq, Long dist)
	{
		LocalTime now = LocalTime.now();
		State old = putIfAbsent(from, new State(seq, dist, now));
		if (old == null) return null;
		if (dist == null) dist = old.dist;
		while (true) {
			State curr = new State(Math.max(seq, old.seq), dist, now);
			if (replace(from, old, curr)) return old.seq;
			old = get(from);
		}
	}

}
