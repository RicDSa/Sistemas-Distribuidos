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
    String host;
    String port;
    Logger logger;
    volatile boolean hasToken;
    private Server server;

    public Peer(String hostname, String port, boolean hasToken) {
        this.host = hostname;
        this.port = port;
        this.hasToken = hasToken;
        logger = Logger.getLogger("logfile");

        try {
            FileHandler handler = new FileHandler("./" + hostname + "_peer.log", true);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Inicializa o servidor
            this.server = new Server(this);
            new Thread(server).start();
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
        boolean hasToken = args[2].equals("TOKEN");
        Peer peer = new Peer(args[0], args[1], hasToken);
        System.out.printf("New peer @ host=%s, port=%s, hasToken=%s\n", args[0], args[1], hasToken);

        new Thread(new Server(peer)).start();
        new Thread(new Client(peer, args[3], Integer.parseInt(args[4]))).start();

        // Start the RequestGenarator that genartes a request for the server
        // with an average frequency of 4 events per minute
        /*Server serverInstance = new Server(peer);
        new Thread(serverInstance).start();
        RequestGenerator requestGenerator = new RequestGenerator(args[0],Integer.parseInt(args[1]),peer.logger,serverInstance);
        new Thread(requestGenerator).start();*/
    }
}

class Server implements Runnable {
    Peer peer;
    ServerSocket server;
    private final Queue<String> operations = new LinkedList<>();

    public Server(Peer peer) throws Exception {
        this.peer = peer;
        this.server = new ServerSocket(Integer.parseInt(peer.port), 1, InetAddress.getByName(peer.host));
    }

    public synchronized void addOperations(String operation) {
        operations.add(operation);
    }

    public synchronized Queue<String> getOperations() {
        return operations;
    }

    @Override
    public void run() {
        try {
            peer.logger.info("Server: running on port " + peer.port);
            while (true) {
                Socket client = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

				//Recebe o token
                String receivedToken = in.readLine();
                if ("TOKEN".equals(receivedToken)) {

                    peer.logger.info("Server: Token received from client");
                    synchronized (peer) {
                        peer.hasToken = true; // Atualiza para indicar que o peer agora possui o token
                    }
                }

                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Client implements Runnable {
    Peer peer;
    String host;
    String port;
    String nextHost;
    int nextPort;
    Logger logger;
    String calculatorServerAddress;

    public Client(Peer peer, String nextHost, int nextPort) {
        this.peer = peer;
        this.host = peer.host;
        this.port = peer.port;
        this.calculatorServerAddress = "localhost";
        this.nextHost = nextHost;
        this.nextPort = nextPort;
        this.logger = peer.logger;
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (peer) {
                    if (peer.hasToken) {

                        //Use the synchronized modifier to prevent race conditions between threads
                        //Notice that we passed a parameter operations to the synchronized block. 
                        //This is the monitor object. The code inside the block gets synchronized 
                        //on the monitor object. Simply put, only one thread 
                        //per monitor object can execute inside that code block (to avoid diferences of
                        //the content in operations queue)

                        synchronized(peer.getOperations()){

                            //2.1 Send all command's in operation to CalculatorMultiServer
                            while (!peer.getOperations().isEmpty()){
                                String request = peer.getOperations().poll();
                                String result = connectToCalculatorMultiServer(calculatorServerAddress, 44444, request);
                                logger.info("client @" + port + " RECEIVED result from server: " + result);
                            }

                            // Comentar no fim (so serve para facilitar a  visualizacao do funcionamento)
                            //Thread.sleep(5000);
                        }
                
                        
                        // Comentar no fim (so server para ser mais facil de ver troca de token)
                        Thread.sleep(5000);
                            

                        // Envia o token para o próximo peer
                        Socket socket = new Socket(InetAddress.getByName(nextHost), nextPort);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                        out.println("TOKEN");
                        out.flush();
                        socket.close();

                        peer.logger.info("Client: Token sent to " + nextHost + ":" + nextPort);

                        // Remove o token localmente após enviá-lo
                        peer.hasToken = false;
                    }
                }

                // Pausa para evitar sobrecarga na comunicação
                Thread.sleep(2000);
            } catch (Exception e) {
                peer.logger.warning("Client: Failed to connect to " + nextHost + ":" + nextPort);
            }
        }
    }

    public String connectToCalculatorMultiServer(String serverHost,int serverPort, String command){
        String result ="";

        try {
            /*
             * create comunication Socket 
             */
            Socket socket = new Socket(InetAddress.getByName(serverHost),serverPort);
            logger.info("client @" + port + " connected to calculator server at " + serverHost + ":" + serverPort);
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

            /*
            * receive result
            */
            result = in.readLine();
            //logger.info("client @" + port + " received result: " + result);

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

}
