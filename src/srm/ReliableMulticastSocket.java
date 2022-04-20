package srm;

import com.google.gson.*;

import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

/**
 * A reliable multicast socket class, made transparent,
 * implemented based on the Scalable Reliable Multicast (SRM) Framework.
 */
public class ReliableMulticastSocket extends MulticastSocket
{
    protected static final String LOG_PATH = "./multicast.log";
    protected static final Logger logger = Logger.getLogger(ReliableMulticastSocket.class.getName());
    protected static Gson gson = new GsonBuilder().serializeNulls().create();   // JSON converter

    /** DATA packet sequencer */
    private long sequencer = 0;

    /** The highest sequence number and its arriving time, received from
     *  each active source (including self) for the current-viewing page.
     *  The page has a periodic removal of those deprecated states
     *  whose sequence number hasn't changed for a while. */
    private final Map<String,
            AbstractMap.SimpleEntry<Long, LocalTime>> states =
            new ConcurrentHashMap<>();
    /** How often to update page view in minutes */
    protected static final int VIEW_TTL = 1;

    /** How often to send session message in minutes */
    protected static double sessionRate = 0.5;
    protected static final double sessionRateMax = 5;
    protected static final double sessionRateMin = 0.1;

    /** Record the number of messages over a period of time */
    protected static int messageCount = 0;

    /** Record the time the session message is sent */
    protected static LocalTime sessionTime = null;

    /**
     * Constructs a multicast socket and
     * binds it to the specified port on the local host machine.
     */
    public ReliableMulticastSocket(int port) throws IOException {
        super(port);
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

        // Update page view
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                states.forEach((k, v) -> {
                    if (ChronoUnit.MINUTES.between(v.getValue(), LocalTime.now()) > VIEW_TTL) {
                        states.remove(k, v);
                    }
                });
            }
        }, VIEW_TTL * 120000, VIEW_TTL * 60000);
    }

    @Override
    public void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        super.joinGroup(mcastaddr, netIf);
    }

    @Override
    public void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        super.leaveGroup(mcastaddr, netIf);
    }

    @Override
    public synchronized void send(DatagramPacket p) throws IOException
    {
        Message data = new Message(sequencer, getLocalPort(), Type.DATA, p);
        byte[] out = gson.toJson(data).getBytes();
        DatagramPacket _p = new DatagramPacket(out, out.length,
                p.getAddress(), p.getPort());
        super.send(_p);
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
        while (true) {
            DatagramPacket _p = new DatagramPacket(new byte[length], length);
            super.receive(_p);
            Message msg = gson.fromJson(
                    new String(_p.getData(), 0, _p.getLength()), Message.class);
            // Block until receives DATA
            if (msg.getType() == Type.DATA) {
                putIfGreater(msg.getFrom(),
                        new AbstractMap.SimpleEntry<>(msg.getSeq(), LocalTime.now()));
                p.setData(msg.getPayload());
                return;
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

    private synchronized void countMsg(LocalTime msgTime){
        if (sessionTime == null) messageCount += 1;
        else if (ChronoUnit.MINUTES.between(msgTime, sessionTime) < sessionRate) messageCount += 1;
    }

    private void updateSessionRate(){
        double sessionRateTemp = sessionRate;
        if (messageCount != 0) sessionRateTemp = 19 * sessionRate / messageCount;
        if (sessionRateTemp > sessionRateMax || sessionRateTemp < sessionRateMin) sessionRateTemp = sessionRate;
        sessionRate = sessionRateTemp;
        messageCount = 0;
        sessionTime = LocalTime.now();
    }
}
