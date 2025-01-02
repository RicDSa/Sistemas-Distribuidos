package ds.assign.tom;

import java.io.Serializable;


public class Message  {
    private static final long serialVersionUID = 1L;

    private String content; // The content of the message
    private int timestamp; // The Lamport timestamp of the message
    private String senderId; // The ID of the sender peer
    private String senderIp; // The IP address of the sender peer

    /**
     * Constructs a Message instance with the given content, timestamp, and sender ID.
     *
     * @param content the content of the message
     * @param timestamp the Lamport timestamp of the message
     * @param senderId the ID of the sender peer
     */
    public Message(String content, int timestamp, String senderId, String senderIp) {
        this.content = content;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.senderIp = senderIp;
    }

    /**
     * Returns the content of the message.
     *
     * @return the content of the message
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the Lamport timestamp of the message.
     *
     * @return the timestamp of the message
     */
    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the ID of the sender peer.
     *
     * @return the sender ID
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Returns the IP of the sender peer.
     *
     * @return the sender IP
     */
    public String getSenderIp() {
        return senderIp;
    }

    /**
     * Returns a string representation of the message.
     *
     * @return a string representation of the message
     */
    @Override
    public String toString() {
        return "Message{" +
                "content='" + this.content + '\'' +
                ", timestamp=" + this.timestamp +
                ", senderId='" + this.senderId + '\'' +
                '}';
    }
}

