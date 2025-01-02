package ds.assign.tom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This class represents a Peer in a distributed system.
 * Each peer has an ID, IP address, port, and a list of other peers it communicates with.
 */
public class Peer {
    private String id;
    private String ipAddress;
    private int port;
    private List<PeerInfo> peers;
    private LamportClock lamportClock;
    private Logger logger;
    private Server server;

    /**
     * Constructs a Peer instance with the given parameters.
     *
     * @param id the ID of the peer
     * @param ipAddress the IP address of the peer
     * @param port the port number of the peer
     * @param peers the list of other peers
     */
    public Peer(String id, String ipAddress, int port, List<PeerInfo> peers) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.peers = peers;
        this.lamportClock = new LamportClock(0); // Initialize LamportClock with initial time 0
        this.logger = Logger.getLogger(Peer.class.getName());

        try {
            FileHandler handler = new FileHandler("./" + id + "_peer.log", true);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.server = new Server(this);
            new Thread(server).start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Server", e);
        }
    }

    /**
     * Returns the IP address of the peer.
     *
     * @return the IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Returns the Logger of the peer.
     *
     * @return the Logger address
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the ID address of the peer.
     *
     * @return the ID address
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the Timestamp of the peer.
     *
     * @return the Timestamp address
     */
    public int getTimestamp() {
        return lamportClock.getTime();
    }

    /**
     * Returns the Port of the peer.
     *
     * @return the Port address
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the list of peers.
     *
     * @return the list of peers
     */
    public List<PeerInfo> getPeers() {
        return peers;
    }

    /**
     * Sends a message to all other peers.
     *
     * @param messageContent the content of the message to send
     */
    public void sendMessage(String messageContent) {
        Message message = new Message(messageContent, lamportClock.getTime(), id, ipAddress);
        lamportClock.increment();
        for (PeerInfo peerInfo : peers) {
            try (Socket socket = new Socket(peerInfo.getIpAddress(), peerInfo.getPort());
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(message);
            } catch (IOException e) {
                logger.warning("Failed to send message to peer " + peerInfo.getId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Receives a message and updates the Lamport clock based on the received message timestamp.
     *
     * @param message the received message
     */
    public void receiveMessage(Message message) {
        // Update Lamport clock based on received message timestamp
        lamportClock.update(message.getTimestamp());
        // Process the received message
        processMessage(message);
    }

    /**
     * Processes the received message.
     *
     * @param message the received message
     */
    private void processMessage(Message message) {
        // Logic to handle the message
        logger.info("Received message: " + message.getContent() + " from " + message.getSenderId());
    }


    /**
     * The main method to start a peer.
     *
     * @param args the command line arguments
     * @throws Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: java Peer <id> <ipAddress> <port> <peer1_ip:peer1_port> <peer2_ip:peer2_port> ...");
            return;
        }

        String id = args[0];
        String ipAddress = args[1];
        int port = Integer.parseInt(args[2]);

        List<PeerInfo> peers = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            String[] peerInfo = args[i].split(":");
            String peerIp = peerInfo[0];
            int peerPort = Integer.parseInt(peerInfo[1]);
            peers.add(new PeerInfo("peer" + (i - 2), peerIp, peerPort));
        }

        Peer peer = new Peer(id, ipAddress, port, peers);
        new Thread(new MultiCast(peer)).start();
    }
}

/**
 * This class represents information about a peer.
 */
class PeerInfo {
    private String id;
    private String ipAddress;
    private int port;

    /**
     * Constructs a PeerInfo instance with the given parameters.
     *
     * @param id the ID of the peer
     * @param ipAddress the IP address of the peer
     * @param port the port number of the peer
     */
    public PeerInfo(String id, String ipAddress, int port) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * Returns the ID of the peer.
     *
     * @return the ID of the peer
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the IP address of the peer.
     *
     * @return the IP address of the peer
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Returns the port number of the peer.
     *
     * @return the port number of the peer
     */
    public int getPort() {
        return port;
    }
}

/**
 * This class represents a server that listens for incoming connections and processes messages.
 */
class Server implements Runnable {
    private Peer peer;
    private ServerSocket serverSocket;

    /**
     * Constructs a Server instance for the given peer.
     *
     * @param peer the peer that the server belongs to
     * @throws Exception if an error occurs while initializing the server
     */
    public Server(Peer peer) throws Exception {
        this.peer = peer;
        this.serverSocket = new ServerSocket(peer.getPort(), 1, InetAddress.getByName(peer.getIpAddress()));
    }

    @Override
    public void run() {
        try {
            peer.getLogger().info("Server running on port " + peer.getPort());
            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String messageContent = in.readLine();
                Message message = new Message(messageContent, peer.getTimestamp(), peer.getId(), peer.getIpAddress());
                peer.receiveMessage(message);
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}