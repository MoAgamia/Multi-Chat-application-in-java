import java.io.IOException;

import javax.swing.JOptionPane;

public class MainMethod {

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException {

		Object[] selectSoC = { "Servers", "Clients" };
		Object[] selectSValues = { "Server1", "Server2", "Server3", "Server4" };
		Object[] selectCValues = { "Client1", "Client2", "Client3", "Client4" };
		String initialSoC = "Servers";
		String initialS = "Server";
		String initialC = "Client";

		Object selection = JOptionPane.showInputDialog(null, "Login as Server or Client : ",
				"ChatApp", JOptionPane.QUESTION_MESSAGE, null, selectSoC,
				initialSoC);
		if (selection.equals("Servers")) {
			Object selection1 = JOptionPane.showInputDialog(null,
					"Login as : ", "ChatApp", JOptionPane.QUESTION_MESSAGE,
					null, selectSValues, initialS);

			// Servers
			if (selection1.equals("Server1")) {
				String[] arguments = new String[] { "localhost" };
				new MultiThreadChatServer().main(arguments, 0);

			} else if (selection1.equals("Server2")) {
				String[] arguments = new String[] { "localhost" };
				new MultiThreadChatServer().main(arguments, 1);

			} else if (selection1.equals("Server3")) {
				String[] arguments = new String[] { "localhost" };
				new MultiThreadChatServer().main(arguments, 2);

			} else if (selection1.equals("Server4")) {
				String[] arguments = new String[] { "localhost" };
				new MultiThreadChatServer().main(arguments, 3);

			}
		} else if (selection.equals("Clients")) {

			// Clients

			Object selection2 = JOptionPane.showInputDialog(null,
					"Login as : ", "ChatApp", JOptionPane.QUESTION_MESSAGE,
					null, selectCValues, initialC);

			if (selection2.equals("Client1")) {
				String IPServer = JOptionPane
						.showInputDialog("Enter the Server ip address");
				String[] arguments = new String[] { IPServer };
				new ChatClient().main(arguments, 0);
			} else if (selection2.equals("Client2")) {
				String IPServer = JOptionPane
						.showInputDialog("Enter the Server ip address");
				String[] arguments = new String[] { IPServer };
				new ChatClient().main(arguments, 1);
			} else if (selection2.equals("Client3")) {
				String IPServer = JOptionPane
						.showInputDialog("Enter the Server ip address");
				String[] arguments = new String[] { IPServer };
				new ChatClient().main(arguments, 2);
			} else if (selection2.equals("Client4")) {
				String IPServer = JOptionPane
						.showInputDialog("Enter the Server ip address");
				String[] arguments = new String[] { IPServer };
				new ChatClient().main(arguments, 3);
			}
		}
	}
}
