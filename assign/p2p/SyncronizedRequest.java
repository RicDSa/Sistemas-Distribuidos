package ds.assign.p2p;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;


public class SyncronizedRequest implements Runnable{

    private PoissonProcess poissonProcess = null;

    //Info do peer de Origem
    private final String host;
    private final int localport;
    private final Logger logger;

    //Info do Peer de Destino    
    private int destinationPort;
    private String destinationHost;
    private final PeerConnection vizinhoInfo;

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

    @Override

    public void run(){
        logger.info("Started SyncronizedRequest on @" + localport);

        while (true) { 
            double intervalTime = poissonProcess.timeForNextEvent() * 1000 * 60;

            try {
                Thread.sleep((long)intervalTime);
                
                synchronized (vizinhoInfo){
                    logger.info("DEBUG -> @" + localport + " vizinhos= " + vizinhoInfo.getVizinhos() );

                    //formato: "hostname:port"
                    // escolhe um vizinho aleatório para sincronizar
                    String n = vizinhoInfo.chooseRandomVizinho();

                    Scanner sc = new Scanner(n).useDelimiter(":");
                    destinationHost = sc.next();
                    destinationPort = Integer.parseInt(sc.next());
                }

                // manda um request de sincronização para o Peer no Host e porta de destino
                sendRequestToServer("SYNC-DATA");
            } catch (Exception e) {
                e.printStackTrace(); 
            }
        }
    }


    public void sendRequestToServer(String request) {

        try {
            /*
            * faz a conexão
            */
           Socket socket = new Socket(InetAddress.getByName(destinationHost), destinationPort);

           /*
			* prepare socket output channel
		    */
           PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

           /*
            * manda um request de sincronização
            */

           out.println(request + ":" + localport + ":" + host);
           out.flush();

           /*
            * fecha a conexão
            */
           socket.close();

        } catch (Exception e) {
            logger.info("Server: error ocured while sending SYNC request to "+ destinationHost + " " + destinationPort);
        }
    }
}