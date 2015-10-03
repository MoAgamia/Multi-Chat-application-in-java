import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class ClientThread extends Thread {
	private String clientName = null;
	private DataInputStream InStream = null;
	private PrintStream OutStream = null;
	private Socket clientSocket = null;
	private static ClientThread[] Clientthreads;
	private static int maxClientsCount;

	public ClientThread(Socket clientSocket, ClientThread[] threads) {
		this.clientSocket = clientSocket;
		ClientThread.Clientthreads = threads;
		maxClientsCount = threads.length;
	}

	public static ClientThread[] getThreads() {
		return Clientthreads;
	}

	public String getClientName() {
		return clientName;
	}

	public boolean CheckName(String name) {
		name = "@" + name;
		for (int i = 0; i < maxClientsCount; i++) {
			if (Clientthreads[i] != null && Clientthreads[i] != this
					&& Clientthreads[i].clientName != null
					&& Clientthreads[i].clientName.equals(name)) {
				getOutStream().println("Sorry, The name already exists");
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void run() {
		int maxClientsCount = ClientThread.maxClientsCount;
		ClientThread[] Clientthreads = ClientThread.Clientthreads;

		try {
			InStream = new DataInputStream(clientSocket.getInputStream());
			setOutStream((new PrintStream(clientSocket.getOutputStream())));
			String name;
			while (true) {
				getOutStream().println("Enter your name.");
				name = InStream.readLine().trim();
				if (name.indexOf('@') != -1) {
					getOutStream().println(
							"The name should not contain '@' character.");
				} else if (CheckName(name) != true) {
					break;
				}
			}

			getOutStream()
					.println(
							"Welcome "
									+ name
									+ " to our chat room.\n"
									+ "To see current members enter /Members in a new line.\n"
									+ "To send a private message enter @nameOfuser and then message.\n"
									+ "To send a private message to another server : \n" 
									+ "Enter @serverName + (nameOfuser) + message \n"
									+ "To leave enter /Bye in a new line.");
			
				for (int i = 0; i < maxClientsCount; i++) {
					if (Clientthreads[i] != null && Clientthreads[i] == this) {
						clientName = "@" + name;
						break;
					}
				}
				for (int i = 0; i < maxClientsCount; i++) {
					if (Clientthreads[i] != null && Clientthreads[i] != this) {
						Clientthreads[i].getOutStream().println(
								"*** A new user " + name
										+ " entered the chat room ***");
					}
				}
			// Start the conversation.
			while (true) {
				String line = InStream.readLine();
				if (line.startsWith("/Bye")) {
					break;
				}

				if (line.startsWith("/Members")) {
					getOutStream().println("Members are: ");
					for (int i = 0; i < maxClientsCount; i++) {
						if (Clientthreads[i] != null
								&& Clientthreads[i] != this
								&& Clientthreads[i].clientName != null
								&& Clientthreads[i] != this) {
							getOutStream().println("~ " + Clientthreads[i].clientName);
						}
					}
				}

				
				/* If the message is private sent it to the given client. */
				if (line.startsWith("@")) {
					String[] words = line.split("\\s", 2);
					String nameofuser = words[0];
					String text = words[1];
					if (nameofuser.length() > 1 && !text.isEmpty()) {
						boolean found = false;
						for (int i = 0; i < maxClientsCount; i++) {
							if (Clientthreads[i] != null
									&& Clientthreads[i] != this
									&& Clientthreads[i].clientName != null
									&& Clientthreads[i].clientName
											.equals(nameofuser)) {
								Clientthreads[i].getOutStream().println(
										"<" + name + "> " + text);
								
								this.getOutStream().println(
										">" + name + "> " + text);
								found = true;
								break;
							}
						}
						if (!found) {
							MultiThreadChatServer.sendToServer(line, name);
						}
					}
				} else if (!line.startsWith("/Members")) {

						for (int i = 0; i < maxClientsCount; i++) {
							if (Clientthreads[i] != null
									&& Clientthreads[i].clientName != null) {
								Clientthreads[i].getOutStream().println(
										"<" + name + "> " + line);
							}
						}
				}
			}
			

				for (int i = 0; i < maxClientsCount; i++) {
					if (Clientthreads[i] != null && Clientthreads[i] != this
							&& Clientthreads[i].clientName != null) {
						Clientthreads[i].getOutStream().println(
								"*** The user " + name
										+ " is leaving the chat room !!! ***");
					}
				}

			getOutStream().println("*** Bye " + name + " ***");

				for (int i = 0; i < maxClientsCount; i++) {
					if (Clientthreads[i] == this) {
						Clientthreads[i] = null;
					}
				}

			InStream.close();
			getOutStream().close();
			clientSocket.close();
		} catch (IOException e) {
		}
	}

	public static void Send(String line) {
		if (line.contains("<") && line.contains(">") && line.contains("(")
				&& line.contains(")")) {
			String name = "@"
					+ line.substring(line.indexOf("(") + 1, line.indexOf(")"));
			for (int i = 0; i < maxClientsCount; i++) {
				if (Clientthreads[i] != null
						&& Clientthreads[i].clientName != null
						&& Clientthreads[i].clientName.equals(name)) {
					String part1 = line.substring(0, line.indexOf(">") + 1);
					String part2 = line.substring(line.indexOf("["),
							line.indexOf("]") + 1);
					line = part1
							+ part2
							+ "  "
							+ line.substring(line.indexOf(")") + 1,
									line.indexOf("["));
					Clientthreads[i].getOutStream().println(line);
				}
			}
		}
	}

	
	public PrintStream getOutStream() {
		return OutStream;
	}

	public void setOutStream(PrintStream outStream) {
		OutStream = outStream;
	}

}
