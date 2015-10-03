import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class MasterServer {

	private static ServerSocket serverSocket = null;
	private static Socket ServerClientsSocket = null;
	private static final int maxServersCount = 4;
	private static final ServerThread[] Sthreads = new ServerThread[maxServersCount];

	public static void main(String args[]) {

		// The default port number.

		int portNumber = 6000;

		if (args.length < 1) {
			System.out
					.println("Usage: java MultiThreadChatServer <portNumber>\n"
							+ "Now using port number=" + portNumber);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
		}

		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		while (true) {
			try {
				ServerClientsSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxServersCount; i++) {
					if (Sthreads[i] == null) {
						(Sthreads[i] = new ServerThread(ServerClientsSocket, Sthreads))
								.start();
						break;
					}
				}
				if (i == maxServersCount) {
					PrintStream os = new PrintStream(ServerClientsSocket.getOutputStream());
					os.println("Server too busy. Try later.");
					os.close();
					ServerClientsSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}

	}
}
