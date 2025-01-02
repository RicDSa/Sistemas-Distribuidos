package ds.assign.p2p;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;


public class SyncronizedRequest implements Runnable{

    private PoissonProcess poissonProcess = null;

    private String host;
    private int localport;
    private Logger logger;

    private PeerConnection vizinhoInfo;

    public SyncronizedRequest(String host, int localport, PeerConnection vizinhoInfo, Logger logger){
        this.host = host;
        this.localport = localport;
        this.vizinhoInfo = vizinhoInfo;
        this.logger = logger;

        //Inicia PoissonProcess
        Random rng = new Random();
        double lambda = 1.0;  //1 evento por minuto
        poissonProcess = new PoissonProcess(lambda, rng);
    }    

    private void sendRequestToServer(String message) {
        try {
            String randomVizinho = vizinhoInfo.chooseRandomVizinho();
            if (randomVizinho == null) {
                return;
            }

            Scanner sc = new Scanner(randomVizinho).useDelimiter(":");
            String destinationHost = sc.next();
            int destinationPort = Integer.parseInt(sc.next());

            Socket socket = new Socket(destinationHost, destinationPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message + "::" + vizinhoInfo.toString());
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        logger.info("Started SyncronizedRequest on @" + localport);

        while (true) { 
            double intervalTime = poissonProcess.timeForNextEvent() * 1000 * 60;

            try {
                Thread.sleep((long) intervalTime);

                synchronized (vizinhoInfo) {
                    logger.info("DEBUG -> @" + localport + " vizinhos= " + vizinhoInfo.getVizinhos());

                    // Send a synchronization request to a random neighbor
                    sendRequestToServer("SYNC-DATA");

                    // Print the number of peers in the updated map
                    logger.info("Number of peers in the updated map: " + vizinhoInfo.getVizinhos().size());

                }
            } catch (Exception e) {
                e.printStackTrace(); 
            }
        }
    }

}