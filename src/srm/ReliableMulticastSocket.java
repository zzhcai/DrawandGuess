package srm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;
import java.util.logging.Formatter;
import java.util.stream.Collectors;

/**
 * A reliable multicast socket class, made transparent,
 * implemented based on the Scalable Reliable Multicast (SRM) Framework.
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

    /** The highest sequence number and its arriving time,
     *  received from each active source (including self).
     *  There is a periodic removal of those deprecated states
     *  whose sequence number hasn't changed for a while. */
    private final Map<String,
            AbstractMap.SimpleEntry<Long, LocalTime>> states =
            new ConcurrentHashMap<>();
    /** How often to update states, in minutes */
    protected static final long STATE_TTL = 5;
    /** How recent the currently-viewing page filters states by, in minutes */
    protected static final long VIEW_TTL = 1;

    /** The aggregate bandwidth in bytes (regardless of headers' overhead),
     *  since from the last session message. */
    private final AtomicInteger aggregBW = new AtomicInteger(0);
    /** The session bandwidth. */
    private final AtomicInteger sessionBW = new AtomicInteger(0);

    /** The dynamic rate of sending SESSION messages, in seconds, that
     *  the bandwidth consumed is adaptive to 5% of the aggregate bandwidth. */
    private long sessionRate = SESSION_RATE_MIN;
    protected static final long SESSION_RATE_MAX = VIEW_TTL * 30;   // half
    protected static final long SESSION_RATE_MIN = 10;

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
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurs in logger.", e);
        }

        // Routines
        new Timer().schedule(new StatesUpdatingTask(), STATE_TTL * 60000 * 2, STATE_TTL * 60000);
        new Timer().schedule(new SessionSendingTask(this), sessionRate * 1000L);
        logger.info("Reliable multicast socket starting.");
    }

    /**
     * Task to remove deprecated states.
     */
    private class StatesUpdatingTask extends TimerTask {
        @Override
        public void run() {
            logger.info("Ejecting states.");
            states.forEach((k, v) -> {
                if (ChronoUnit.MINUTES.between(v.getValue(), LocalTime.now()) > STATE_TTL) {
                    states.remove(k, v);
                }
            });
        }
    }

    /**
     * Task to mutlicast SESSION messages
     */
    private class SessionSendingTask extends TimerTask
    {
        ReliableMulticastSocket socket;
        /**
         * @param socket the socket used for multicast
         */
        public SessionSendingTask(ReliableMulticastSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run()
        {
            if (group != null) {
                Message session = new Message(sequencer, getLocalPort(), Type.SESSION,
                        gson.toJson(getViewingPage()).getBytes());
                byte[] out = gson.toJson(session).getBytes();
                DatagramPacket p = new DatagramPacket(out, out.length, group, getLocalPort());
                try {
                    logger.info("Multicasting SESSION.");
                    _send(p);
                    sessionBW.addAndGet(p.getLength());   // inc
                }
                catch (IOException e) {
                    logger.log(Level.SEVERE, "Multicasting SESSION failed.", e);
                }
                updateSessionRate();
            }
            // Schedule next
            new Timer().schedule(new SessionSendingTask(socket), sessionRate * 1000L);
        }
    }

    /**
     * Returns the currently-viewing page on states.
     */
    private Map<String, Long> getViewingPage() {
        return states.entrySet().stream()
                .filter(c -> ChronoUnit.MINUTES.between(c.getValue().getValue(),
                        LocalTime.now()) <= VIEW_TTL)
                .collect(Collectors.toMap(Map.Entry::getKey, c -> c.getValue().getKey()));
    }

    /**
     * Reset bandwidth counters, then adjust the session rate.
     */
    public void updateSessionRate() {
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

    /**
     * Multicast a datagram packet unreliably,
     * and measures bandwidth cost at the same time.
     */
    private void _send(DatagramPacket p) throws IOException {
        super.send(p);
        aggregBW.addAndGet(p.getLength());
    }

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

    @Override
    public void receive(DatagramPacket p) throws IOException
    {
        int length = p.getLength();
        while (true)
        {
            // Intercept packets
            DatagramPacket _p = new DatagramPacket(new byte[length], length);
            super.receive(_p);
            Message msg = gson.fromJson(
                    new String(_p.getData(), 0, _p.getLength()), Message.class);

            if (msg == null ||
                    msg.getFrom() == null || msg.getPayload() == null) {
                continue;
            }
            switch (msg.getType()) {
            case DATA -> {
                logger.info("Received DATA.");
                putIfGreater(msg.getFrom(),
                        new AbstractMap.SimpleEntry<>(msg.getSeq(), LocalTime.now()));
                p.setData(msg.getPayload());
                return;
            }
            case SESSION, REQUEST, REPAIR -> {
                // TODO
            }
            }
        }
    }

    /**
     * Thread-safe,
     * put into states if the given sequence number is greater.
     *
     * @return true if the value is put
     */
    private boolean putIfGreater(String k, AbstractMap.SimpleEntry<Long, LocalTime> v)
    {
        AbstractMap.SimpleEntry<Long, LocalTime> curr = states.putIfAbsent(k, v);
        if (curr == null) return true;
        while (true) {
            if (v.getKey() <= curr.getKey()) return false;
            if (states.replace(k, curr, v))  return true;
            curr = states.get(k);
        }
    }
    
}
