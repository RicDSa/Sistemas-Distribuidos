package ds.examples.sockets.ex8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 *Esta classe e parecida com a que esta 
 no pacote calculator, mas o metodo listen()
 e feito de forma diferente usando Thread.

 explicacao thread :https://www.w3schools.com/java/java_threads.asp
 */
public class CalculatorMulti {
	private ServerSocket server;

	public CalculatorMulti(String ipAddress) throws Exception {
		this.server = new ServerSocket(0, 1, InetAddress.getByName(ipAddress));
	}

	private void listen() throws Exception {
		while (true) {
			Socket client = this.server.accept();
			String clientAddress = client.getInetAddress().getHostAddress();
			System.out.printf("\r\nnew connection from %s\n", clientAddress);
			new Thread(new ConnectionHandler(clientAddress, client)).start();
		}
	}

	public InetAddress getSocketAddress() {
		return this.server.getInetAddress();
	}

	public int getPort() {
		return this.server.getLocalPort();
	}

	public static void main(String[] args) throws Exception {
		CalculatorMulti app = new CalculatorMulti(args[0]);
		System.out.printf("\r\nrunning server: host=%s @ port=%d\n",
				app.getSocketAddress().getHostAddress(), app.getPort());
		app.listen();
	}
}

// https://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html
class ConnectionHandler implements Runnable {
	String clientAddress;
	Socket clientSocket;

	public ConnectionHandler(String clientAddress, Socket clientSocket) {
		this.clientAddress = clientAddress;
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		/*
		 * prepare socket I/O channels
		 */
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

			while (true) {
				/*
				 * receive command
				 */
				String command;
				if ((command = in.readLine()) == null)
					break;
				else
					System.out.printf("message from %s : %s\n", clientAddress, command); 
					
				/*
				* process command
				*/

				String result = process(command);
			
				/*
				 * send result
				 */
				//out.println(String.valueOf(result));
				out.println(result);
				out.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param command
	 * @return
	 */
	private String process(String command){
		String result = "nothing";
		Scanner sc = new Scanner(command).useDelimiter(":");
		String op = sc.next();

		if (isArithmetic(op)) {
			double x = Double.parseDouble(sc.next());
			double y = Double.parseDouble(sc.next());

			switch (op) {
				case "add":
					result = String.valueOf(x + y);
					break;
				case "sub":
					result = String.valueOf(x - y);
					break;
				case "mul":
					result = String.valueOf(x * y);
					break;
				case "div":
					result = String.valueOf(x / y);
					break;
			}
		}
		else {
			String str = sc.next();
			String str2 = "";
			switch (op) {
				case "length":
					result = String.valueOf(str.length());
					break;
				case "equal":
					str2 = sc.next();
					result = String.valueOf(str.equals(str2));
					break;
				case "cat":	//Supondo que a operacao cat e a concatencao
					str2 = sc.next();
					result = str + str2;
					break;
				case "break":
					str2 = sc.next();
					result = Arrays.toString(str.split(str2));
					break;
				default:
					break;
			}
		}
		
		return result;
	}


	private Boolean isArithmetic(String operation){
		return operation.equals("add") || operation.equals("sub") || operation.equals("mul") || operation.equals("div");
	}

}
