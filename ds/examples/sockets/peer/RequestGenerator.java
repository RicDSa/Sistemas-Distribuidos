package ds.examples.sockets.peer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;



public class RequestGenerator implements Runnable{

    String  host;
    Logger  logger;
	String  port;
	

    public RequestGenerator(String host,String port,Logger logger) {
		this.host = host;
		this.port = port;
		this.logger = logger;
    }

    @Override 
    public void run() {
	try {
	    logger.info("client: endpoint running ...\n");	
	    /*
	     * send messages such as:
	     *   - ip port add x y
	     *   - ip port sub x y
	     *   - ip port mul x y
	     *   - ip port div x y
	     * where x, y are floting point values and ip is the address of the server
	     */
	    while (true) {
		try {
		    /* 
		     * make connection
		     */
		    Socket socket  = new Socket(InetAddress.getByName(host), Integer.parseInt(port));
		    logger.info("client: connected to server " + socket.getInetAddress() + "[port = " + socket.getPort() + "]");
		    /*
		     * prepare socket I/O channels
		     */
		    PrintWriter   out = new PrintWriter(socket.getOutputStream(), true);
		    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));    
		    /*
		     * send command
		     */
		    out.println(host + " " + port + " add 3.0 5.0");
		    out.flush();	    
		    /*
		     * receive result
		     */
		    String result = in.readLine();
		    System.out.printf("= %f\n", Double.parseDouble(result));
		    /*
		     * close connection
		     */
		    socket.close();
		} catch(Exception e) {
		    e.printStackTrace();
		}   
	    }
	} catch(Exception e) {
	    e.printStackTrace();
	}   	    
    }
}