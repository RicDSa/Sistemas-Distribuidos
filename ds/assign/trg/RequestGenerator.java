package ds.assign.trg;

import java.util.Random;
import java.util.logging.Logger;



/**
 * A thread that generates a random request and sends it 
 * to a given Peer (basicaly acts as a client)
 */
public class RequestGenerator implements Runnable{

    private final String host;
    private final int localPort;
    private final Logger logger;
    private final Server server;
    private  PoissonProcess poissonProcess = null;


   
    /**
     * All atributes are realated to the Peer that started this RequesteGenarator
     * @param host      --> Peer host name 
     * @param localPort --> Peer local port
     * @param logger    --> Peer loger
     * @param server    --> Peer Server 
     */
    public RequestGenerator (String host,int localPort, Logger logger,Server server){
        this.host = host;
        this.localPort = localPort;
        this.logger = logger;
        this.server = server;

        //Intialize PoissonProcess
        Random rng = new Random();
        double lambda = 4.0;   // 4 events per minute
        poissonProcess = new PoissonProcess(lambda, rng);
    }


    @Override
    public void run() {
        while (true) {
            double intervalTime = poissonProcess.timeForNextEvent() * 1000 * 60; // Converting to milliseconds

            try {
                Thread.sleep((long)intervalTime);
                String request = generateRandomRequest();
                
                //Use the synchronized modifier to prevent race conditions between threads.
                //Notice that we passed a parameter request to the synchronized block. 
                //This is the monitor object. The code inside the block gets synchronized 
                //on the monitor object. Simply put, only one thread 
                //per monitor object can execute inside that code block (to avoid adding the same request
                //to the queue of the peer's server)
                synchronized(request){
                    server.addOperations(request);
                }

            }catch (Exception e){
                e.printStackTrace(); 
            }

        }
    }


    /**
     * 
     * @return a String of a request in the format
     * "operation:x:y"
     * where x and y are double's
     * 
     */
    private String generateRandomRequest() {

        String[] operations = {"add" , "sub" , "mul" , "div"};

        Random random = new Random();
        String operation = operations[random.nextInt(operations.length)];

        //Used Math.floor, to have more 'readable' numbers
        double x = Math.floor(random.nextDouble() * 100);
        double y = Math.floor(random.nextDouble() * 100);

        return  operation + ":" + x + ":" + y;
    }
    


}