package srm;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

/**
 * A wrapper class of a single datagram packet, which
 * specifies a type on which the packet can be handled accordingly.
 */
public class Message
{
    /** The sequence number of message */
    private final long seq;
    /** From which the datagram is being sent,
     *  ${IpAddress}@${Port}@${Pid} */
    private final String from;
    /** Either one of those in enum class Type */
    private final Type type;
    /** The payload of the datagram packet */
    private final byte[] payload;

    /**
     * @param port the port number to which multicast socket is connected
     */
    public Message(long seq, int port, Type type, DatagramPacket packet)
    {
        this.seq = seq;
        String address = null;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {
            ReliableMulticastSocket.logger.log(Level.WARNING,
                    "Cannot retrieve the address of the local host.", e);
        }
        this.from = (address != null ? address+"@" : "") + port + "@" + ProcessHandle.current().pid();
        this.type = type;
        this.payload = packet.getData();
    }

    public long getSeq() {
        return seq;
    }

    public String getFrom() {
        return from;
    }

    public Type getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

}
