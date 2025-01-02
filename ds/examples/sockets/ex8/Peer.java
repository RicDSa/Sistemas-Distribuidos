package ds.examples.sockets.ex8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Peer {
    String host;
    Logger logger;
    boolean hasToken = false;
    int serverPort;

    public Peer(String hostname, int serverPort) {
        this.host = hostname;
        this.serverPort = serverPort;
        logger = Logger.getLogger("logfile");
        try {
            FileHandler handler = new FileHandler("./" + hostname + "_peer.log", true);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Peer peer = new Peer(args[0], Integer.parseInt(args[1]));
        System.out.printf("New peer @ host=%s\n", args[0]);
        new Thread(new Server(args[0], Integer.parseInt(args[1]), peer.logger, peer)).start();
        new Thread(new RequestGenerator(peer)).start();
    }

    public synchronized void receiveToken() {
        hasToken = true;
        logger.info("Token received.");
    }

    public synchronized void passToken(String peerHost, int peerPort) {
        hasToken = false;
        try (Socket socket = new Socket(peerHost, peerPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("TOKEN");
            logger.info("Token passed to " + peerHost + ":" + peerPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void attemptCalculation() {
        if (hasToken) {
            // Simulate coin flip
            if (new Random().nextBoolean()) {
                try (Socket socket = new Socket("localhost", serverPort);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String command = "localhost " + serverPort + " add 3 5"; // Example calculation
                    out.println(command);
                    String result = in.readLine();
                    System.out.printf("Calculation result: %s\n", result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class Server implements Runnable {
    String host;
    int port;
    ServerSocket server;
    Logger logger;
    Peer peer;
    String token;

    public Server(String host, int port, String nextHost, int nextPort, Logger logger, Peer peer) throws Exception {
        this.host = host;
        this.port = port;
        this.nextHost = nextHost;
        this.nextPort = nextPort;
        this.logger = logger;
        this.peer = peer;
        server = new ServerSocket(port, 1, InetAddress.getByName(host));
    }

    @Override
    public void run() {
        try {
            logger.info("Server: endpoint running at port " + port + " ...");
            while (true) {
                try {
                    Socket client = server.accept();
                    String clientAddress = client.getInetAddress().getHostAddress();
                    logger.info("Server: new connection from " + clientAddress);

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    
                    String message = in.readLine();
                    if(message.equals("Token")){
                        try {
                            Socket socket = new Socket(nextHost, nextPort);
                            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);

                            out.println("Token");
                            out.flush();

                            socket.close();
                            token = null;

                            logger.info("Client @" + host + "sent the token to peer at port " + nextPort);

                            String clientAddress = client.getInetAddress().getHostAddress();
                        } catch (Exception e) {
                            System.out.printf("Connection between %s @%s and %s @%s FAILED\n", host, port, nextHost,
                                nextPort);
                        }
                    }

                    logger.info("Server: message from host " + clientAddress + "[message = " + message + "]");
            if (message.equals("TOKEN")) {
                peer.receiveToken();
            }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

}

public String connectToCalculatorMultiServer(String serverHost, int serverPort, String command) {
        String result = "";

        try {
            /*
             * create comunication Socket
             */
            Socket socket = new Socket(InetAddress.getByName(serverHost), serverPort);

            /*
             * prepare socket I/O channels
             */
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            /*
             * send command and port of this client
             */
            out.println(command + ":" + port);
            out.flush();

            /*
             * receive result
             */
            result = in.readLine();

            /*
             * close connection
             */
            socket.close();
        } catch (Exception e) {
            logger.info("client @" + port + " failed to connect to calculator server.");
            e.printStackTrace();

        }

        return result;
    }


    private String generateRandomRequest() {

        String[] operations = { "add", "sub", "mul", "div" };

        Random random = new Random();
        String operation = operations[random.nextInt(operations.length)];

        double x = Math.floor(random.nextDouble() * 100);
        double y = Math.floor(random.nextDouble() * 100);

        return operation + ":" + x + ":" + y;
    }

/*class RequestGenerator implements Runnable {
    Peer peer;

    public RequestGenerator(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (peer.hasToken) {
                    peer.attemptCalculation();
                    peer.passToken("localhost", peer.serverPort == 22222 ? 33333 : 22222);
                } else {
                    // Wait for a moment before checking for the token
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}*\
