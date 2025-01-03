package ds.assign.trg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Peer {
    String host; // Hostname of the peer
    int port; // Port number of the peer
    Logger logger; // Logger for logging events
    String Token; // Flag to indicate if the peer has the token
    private Server server; // Server instance for the peer

    // Constructor to initialize the peer
    public Peer(String hostname, int port, String nextHost,int nextPort,String calcHost, int calcPort) {
        this.host = hostname;
        this.port = port;
        logger = Logger.getLogger("logfile");

        try {
            // Set up the file handler for logging
            FileHandler handler = new FileHandler("./" + hostname + "_peer.log", true);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Inicializa o servidor
            this.server = new Server(this,nextHost,nextPort,calcHost,calcPort);
            new Thread(server).start();

             // Start the RequestGenerator thread
            RequestGenerator requestGenerator = new RequestGenerator(hostname, port, logger, server);
            new Thread(requestGenerator).start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Server", e);
        }
    }

    // Getter para acessar operações do Server
    public Queue<String> getOperations() {
        return server.getOperations();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 6) {
            System.err.println("Usage: java ds.assign.trg.Peer <host> <port> <nextHost> <nextPort> <calcHost> <calcPort>");
            System.exit(1);
        }
        // Create a new peer instance
        Peer peer = new Peer(args[0], Integer.parseInt(args[1]), args[2],Integer.parseInt(args[3]), args[4], Integer.parseInt(args[5]));
        System.out.printf("New peer @ host=%s, port=%s\n", args[0], Integer.parseInt(args[1]));
    }
}

class Server implements Runnable {
    Peer peer; // Reference to the peer
    ServerSocket server; // Server socket for the peer
    String receivedToken;

    // Queue to store operations
    Queue<String> operations = new LinkedList<>();

    // Hostname and port of the next peer
    String nextHost;
    int nextPort;

    // Hostname and port of the calculator service
    String calcHost;
    int calcPort;

    // Constructor to initialize the server
    public Server(Peer peer, String nextHost, int nextPort, String calcHost, int calcPort) throws Exception {
        this.peer = peer;
        this.server = new ServerSocket(peer.port, 1, InetAddress.getByName(peer.host));
        this.nextHost = nextHost;
        this.nextPort = nextPort;
        this.calcHost = calcHost;
        this.calcPort = calcPort;
    }

    // Method to add operations to the queue
    public void addOperations(String operation) {
        operations.add(operation);
        peer.logger.info("Server: Added operation to queue: " + operation);
    }

    // Method to get operations from the queue
    public Queue<String> getOperations() {
        return operations;
    }

    @Override
    public void run() {
        try {
            peer.logger.info("Server: running on port " + peer.port);
            while (true) {

                try{
                        //Aceita ligações para receber o Token
                        Socket client = server.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        //Recebe o token
                        String receivedToken = in.readLine();

                        if (receivedToken.equals("TOKEN")) {
                            peer.logger.info("server: received token");
                            synchronized (peer) {
                                processOperations();
                                passTokenToNextPeer();
                            }
                        }

                        client.close();
                    } catch (Exception e) {
                        peer.logger.warning("Timeout: Token was not received in given time limit.\nPeer will Shutdown ...");
                        Thread.sleep(5000);
                        System.exit(0); // Encerrar o programa ou tomar outra ação
                    }

                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para processsar operações que estão na fila de operações
    private void processOperations(){
        synchronized (operations) {
            while(!operations.isEmpty()){
                String request = operations.poll();
                peer.logger.info("Server: Processing operation: " + request);

                String result = connectToCalculatorMultiServer(calcHost, calcPort, request);
                peer.logger.info("Server: Processed operation: " + request + ", Result: " + result);

                if (result != null && !result.isEmpty()) {
                peer.logger.info("Server: Received result from calculator server: " + result);
            } else {
                peer.logger.warning("Server: No result received for operation: " + request);
            }
            }
        }
    }

    // Método para passar o token para o próximo peer
    private void passTokenToNextPeer() {
        try {
            peer.logger.info("Server: Preparing to pass the token to the next peer at " + nextHost + ":" + nextPort);
            Thread.sleep(5000); // Optional delay for visualization
            Socket socket = new Socket(InetAddress.getByName(nextHost), nextPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("TOKEN");
            out.flush();
            socket.close();

            peer.logger.info("Server: Token passed to " + nextHost + ":" + nextPort);
        } catch (Exception e) {
            peer.logger.warning("Server: Failed to pass token to " + nextHost + ":" + nextPort);
            e.printStackTrace();
        }
    }


    // Método para conectar ao servidor de calculadora
    public String connectToCalculatorMultiServer(String serverHost,int serverPort, String command){
        String result ="";

        try {
            /*
             * create comunication Socket 
             */
            Socket socket = new Socket(InetAddress.getByName(serverHost),serverPort);
            peer.logger.info("client @" + peer.port + " connected to calculator server at " + serverHost + ":" + serverPort);
            /*
            * prepare socket I/O channels
            */
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         
            /*
            * send command of this client
            */
            out.println(command);
            out.flush();
            peer.logger.info("Server: Sent operation to calculator server: " + command);
            /*
            * receive result
            */
            result = in.readLine();
            peer.logger.info("Server: Received result: " + result);
            //logger.info("client @" + port + " received result: " + result);

		    /*
		     * close connection
		    */
            socket.close();
        } catch (Exception e) {
            peer.logger.info("client @" + peer.port + " failed to connect to calculator server.");
            e.printStackTrace();
        
        }

        return result;
    }
}

