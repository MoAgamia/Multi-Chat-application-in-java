import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class ServerThread extends Thread {
	private String ServerName = null;
	private DataInputStream InStream = null;
	private PrintStream OutStream = null;
	private Socket ServerSocket = null;
	private final ServerThread[] Serverthreads;
	private int maxServerCount;

	public ServerThread(Socket ServerSocket, ServerThread[] Sthreads) {
		this.ServerSocket = ServerSocket;
		this.Serverthreads = Sthreads;
		maxServerCount = Sthreads.length;
	}

	public boolean CheckName(String name) {
		name = "@" + name;
		for (int i = 0; i < maxServerCount; i++) {
			if (Serverthreads[i] != null && Serverthreads[i] != this
					&& Serverthreads[i].ServerName != null
					&& Serverthreads[i].ServerName.equals(name)) {
				OutStream.println("Sorry, The name already exists");
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void run() {
		int maxServerCount = this.maxServerCount;
		ServerThread[] Serverthreads = this.Serverthreads;

		try {
			InStream = new DataInputStream(ServerSocket.getInputStream());
			OutStream = new PrintStream(ServerSocket.getOutputStream());
			String name;

			while (true) {
				OutStream.println("Enter your name.");
				name = InStream.readLine().trim();
				if (name.indexOf('@') != -1) {
					OutStream
							.println("The name should not contain '@' character.");
				} else if (CheckName(name) != true) {
					break;
				}
			}

				for (int i = 0; i < maxServerCount; i++) {
					if (Serverthreads[i] != null && Serverthreads[i] == this) {
						ServerName = "@" + name;
						break;
					}
				}
				for (int i = 0; i < maxServerCount; i++) {
					if (Serverthreads[i] != null && Serverthreads[i] != this) {
						Serverthreads[i].OutStream.println("*** A new server "
								+ name + " entered the chat room !!! ***");
					}
				}

			// Start the conversation.
			while (true) {
				String line = InStream.readLine();
				if (line.startsWith("/Bye")) {
					break;
				}

				if (line.startsWith("/Members")) {
					OutStream.println("Members are: ");
					for (int i = 0; i < maxServerCount; i++) {
						if (Serverthreads[i] != null
								&& Serverthreads[i] != this
								&& Serverthreads[i].ServerName != null
								&& Serverthreads[i] != this) {
							OutStream.println(Serverthreads[i].ServerName);
						}
					}
				}

				if (line.startsWith("@")) {
					String[] words = line.split("\\s", 2);
					String nameofuser = words[0];
					String text = words[1];
					if (nameofuser.length() > 1 && !text.isEmpty()) {
						boolean found = false;
						for (int i = 0; i < maxServerCount; i++) {
							if (Serverthreads[i] != null
									&& Serverthreads[i] != this
									&& Serverthreads[i].ServerName != null
									&& Serverthreads[i].ServerName
											.equals(nameofuser)) {
								Serverthreads[i].OutStream.println(
										"<" + name + "> " + text);
								
								this.OutStream.println(
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

						for (int i = 0; i < maxServerCount; i++) {
							if (Serverthreads[i] != null
									&& Serverthreads[i].ServerName != null) {
								Serverthreads[i].OutStream.println("<" + name
										+ "> " + line);
							}
						}
				}
			}

				for (int i = 0; i < maxServerCount; i++) {
					if (Serverthreads[i] != null && Serverthreads[i] != this
							&& Serverthreads[i].ServerName != null) {
						Serverthreads[i].OutStream.println("*** The user "
								+ name + " is leaving the chat room !!! ***");
					}
				}

			OutStream.println("*** Bye " + name + " ***");

				for (int i = 0; i < maxServerCount; i++) {
					if (Serverthreads[i] == this) {
						Serverthreads[i] = null;
					}
				}


			InStream.close();
			OutStream.close();
			ServerSocket.close();
		} catch (IOException e) {
		}
	}
}
