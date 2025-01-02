package ds.peerteste;

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
    volatile boolean hasToken;

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
    }

    public static void main(String[] args) throws Exception {
        boolean hasToken = args[2].equals("true");
        Peer peer = new Peer(args[0], args[1], hasToken);
        System.out.printf("New peer @ host=%s, port=%s, hasToken=%s\n", args[0], args[1], hasToken);

        new Thread(new Server(peer)).start();
        new Thread(new Client(peer, args[3], Integer.parseInt(args[4]))).start();
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
    String nextHost;
    int nextPort;

    public Client(Peer peer, String nextHost, int nextPort) {
        this.peer = peer;
        this.nextHost = nextHost;
        this.nextPort = nextPort;
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (peer) {
                    if (peer.hasToken) {
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
}
