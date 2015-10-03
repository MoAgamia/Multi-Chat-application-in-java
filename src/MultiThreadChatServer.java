import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

public class MultiThreadChatServer extends Thread {

	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	private static final int maxClientsCount = 10;
	private static final ClientThread[] Clientthreads = new ClientThread[maxClientsCount];

	static class AccessToServer extends Observable {
		private Socket ThisServersocket;
		private Socket Mastersocket;
		private OutputStream ThisServeroutputStream;
		private OutputStream MasteroutputStream;

		public void notifyObservers(Object arg) {
			super.setChanged();
			super.notifyObservers(arg);
		}

		public AccessToServer(String server, int thisport) throws IOException {
			ThisServersocket = new Socket(server, thisport);
			Mastersocket = new Socket(server, 6000);
			ThisServeroutputStream = ThisServersocket.getOutputStream();
			MasteroutputStream = Mastersocket.getOutputStream();

			Thread receivingThread = new Thread() {
				public void run() {
					try {
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(ThisServersocket.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null)
							notifyObservers(line);

					} catch (IOException ex) {
						notifyObservers(ex);
					}
				}
			};
			receivingThread.start();

			Thread OtherreceivingThread = new Thread() {
				public void run() {
					try {
						BufferedReader reader2 = new BufferedReader(
								new InputStreamReader(Mastersocket.getInputStream()));
						String line2;
						while ((line2 = reader2.readLine()) != null) {
							notifyObservers(line2);
							ClientThread.Send(line2);
						}
					} catch (IOException ex) {
						notifyObservers(ex);
					}
				}
			};
			OtherreceivingThread.start();
		}

		public void send(String text) {
			try {
				if (ThisServeroutputStream != null) {
					ThisServeroutputStream.write((text + "\r\n").getBytes());
					ThisServeroutputStream.flush();
				}
				if (MasteroutputStream != null) {
					MasteroutputStream.write((text + "\r\n").getBytes());
					MasteroutputStream.flush();
				}
			} catch (IOException ex) {
				notifyObservers(ex);
			}
		}

		public void close() {
			try {
				ThisServersocket.close();
				Mastersocket.close();
			} catch (IOException ex) {
				notifyObservers(ex);
			}
		}
	}

	@SuppressWarnings("serial")
	static class ChatFrame extends JFrame implements Observer {

		private JTextArea textArea;
		private static JTextField inputTextField;
		private static JButton sendButton;
		private AccessToServer ATS;

		public ChatFrame(AccessToServer chatAccess) {
			this.ATS = chatAccess;
			chatAccess.addObserver(this);
			GUI();
		}

		public static JButton getSendButton() {
			return sendButton;
		}

		public static JTextField getJTF() {
			return inputTextField;
		}

		private void GUI() {
			textArea = new JTextArea(20, 50);
			DefaultCaret caret = (DefaultCaret) textArea.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			add(new JScrollPane(textArea), BorderLayout.CENTER);

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.SOUTH);
			inputTextField = new JTextField();
			sendButton = new JButton("Send");
			box.add(inputTextField);
			box.add(sendButton);

			ActionListener sendListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String str = inputTextField.getText();
					if (str != null && str.trim().length() > 0)
						ATS.send(str);
					inputTextField.selectAll();
					inputTextField.requestFocus();
					inputTextField.setText("");
				}
			};
			inputTextField.addActionListener(sendListener);
			sendButton.addActionListener(sendListener);

			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					ATS.close();
				}
			});

		}

		public void update(Observable o, Object arg) {
			final Object finalArg = arg;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (finalArg.toString() != null && textArea != null) {
						textArea.append(finalArg.toString());
						textArea.append("\n");
					}
				}
			});
		}
	}

	public static void sendToServer(String line, String name) {
		line = line + "[from :  " + name + "]";
		ChatFrame.getJTF().setText(line);
		ChatFrame.getSendButton().doClick();
	}
	
	
	public static void main(String args[], int ServerNumber) throws IOException {

		// The default port number.
		int portNumber;
		String ServerName;
		if (ServerNumber == 0) {
			portNumber = 6001;
			ServerName = "Server1";
		} else if (ServerNumber == 1) {
			portNumber = 6002;
			ServerName = "Server2";
		} else if (ServerNumber == 2) {
			portNumber = 6003;
			ServerName = "Server3";
		} else {
			portNumber = 6004;
			ServerName = "Server4";
		}
		if (args.length < 2) {
			System.out
					.println("Usage: java MultiThreadChatServer <portNumber>\n"
							+ "Now using port number=" + portNumber);
		} else {
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		// client for server
		String server = args[0];
		AccessToServer access = null;
		try {
			access = new AccessToServer(server, portNumber);
		} catch (IOException ex) {
			System.out
					.println("Cannot connect to " + server + ":" + portNumber);
			ex.printStackTrace();
			System.exit(0);
		}
		JFrame frame = new ChatFrame(access);
		frame.setTitle("ChatApp " + ServerName + " - connected to " + server + ":"
				+ portNumber);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		// end

		while (true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxClientsCount; i++) {
					if (Clientthreads[i] == null) {
						(Clientthreads[i] = new ClientThread(clientSocket, Clientthreads))
								.start();
						break;
					}
				}
				if (i == maxClientsCount) {
					PrintStream os = new PrintStream(
							clientSocket.getOutputStream());
					os.println("Server too busy. Try later.");
					os.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}

	}

}