package com.socket;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.ui.ChatFrame;

public class SocketClient implements Runnable {

	public int port;
	public String serverAddr;
	public Socket socket;
	public ChatFrame ui;
	public ObjectInputStream In;
	public ObjectOutputStream Out;
	public int checkIP = 0;
	public int avoidIP = 0;

	// public History hist;

	public SocketClient(ChatFrame frame) throws IOException {
		ui = frame;
		this.serverAddr = ui.serverAddr;
		this.port = ui.port;
		socket = new Socket(InetAddress.getByName(serverAddr), port);

		Out = new ObjectOutputStream(socket.getOutputStream());
		Out.flush();
		In = new ObjectInputStream(socket.getInputStream());

		// hist = ui.hist;
	}

	// ***************************************************************************/

	public static DB db = new DB();

	private void sqlprocess(String sql_IP) throws IOException {
		try {
			String sql = sql_IP.trim().substring(2);
			int index1 = sql.indexOf(" ");
			int value_id = Integer.parseInt(sql.substring(0, index1));
			int index2 = sql.indexOf(" ", index1 + 1);
			int entity = Integer.parseInt(sql.substring(index1 + 1, index2));
			int index3 = sql.indexOf(" ", index2 + 1);
			int attribute = Integer.parseInt(sql.substring(index2 + 1, index3));
			int index4 = sql.indexOf(" ", index3 + 1);
			int store_id = Integer.parseInt(sql.substring(index3 + 1, index4));
			int index5 = sql.indexOf(" ", index4 + 1);
			int entity_id_value = Integer.parseInt(sql.substring(index4 + 1,
					index5));
			int value = Integer.parseInt(sql.substring(index5 + 1));
			String query = "INSERT INTO  SampleDDB.demo VALUES "
					+ "(?,?,?,?,?,?)";
			PreparedStatement stmt = db.conn.prepareStatement(query,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, value_id);
			stmt.setInt(2, entity);
			stmt.setInt(3, attribute);
			stmt.setInt(4, store_id);
			stmt.setInt(5, entity_id_value);
			stmt.setInt(6, value);
			stmt.executeUpdate();
			// ChatFrame.successQuery = 1;
		} catch (SQLException e) {
			System.out.println("Can't be inserted, Wrong Format!");
			e.printStackTrace();
		}
	}

	// *******************************************************************************
	public int c = 0;

	private void sqlexecute(String sql, String sndr, String rcvr)
			throws IOException {
		try {
			// if(!sql.substring(0, 6).equalsIgnoreCase("select")){
			java.sql.PreparedStatement stmt = db.conn.prepareStatement(sql,
					Statement.RETURN_GENERATED_KEYS);
			stmt.execute();
			// }
			// else{
			ResultSet rs = db.runSql(sql);
			if (rs.next()) {
				do {
					int r1 = rs.getInt("value_id");
					int r2 = rs.getInt("entity");
					int r3 = rs.getInt("attribute");
					int r4 = rs.getInt("store_id");
					int r5 = rs.getInt("entity_id_value");
					int r6 = rs.getInt("value");
					String sends = "r:" + r1 + " " + r2 + " " + r3 + " " + r4
							+ " " + r5 + " " + r6 + " ".trim();
					System.out.println(sends);
					Message ms = new Message("message", rcvr, sends, sndr);
					send(ms);
				} while (rs.next());
			} else {
				if (rcvr != "Suvam") {
					Message ms = new Message("message", rcvr, "empty", sndr);
					send(ms);
				} else {
					ui.jTextArea1
							.append("["
									+ rcvr
									+ " > Me] : "
									+ "Hey, have a nice day!"
									+ "\n");
				}
			}
			// }
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// *****************************************************************************/
	public String ipName = "";

	@Override
	public void run() {
		boolean keepRunning = true;
		while (keepRunning) {
			try {
				int count = 0;
				Message msg = (Message) In.readObject();
				System.out.println("Incoming : " + msg.toString());
				// System.out.println(msg.content.toString());
				if (msg.type.equals("_ipconfig")) {
					StringBuilder strb = new StringBuilder("");
					String tmp = msg.content.toString().trim();
					String[] ipList = new String[50];
					String[] recpName = new String[50];
					int flag1 = 1;
					int flag2 = 1;
					int y = 0;
					for (int i = 0; i < tmp.length(); i++) {
						if (tmp.charAt(i) == ':') {
							if (flag1 == 1) {
								flag1 = 0;
							} else {
								flag1 = 1;
								ipList[y] = "http://" + strb.toString();
								strb.setLength(0);
							}
						} else if (flag1 == 0) {
							strb.append(tmp.charAt(i));
						}
						if (tmp.charAt(i) == '*' && flag1 == 1) {
							if (flag2 == 1) {
								flag2 = 0;
							} else {
								flag2 = 1;
								recpName[y++] = strb.toString();
								strb.setLength(0);
							}
						} else if (flag1 == 1 && tmp.charAt(i) != ':') {
							strb.append(tmp.charAt(i));
						}
					}
					int ipIndex = 0;
					if (checkIP == 0) {
						checkIP = 1;
						avoidIP = y - 1;
					}
					for (int i = 0; i < y; i++) {
						if (i != avoidIP) {
							String url = ipList[i];
							ipIndex = getStatus(url, i);
						}
						// System.out.println(url + "\t\tStatus:" + status);
					}
					ipName = recpName[ipIndex];
				}
				if (msg.type.equals("message")) {
					if (msg.recipient.equals(ui.username)) {
						ui.jTextArea1.append("[" + msg.sender + " > Me] : "
								+ msg.content + "\n");
						if (msg.content.indexOf("empty") != -1) {
							try {
								if (c == 0) {
									c++;
									Message ms1 = new Message("message",
											msg.recipient, ui.buffer,
											"Suvam");
									send(ms1);
								}
							} catch (Exception e) {
								System.out.println("Empty received wrongly");
							}
							// sqlexecute(buffer,msg.sender,"suvam");
						} else if (msg.content.substring(0, 2).toLowerCase()
								.equals("r:")) {
							try {
								ui.buffer = msg.content;
								LineIterator it = FileUtils
										.lineIterator(ui.writeQuery);
								try {
									while (it.hasNext()) {
										String line = it.nextLine().toString();
										// System.out.println(ui.buffer1);
										if (line.equalsIgnoreCase(ui.buffer1)) {
											count++;
										}// System.out.print("  "+count);
										if (count > 3) {
											sqlprocess(msg.content);
											break;
										}
									}
								} finally {
									count = 0;
									it.close();
								}

							} catch (Exception e) {
								System.out
										.println("Empty received insert query");
							}
						} else if (msg.content.indexOf("select") == 0) {
							try {
								sqlexecute(msg.content, msg.sender,
										msg.recipient);
							} catch (Exception e) {
								System.out
										.println("Empty received select query");
							}
						}/*
						 * else { Message ms= new
						 * Message("message",msg.recipient
						 * ,msg.content,msg.sender); send(ms); }
						 */
					} else {

						ui.jTextArea1.append("[" + msg.sender + " > "
								+ msg.recipient + "] : " + msg.content + "\n");
					}

					/*
					 * if(!msg.content.equals(".bye") &&
					 * !msg.sender.equals(ui.username)){ String msgTime = (new
					 * Date()).toString();
					 * 
					 * try{ hist.addMessage(msg, msgTime); DefaultTableModel
					 * table = (DefaultTableModel)
					 * ui.historyFrame.jTable1.getModel(); table.addRow(new
					 * Object[]{msg.sender, msg.content, "Me", msgTime}); }
					 * catch(Exception ex){} }
					 */
				} else if (msg.type.equals("login")) {
					if (msg.content.equals("TRUE")) {
						ui.jButton2.setEnabled(false);
						ui.jButton3.setEnabled(false);
						ui.jButton4.setEnabled(true);
						ui.jButton5.setEnabled(true);
						ui.jTextArea1
								.append("[SERVER > Me] : Login Successful\n");
						ui.jTextField3.setEnabled(false);
						ui.jPasswordField1.setEnabled(false);
					} else {
						ui.jTextArea1.append("[SERVER > Me] : Login Failed\n");
					}
				} else if (msg.type.equals("test")) {
					ui.jButton1.setEnabled(false);
					ui.jButton2.setEnabled(true);
					ui.jButton3.setEnabled(true);
					ui.jTextField3.setEnabled(true);
					ui.jPasswordField1.setEnabled(true);
					ui.jTextField1.setEditable(false);
					ui.jTextField2.setEditable(false);
					// ui.jButton7.setEnabled(true);
				} else if (msg.type.equals("newuser")) {
					if (!msg.content.equals(ui.username)) {
						boolean exists = false;
						for (int i = 0; i < ui.model.getSize(); i++) {
							if (ui.model.getElementAt(i).equals(msg.content)) {
								exists = true;
								break;
							}
						}
						if (!exists) {
							ui.model.addElement(msg.content);
						}
					}
				} else if (msg.type.equals("_ipconfig")) {
					if (msg.content != null) {
						ui.jTextArea1
								.append("[SERVER > Me] : Connected to a high speed server @ Server:_$$"
										+ ipName + "%$\n");
					} else {
						ui.jTextArea1.append("[SERVER > Me] : Signup Failed\n");
					}
				} else if (msg.type.equals("signup")) {
					if (msg.content.equals("TRUE")) {
						ui.jButton2.setEnabled(false);
						ui.jButton3.setEnabled(false);
						ui.jButton4.setEnabled(true);
						ui.jButton5.setEnabled(true);
						ui.jTextArea1
								.append("[SERVER > Me] : Singup Successful\n");
					} else {
						ui.jTextArea1.append("[SERVER > Me] : Signup Failed\n");
					}
				} else if (msg.type.equals("signout")) {
					if (msg.content.equals(ui.username)) {
						ui.jTextArea1.append("[" + msg.sender
								+ " > Me] : Bye\n");
						ui.jButton1.setEnabled(true);
						ui.jButton4.setEnabled(false);
						ui.jTextField1.setEditable(true);
						ui.jTextField2.setEditable(true);

						for (int i = 1; i < ui.model.size(); i++) {
							ui.model.removeElementAt(i);
						}

						ui.clientThread.stop();
					} else {
						ui.model.removeElement(msg.content);
						ui.jTextArea1.append("[" + msg.sender + " > All] : "
								+ msg.content + " has signed out\n");
					}
				} else if (msg.type.equals("upload_req")) {

					if (JOptionPane.showConfirmDialog(ui, ("Accept '"
							+ msg.content + "' from " + msg.sender + " ?")) == 0) {

						JFileChooser jf = new JFileChooser();
						jf.setSelectedFile(new File(msg.content));
						int returnVal = jf.showSaveDialog(ui);

						String saveTo = jf.getSelectedFile().getPath();
						if (saveTo != null
								&& returnVal == JFileChooser.APPROVE_OPTION) {
							Download dwn = new Download(saveTo, ui);
							Thread t = new Thread(dwn);
							t.start();
							// send(new Message("upload_res",
							// (""+InetAddress.getLocalHost().getHostAddress()),
							// (""+dwn.port), msg.sender));
							send(new Message("upload_res", ui.username,
									("" + dwn.port), msg.sender));
						} else {
							send(new Message("upload_res", ui.username, "NO",
									msg.sender));
						}
					} else {
						send(new Message("upload_res", ui.username, "NO",
								msg.sender));
					}
				} else if (msg.type.equals("upload_res")) {
					if (!msg.content.equals("NO")) {
						int port = Integer.parseInt(msg.content);
						String addr = msg.sender;

						ui.jButton5.setEnabled(false);
						ui.jButton6.setEnabled(false);
						Upload upl = new Upload(addr, port, ui.file, ui);
						Thread t = new Thread(upl);
						t.start();
					} else {
						ui.jTextArea1.append("[SERVER > Me] : " + msg.sender
								+ " rejected file request\n");
					}
				} else {
					ui.jTextArea1
							.append("[SERVER > Me] : Unknown message type\n");
				}
			} catch (Exception ex) {
				// keepRunning = false;
				ui.jTextArea1
						.append("[Application > Me] : Connection Failure\n");
				/*
				 * ui.jButton1.setEnabled(true);
				 * ui.jTextField1.setEditable(true);
				 * ui.jTextField2.setEditable(true);
				 * ui.jButton4.setEnabled(false); ui.jButton5.setEnabled(false);
				 * ui.jButton5.setEnabled(false);
				 * 
				 * for(int i = 1; i < ui.model.size(); i++){
				 * ui.model.removeElementAt(i); }
				 * 
				 * ui.clientThread.stop();
				 * 
				 * System.out.println("Exception SocketClient run()");
				 * ex.printStackTrace();
				 */
			}
		}
	}

	int minIndex = 0;
	long minTime = Long.MAX_VALUE;
	long currentTime = 0;

	private int getStatus(String url, int index) {
		// String result = "";
		try {
			currentTime = System.currentTimeMillis();
			URL siteURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) siteURL
					.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			int code = connection.getResponseCode();
			// System.out.println(code);
			currentTime = System.currentTimeMillis() - currentTime;
			// if (code == 200) {
			// result = "Ping Time = " + currentTime;
			// }
		} catch (Exception e) {
			// result = "Ping Failed!";
		}
		if (currentTime < minTime) {
			minTime = currentTime;
			minIndex = index;
			return minIndex;
		} else
			return minIndex;
	}

	public void send(Message msg) {
		try {
			Out.writeObject(msg);
			Out.flush();
			System.out.println("Outgoing : " + msg.toString());

			/*
			 * if(msg.type.equals("message") && !msg.content.equals(".bye")){
			 * String msgTime = (new Date()).toString(); try{
			 * hist.addMessage(msg, msgTime); DefaultTableModel table =
			 * (DefaultTableModel) ui.historyFrame.jTable1.getModel();
			 * table.addRow(new Object[]{"Me", msg.content, msg.recipient,
			 * msgTime}); } catch(Exception ex){} }
			 */
		} catch (IOException ex) {
			System.out.println("Exception SocketClient send()");
		}
	}

	public void closeThread(Thread t) {
		t = null;
	}
}
