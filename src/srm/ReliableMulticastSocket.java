package srm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

/**
 * A reliable multicast socket class, made transparent.
 *
 * Implements the Scalable Reliable Multicast (SRM) Framework in the paper:
 * Floyd, S., Jacobson, V., Liu, C.-G., McCanne, S., & Zhang, L. (1997).
 * A reliable multicast framework for light-weight sessions and application level framing.
 * IEEE/ACM Transactions on Networking, 5(6), 784&ndash;803.
 *
 * @author Team Snorlax:
 * Bingzhe Jin, Kaixun Yang, Shizhan Xu, Zhen Cai
 */
public class ReliableMulticastSocket extends MulticastSocket
{
    protected static final String LOG_PATH = "./multicast.log";
    protected static final Logger logger = Logger.getLogger(ReliableMulticastSocket.class.getName());
    protected static Gson gson = new GsonBuilder().serializeNulls().create();   // JSON converter

    /** Group IP address */
    private InetAddress group = null;

    /** DATA packet sequencer */
    private long sequencer = 0;

    /**
     * Tracks the highest sequence number and arrival time,
     * received from each active source (including socket itself).
     * There is a periodic removal of those deprecated states
     * whose sequence number hasn't changed for a while.
     */
    protected final StateTable states = new StateTable(5, 1);
    /**
     * A cache of recent DATA/REPAIR messages, keeping arrival time,
     * with a periodic removal of those considered deprecated.
     * Also contains a queue of unconsumed datagram packets for
     * the method ReliableMulticastSocket::receive to fetch from.
     */
    protected final DataCache cache = new DataCache(5);

    /** The dynamic rate of sending SESSION messages, in seconds, that
     *  the bandwidth consumed is adaptive to 5% of the aggregate bandwidth. */
    private long sessionRate = SESSION_RATE_MIN;
    protected static final long SESSION_RATE_MAX = 30;
    protected static final long SESSION_RATE_MIN = 10;
    private final Timer sessionSender = new Timer();

    /** The aggregate bandwidth in bytes (regardless of headers' overhead),
     *  since from the last session message. */
    private final AtomicInteger aggregBW = new AtomicInteger(0);
    /** The session bandwidth. */
    private final AtomicInteger sessionBW = new AtomicInteger(0);

    /**
     * Constructs a multicast socket and
     * binds it to the specified port on the local host machine.
     */
    public ReliableMulticastSocket(int port) throws IOException {
        super(port);
        init();
    }

    /**
     * Constructs a multicast socket and
     * binds it to the specified local socket address.
     */
    public ReliableMulticastSocket(SocketAddress bindaddr) throws IOException {
        super(bindaddr);
        init();
    }

    private void init()
    {
        // Assigning handler to logger
        Handler handler;
        Formatter simpleFormatter;
        try {
            handler = new FileHandler(LOG_PATH);
            simpleFormatter = new SimpleFormatter();
            handler.setFormatter(simpleFormatter);
            logger.addHandler(handler);
            logger.info("Reliable multicast socket starting.");
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurs in logger.", e);
        }

        // Session sending routines
        sessionSender.schedule(new SessionSendTask(), sessionRate * 1000L);
    }

    /**
     * Task to multicast SESSION messages.
     */
    private class SessionSendTask extends TimerTask
    {
        @Override
        public void run()
        {
            if (group != null) {
                Message session = new Message(sequencer, getLocalPort(), Type.SESSION,
                        gson.toJson(states.getViewingPage()).getBytes());
                byte[] out = gson.toJson(session).getBytes();
                DatagramPacket p = new DatagramPacket(out, out.length, group, getLocalPort());
                try {
                    _send(p);
                    logger.info("Multicasting SESSION.");
                    sessionBW.addAndGet(p.getLength());   // inc
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                updateSessionRate();
            }
            // Schedule next
            new Timer().schedule(new SessionSendTask(), sessionRate * 1000L);
        }
    }

    /**
     * Reset bandwidth counters, then adjust the session rate.
     */
    private void updateSessionRate() {
        double ratio = ((double) sessionBW.getAndSet(0)) / aggregBW.getAndSet(0);
        long sessionRateTemp = (long) (20 * sessionRate / ratio);
        sessionRate = Math.min(Math.max(sessionRateTemp, SESSION_RATE_MIN), SESSION_RATE_MAX);
        logger.info("Session rate gets updated to per "+sessionRate+" seconds.");
    }

    @Override
    public synchronized void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        super.joinGroup(mcastaddr, netIf);
        group = ((InetSocketAddress) mcastaddr).getAddress();
    }

    @Override
    public void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        super.leaveGroup(mcastaddr, netIf);
    }

    // send DATA only
    @Override
    public synchronized void send(DatagramPacket p) throws IOException
    {
        Message data = new Message(sequencer, getLocalPort(), Type.DATA, p.getData());
        byte[] out = gson.toJson(data).getBytes();
        DatagramPacket _p = new DatagramPacket(out, out.length,
                p.getAddress(), p.getPort());
        logger.info("Multicasting DATA.");
        _send(_p);
        if (!getOption(StandardSocketOptions.IP_MULTICAST_LOOP)) {
            states.put(data.getFrom(),
                    new AbstractMap.SimpleEntry<>(sequencer, LocalTime.now()));
        }
        sequencer++;
    }

    // Receive DATA only
    @Override
    public void receive(DatagramPacket p) {
        try {
            cache.consume();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delegate multicasting a datagram packet unreliably, and
     * measures bandwidth cost at the same time.
     */
    protected void _send(DatagramPacket p) throws IOException {
        super.send(p);
        aggregBW.addAndGet(p.getLength());
    }

    /**
     * Delegate receiving a multicasted datagram packet unreliably, and
     * measures bandwidth cost at the same time.
     */
    protected void _receive(DatagramPacket p) throws IOException {
        super.receive(p);
        aggregBW.addAndGet(p.getLength());
    }

    @Override
    public void close() {
        super.close();
        sessionSender.cancel();
        // TODO: stop pool
        states.getUpdater().cancel();
        cache.getUpdater().cancel();
        sessionSender.purge();   // for garbage collection
        states.getUpdater().purge();
        cache.getUpdater().purge();
    }

    /** Disabled. */
    @Override
    public void connect(InetAddress address, int port) {
    }

    /** Disabled. */
    @Override
    public void setSoTimeout(int timeout) {
    }

}
