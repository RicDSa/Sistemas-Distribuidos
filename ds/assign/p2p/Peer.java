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
    PeerConnection vizinhoInfo;

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
        vizinhoInfo = new PeerConnection(hostname, Integer.parseInt(port));
        
        // Inicializa o servidor
        try {
            this.server = new Server(this);
            new Thread(server).start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Server", e);
        }

    }


    public static void main(String[] args) throws Exception {
        String hostname = args[0];
        String port = args[1];
        Peer peer = new Peer(hostname, port);

        System.out.printf("New peer @ host=%s, port=%s%n", hostname, port);

        for (int i = 2; i < args.length; i += 2) {
            String nextHost = args[i];
            int nextPort = Integer.parseInt(args[i + 1]);
            peer.vizinhoInfo.addVizinho(nextPort, nextHost);
        }

        // Inicializa a sincronização de requests
        SyncronizedRequest syncronizedRequest = new SyncronizedRequest(peer.host, Integer.parseInt(peer.port), peer.vizinhoInfo,peer.logger);
        new Thread(syncronizedRequest).start();

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

                // Handle synchronization messages
                if (message.startsWith("SYNC-DATA")) {
                    String[] parts = message.split("::", 2);
                    if (parts.length == 2) {
                        String incomingMap = parts[1];
                        peer.vizinhoInfo.mergeIncomingMap(incomingMap);;
                    }
                }

                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            peer.logger.severe("Server encountered an error: " + e.getMessage());
        }
    }
}

