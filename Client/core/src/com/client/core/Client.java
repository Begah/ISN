package com.client.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread {
	static final int PORT = 20016;
	
	Socket socket;
	private OutputStream OS;
	public DataOutputStream out;
	private InputStream IS;
	public DataInputStream in;	
	
	private String username;
	
	public ArrayList<OtherClient> otherClients = new ArrayList<OtherClient>();
	
	public static enum State {
		Connected,
		Connecting,
		FailedToConnect,
		ClosedConnection
	}
	
	public State state = State.Connecting;
	
	public Client(String username) {
		this.username = username;
	}
	
	@Override
	public void run() {
		try { //  InetAddress.getLocalHost().getHostName()
			socket = new Socket(InetAddress.getByName("matrouxclient.ddns.net").getHostAddress(), PORT);

			OS = socket.getOutputStream();
			out = new DataOutputStream(OS);
			
			IS = socket.getInputStream();
			in = new DataInputStream(IS);
			
			out.writeUTF("Username");
			out.writeUTF(username);
			out.flush();
			
			String s = null;
			try {
				s = in.readUTF();
			} catch(EOFException e) {
				e.printStackTrace();
				state = State.FailedToConnect;
				close();
				return;
			}
			
			if(!s.equals("Accepted")) {
				System.out.println("Could not autenticate to server");
				state = State.FailedToConnect;
				close();
				return;
			}
		} catch (IOException e) {
			state = State.FailedToConnect;
			e.printStackTrace();
			return;
		}
		
		state = State.Connected;
		
		while(socket.isConnected()) {
			try {
				String str = in.readUTF();
				
				if(str.equals("ping")) {
				} else if(str.equals("Connection")) {
					int id = in.readInt(); String username = in.readUTF(); boolean isPlaying = in.readBoolean();
					
					OtherClient oClient;
					otherClients.add(oClient = new OtherClient(id, username, isPlaying));
					MainClass.getCurrentScene().connection(oClient);
				} else if(str.equals("Disconnection")) {
					int id = in.readInt();
					OtherClient oClient = null;
					for(int i = 0; i < otherClients.size(); i++) {
						if(otherClients.get(i).ID == id) {
							oClient = otherClients.remove(i);
							break;
						}
					}
					MainClass.getCurrentScene().disconnection(oClient);
				} else if(str.equals("Update Playing Status")) {
					int id = in.readInt();
					boolean isPlaying = in.readBoolean();
					OtherClient oClient = null;
					
					for(int i = 0; i < otherClients.size(); i++) {
						if(otherClients.get(i).ID == id) {
							oClient = otherClients.get(i);
							oClient.isPlaying = isPlaying;
							break;
						}
					}
					MainClass.getCurrentScene().clientUpdated(oClient);
				} else {
					System.out.println(str);
					MainClass.getCurrentScene().event(str);
				}
			} catch (IOException e) {
			}
		}
		close();
		state = State.ClosedConnection;
	}
	
	public int readInt() {
		int value = 0;
		try {
			value = in.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public float readFloat() {
		float value = 0;
		try {
			value = in.readFloat();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public double readDouble() {
		double value = 0;
		try {
			value = in.readDouble();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public boolean readBool() {
		boolean value = false;
		try {
			value = in.readBoolean();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public String readString() {
		String value = "";
		try {
			value = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public void sendInt(int value) {
		try {
			out.writeInt(value);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendFloat(float value) {
		try {
			out.writeFloat(value);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendDouble(double value) {
		try {
			out.writeDouble(value);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendBool(boolean value) {
		try {
			out.writeBoolean(value);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendString(String value) {
		try {
			out.writeUTF(value);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void close() {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			OS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			IS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getUsername() {
		return username;
	}
}
