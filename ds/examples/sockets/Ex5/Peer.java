package ds.examples.sockets.ex_5;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.Arrays;
import java.util.Random;

/**
 * Represents a Peer that only acts as a Server 
 * and uses  RequestGenerator.java to randomly send
 * a command to another Peer (according to a Poisson
 * distribution with an average of 5 events per minute)
 */
public class Peer {
	String host;
	Logger logger;

	public Peer(String hostname) {
		host = hostname;
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

	/**
	 * args[0] -> localhost 
	 * args[i] (i > 0 ) -> representam as portas dos diferentes 
	 * peers  
	 * exemplo com 2 peers usando o metodo multipleTerminalVersion
	 * $java Peer localhost 20001 20002 
	 * $java Peer localhost 20002 20001
	 * 
	 * exemplo com 2 peers usando o metodo oneTerminalVersion
	 * $java Peer localhost 20001 20002
	 * @param args 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Peer peer = new Peer(args[0]);
		System.out.printf("new peer @ host=%s\n", args[0]);

		/*
		 * Escolher versao que so utiliza um terminal
		 * ou versao que utiliza apenas um terminal
		 */
		
		oneTerminalVersion(args,peer);

	}
	
	private static void oneTerminalVersion(String [] args, Peer peer) throws Exception {
		int numberPeers = args.length-1;
		for(int i = 0 ; i <numberPeers ;i++){
			new Thread(new Server(args[0], Integer.parseInt(args[i+1]), peer.logger)).start();
		}

		String [] availablePorts = Arrays.copyOfRange(args,1,args.length);
		for(int i = 0 ; i < numberPeers ;i++){
			new Thread(new RequestGenerator(args[0], availablePorts, peer.logger )).start();;
			//System.out.println(chooseRandomPort(availablePorts));
		}

	}
}




class Server implements Runnable {
	String host;
	int port;
	ServerSocket server;
	Logger logger;

	public Server(String host, int port, Logger logger) throws Exception {
		this.host = host;
		this.port = port;
		this.logger = logger;
		server = new ServerSocket(port, 1, InetAddress.getByName(host));
	}

	@Override
	public void run() {
		try {
			logger.info("server: endpoint running at port " + port + " ...");
			while (true) {	 // Permite que o servidor esteja constantemente a escuta de novas conexoes
				try {
					// Para execuao do servidor e fica a escuta da ligacao do cliente
					// e continua para proxima linha
					Socket client = server.accept();
					String clientAddress = client.getInetAddress().getHostAddress();
					logger.info("server: new connection from " + clientAddress);
					
					//Cria uma nova Thread para lidar com o cliente
					new Thread(new Connection(clientAddress, client, logger)).start();
				} catch (Exception e) {
					logger.info("server: error ocorred while trying to accept new connection");
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Connection implements Runnable {
	String clientAddress;
	Socket clientSocket;
	Logger logger;

	public Connection(String clientAddress, Socket clientSocket, Logger logger) {
		this.clientAddress = clientAddress;
		this.clientSocket = clientSocket;
		this.logger = logger;
	}

	@Override
	public void run() {
		/*
		 * prepare socket I/O channels
		 */
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			
			String [] command = in.readLine().split(" ");

			logger.info("server: message from host " + clientAddress + "[command = " + displayCommand(command) + "]");
			/*
			 * parse command
			 */

			String op = command[2];
			double x = Double.valueOf(command[3]);
			double y = Double.valueOf(command[4]);
			double result = 0.0;
			/*
			 * execute op
			 */
			switch (op) {
				case "add":
					result = x + y;
					break;
				case "sub":
					result = x - y;
					break;
				case "mul":
					result = x * y;
					break;
				case "div":
					result = x / y;
					break;
			}
			/*
			 * send result
			 */
			
			out.println(String.valueOf(result));
			out.flush();
			//logger.info(String.valueOf(result));

			/*
			 * close connection
			 */
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String displayCommand(String[] command){
		String ans = "";
		for (String s : command) 
			ans += s + " ";
		return ans;
	}
}

