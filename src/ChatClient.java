import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class ChatClient {

	static class AccessToServer extends Observable {
		private Socket Serversocket;
		private OutputStream ThisServeroutputStream;

		public void notifyObservers(Object arg) {
			super.setChanged();
			super.notifyObservers(arg);
		}

		public AccessToServer(String server, int port) throws IOException {
			Serversocket = new Socket(server, port);
			ThisServeroutputStream = Serversocket.getOutputStream();

			Thread receivingThread = new Thread() {
				@Override
				public void run() {
					try {
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(
										Serversocket.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null)
							notifyObservers(line);
					} catch (IOException ex) {
						notifyObservers(ex);
					}
				}
			};
			receivingThread.start();
		}

		public void send(String text) {
			try {
				ThisServeroutputStream.write((text + "\r\n").getBytes());
				ThisServeroutputStream.flush();
			} catch (IOException ex) {
				notifyObservers(ex);
			}
		}

		public void close() {
			try {
				Serversocket.close();
			} catch (IOException ex) {
				notifyObservers(ex);
			}
		}
	}

	@SuppressWarnings("serial")
	static class ChatFrame extends JFrame implements Observer {

		private JTextArea textArea;
		private JTextField inputTextField;
		private JButton sendButton;
		private AccessToServer chatA;

		public ChatFrame(AccessToServer chatAccess) {
			this.chatA = chatAccess;
			chatAccess.addObserver(this);
			GUI();
		}

		private void GUI() {
			textArea = new JTextArea(20, 50);
			DefaultCaret caret = (DefaultCaret) textArea.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setText("Please type your Name \n");
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
						chatA.send(str);
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
					chatA.close();
				}
			});
		}

		public void update(Observable o, Object arg) {
			final Object finalArg = arg;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					textArea.append(finalArg.toString());
					textArea.append("\n");
				}
			});
		}
	}

	public static void main(String[] args, int ServerNumber) {
		String server = args[0];
		int port;
		if (ServerNumber == 0) {
			port = 6001;
		} else if (ServerNumber == 1) {
			port = 6002;
		} else if (ServerNumber == 2) {
			port = 6003;
		} else {
			port = 6004;
		}
		AccessToServer access = null;
		try {
			access = new AccessToServer(server, port);
		} catch (IOException ex) {
			System.out.println("Cannot connect to " + server + ":" + port);
			ex.printStackTrace();
			System.exit(0);
		}
		JFrame frame = new ChatFrame(access);
		frame.setTitle("ChatApp Client- connected to " + server + ":" + port);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}
}