package ds.assign.trg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class CalculatorMultiServer {
    private ServerSocket server; // server socket
	private int serverPort = 44444;	// server port

	// Constructor to initialize the server with the given IP address
    public CalculatorMultiServer(String ipAddress) throws Exception {
        this.server = new ServerSocket(serverPort, 1, InetAddress.getByName(ipAddress));
    }

	// Method to listen for incoming connections
    private void listen() throws Exception {
		while(true) {
			// Accept a new client connection
			Socket client = this.server.accept();
			String clientAddress = client.getInetAddress().getHostAddress();
			System.out.printf("\r\nnew connection from %s\n", clientAddress);
			// Start a new thread to handle the connection
			new Thread(new ConnectionHandler(clientAddress, client)).start();
		}
    }
 
	// Method to get the server's socket address
    public InetAddress getSocketAddress() {
		return this.server.getInetAddress();
    }

	// Method to get the server's port
    public int getPort() {
		return serverPort;
    }

    // Main method to start the server
	public static void main(String[] args) throws Exception {
		CalculatorMultiServer app = new CalculatorMultiServer(args[0]);
		System.out.printf("\r\nrunning server: host=%s @ port=%d\n",
			app.getSocketAddress().getHostAddress(), app.getPort());
		app.listen();
    }
}



class ConnectionHandler implements Runnable {
    String clientAddress; // client address
    Socket clientSocket;  // client socket

	// Constructor to initialize the connection handler with the client address and socket
    public ConnectionHandler(String clientAddress, Socket clientSocket) {
		this.clientAddress = clientAddress;
		this.clientSocket  = clientSocket;    
    }

    @Override
    public void run() {
		/*
		* prepare socket I/O channels
		*/
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));    
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		
			while(true) {
				/* 
				* receive command 
				*/
				String command;
				if( (command = in.readLine()) == null)
					break;
				else
					System.out.printf("message from %s : %s\n", clientAddress, command);		      	                    
				
				
				/*
				* process command 
				*/
				Scanner sc = new Scanner(command).useDelimiter(":");
				String  op = sc.next();
				double  x  = Double.parseDouble(sc.next());
				double  y  = Double.parseDouble(sc.next());
				double  result = 0.0; 

				// Perform the operation based on the command
				switch(op) {
					case "add": result = x + y; break;
					case "sub": result = x - y; break;
					case "mul": result = x * y; break;
					case "div": result = x / y; break;
				}  
				/*
				* send result
				*/
				out.println(String.valueOf(result));
				out.flush();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
    }
}
