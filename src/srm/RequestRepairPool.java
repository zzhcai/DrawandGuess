package srm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class RequestRepairPool
{
	private final ExecutorService pool = Executors.newCachedThreadPool();

	protected synchronized void request(String whose, long seq) {
	}

}
