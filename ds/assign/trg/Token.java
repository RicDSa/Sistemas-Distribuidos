package ds.assign.trg;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Represents the Token class responsible for injecting a token into a Peer
 * for the first time in a distributed ring network.
 * 
 * This class is used to initiate the token-passing mechanism by sending a token
 * to a specified Peer in the ring.
 * 
 * @see <a href="https://github.com/RS181/">Repository</a>
 * @author Rui Santos
 */
public class Token {

    // The token value to be sent
    static String token = "TOKEN";


    /**
     * Sends the token to the specified Peer at the given host and port.
     * 
     * @param host the hostname of the Peer to send the token to
     * @param port the port of the Peer to send the token to
     */
    private static void sendToken(String host, int port) {
        
        try {
            Socket socket = new Socket(host,port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(token);
            out.flush();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        } 


    }


    /**
     * 
     * Main method to execute the Token injector
     * @param args
     * args[0] -> Peer hostname
     * args[1] -> Peer port
     * @throws Exception if an error occurs during execution
     */
    public static void main(String[] args) throws Exception {
        
        // Retrieve the host and port from command-line arguments
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        // Send the token to the specified Peer
        sendToken(host,port);

        // Log success message
        System.out.println("Token was sent successfuly");
    }
}