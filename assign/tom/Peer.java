package ds.assign.p2p;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Peer {
    String host;
    String port;
    Logger logger;
    private Server server;
    private PeerConnection peerConnection;

    public Peer(String hostname, String port) {
        this.host = hostname;
        this.port = port;
        logger = Logger.getLogger("logfile");

        // Configura o logger para guardar em arquivo
        try {
            FileHandler handler = new FileHandler("./" + hostname + "_peer.log", true);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Inicializa as conexões de vizinhos
        peerConnection = new PeerConnection(hostname, Integer.parseInt(port));

        // Inicializa o servidor
        try {
            this.server = new Server(this);
            new Thread(server).start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Server", e);
        }

        // Inicializa a sincronização de requests
        new Thread(new SyncronizedRequest(hostname, Integer.parseInt(port), peerConnection, logger)).start();
    }


    public static void main(String[] args) throws Exception {
        String hostname = args[0];
        String port = args[1];
        Peer peer = new Peer(hostname, port);

        System.out.printf("New peer @ host=%s, port=%s%n", hostname, port);

        //Inicializa o cliente para conexão com o vizinho,se configurado
        if(args.length >= 4){
            String nextHost = args[2];
            int nextPort = Integer.parseInt(args[3]);
            peer.peerConnection.addVizinho(nextPort, nextHost);
            new Thread(new Client(peer, nextHost, nextPort)).start();
        }

    }
}

class Server implements Runnable {
    Peer peer;
    ServerSocket server;

    public Server(Peer peer) throws Exception {
        this.peer = peer;
        this.server = new ServerSocket(Integer.parseInt(peer.port), 1, InetAddress.getByName(peer.host));
    }

    @Override
    public void run() {
        try {
            peer.logger.info("Server: running on port " + peer.port);
            while (true) {
                Socket client = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String message = in.readLine();
                peer.logger.info("Server received: " + message);
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            peer.logger.severe("Server encountered an error: " + e.getMessage());
        }
    }
}

class Client implements Runnable {
    Peer peer;
    String nextHost;
    int nextPort;
    Logger logger;

    public Client(Peer peer, String nextHost, int nextPort) {
        this.peer = peer;
        this.nextHost = nextHost;
        this.nextPort = nextPort;
        this.logger = peer.logger;
    }

    @Override
    public void run() {
        while (true) {
            try {
                logger.info("Client connecting to " + nextHost + ":" + nextPort);
                Socket socket = new Socket(InetAddress.getByName(nextHost), nextPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Hello from " + peer.host + ":" + peer.port);
                socket.close();

                // Pausa para evitar sobrecarga na comunicação
                Thread.sleep(2000);
            } catch (Exception e) {
                peer.logger.warning("Client: Failed to connect to " + nextHost + ":" + nextPort);
                try {
                    Thread.sleep(2000); // Pausa antes de tentar novamente
                } catch (InterruptedException ignored) {}
            }
        }
    }

}
