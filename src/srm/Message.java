package srm;

/**
 * A wrapper class of a single datagram packet, which
 * specifies a type on which the packet can be handled accordingly.
 *
 * @param seq The sequence number of message
 * @param from Whom the datagram is being sent from
 * @param type Either one of those in enum class Type
 * @param body Body of the message:
 *  - DATA: byte(payload)
 *  - SESSION: byte(toJson([localTime, StateTable::getViewingPage]))
 *  - REQUEST: byte(${whose}-${seq})
 *  - REPAIR: byte(toJson([${whose}-${seq}, payload]))
 */
public record Message(long seq, String from, Type type, byte[] body) {
}
