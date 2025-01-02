package ds.assign.tom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Logger;


/**
 * This class implements the Totally-Ordered Multicast (TOM) algorithm using Lamport clocks.
 * It handles message broadcasting and processing to ensure a globally agreed order of messages.
 */
public class MultiCast implements Runnable {

    // Peer that is going to send message
    private Peer currentPeer;
    private Logger logger;
    private LamportClock LamportClock;
    private PriorityQueue<Message> messageQueue;
    // List of words loaded from a dictionary file
    private ArrayList<String> wordsList;
    private PoissonProcess poissonProcess;

    /**
     * Constructs a MultiCast instance for the given Peer. Initializes the Poisson process for event 
     * generation, loads the dictionary of words, and initializes the Lamport clock.
     *
     * @param peer the Peer instance that will be responsible for sending messages
     */
    public MultiCast(Peer peer) {
        this.currentPeer = peer;
        this.logger = peer.getLogger();

        // Initialize the message queue with a comparator for Lamport timestamps
        this.messageQueue = new PriorityQueue<>((m1, m2) -> {
            int cmp = Integer.compare(m1.getTimestamp(), m2.getTimestamp());
            return cmp != 0 ? cmp : m1.getSenderId().compareTo(m2.getSenderId());
        });

        // Initialize Poisson process for generating events
        Random rng = new Random();
        double lambda = 60.0; // 60 events per minute
        poissonProcess = new PoissonProcess(lambda, rng);

        // Load words from dictionary file
        wordsList = new ArrayList<>();
        loadWordsFromFile("ds/assign/tom/dictionary.txt");

        // Initialize Lamport clock
        LamportClock = new LamportClock(0);
    }

    /**
     * Sends a message containing a word to all peers.
     *
     * @param word the word to send
     */
    private void sendMessage(String word) {
        // Increment the Lamport clock before sending the message
        LamportClock.increment();
        // Create a new message with the current timestamp and peer ID
        Message message = new Message(word, LamportClock.getTime(), currentPeer.getId(), currentPeer.getIpAddress());
        // Add the message to the message queue
        messageQueue.add(message);
        // Broadcast the message to all peers
        broadcastMessage(message);
    }

     /**
     * Broadcasts a message to all peers.
     *
     * @param message the message to broadcast
     */
    private void broadcastMessage(Message message) {
        for (PeerInfo peerInfo : currentPeer.getPeers()) {
            boolean sent = false;
            int retries = 3;
            while (!sent && retries > 0) {
                try (Socket socket = new Socket(peerInfo.getIpAddress(), peerInfo.getPort());
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    out.println(message);
                    sent = true;
                } catch (IOException e) {
                    logger.warning("Failed to send message to peer " + peerInfo.getId() + ": " + e.getMessage());
                    retries--;
                    try {
                        Thread.sleep(1000); // Wait before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Receives a message and adds it to the message queue for processing.
     *
     * @param message the received message
     */
    public void receiveMessage(Message message) {
        messageQueue.add(message);
        processMessages();
    }

    /**
     * Processes messages in the message queue in the order determined by the Lamport timestamps.
     */
    private void processMessages() {
        while (!messageQueue.isEmpty()) {
            Message message = messageQueue.poll();
            if (message.getTimestamp() >= LamportClock.getTime()) {
                LamportClock.setTime(message.getTimestamp() + 1);
                logger.info("Received message: " + message.getContent() + " from " + message.getSenderId());
            } else {
                messageQueue.add(message); // Re-add to queue if not ready to process
                break;
            }
        }
    }

    /**
     * Loads words from the specified dictionary file.
     *
     * @param filePath the path to the dictionary file
     */
    private void loadWordsFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                wordsList.add(line.trim());
            }
        } catch (IOException e) {
            logger.severe("Error loading words from file: " + e.getMessage());
        }
    }


    @Override
    public void run() {
        while (true) {
            try {
                // Wait for the next event based on Poisson process
                Thread.sleep((long) (poissonProcess.nextEvent() * 1000));
                // Select a random word from the dictionary
                String word = wordsList.get(new Random().nextInt(wordsList.size()));
                // Send the selected word as a message
                sendMessage(word);
            } catch (InterruptedException e) {
                logger.warning("Multicast thread interrupted: " + e.getMessage());
            }
        }
    }

}