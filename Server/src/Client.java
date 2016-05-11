import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map.Entry;

public class Client extends Thread {
	Socket socket;
	
	int ID;
	String username;
	Client playingWith = null;
	String gameName = null;
	
	DataOutputStream out;
	DataInputStream in;
	
	public Client(Socket socket, int ID) {
		this.socket = socket;
		this.ID = ID;
	}
	
	public void run() {
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			
			System.out.println("Waiting for username");
			
			String msg = in.readUTF();
			
			if(msg.equals("Username")) {
				username = in.readUTF();
				
				out.writeUTF("Accepted");
				
				Server.serverClass.connected(ID);
			} else {
				close();
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		Iterator<Entry<Integer, Client>> it = Server.serverClass.clients.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, Client> c = it.next();
			if(c.getValue().ID == ID)
				continue;
			try {
				out.writeUTF("Connection");
				out.writeInt(c.getValue().ID);
				out.writeUTF(c.getValue().username);
				
				out.writeBoolean(c.getValue().playingWith != null);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		while(socket.isClosed() == false) {
			try {
				String msg = in.readUTF();
				
				System.out.println(username + " : " + msg);
				
				switch(msg) {
				case "ping":
					break;
				case "Update Playing Status":
					boolean isPlaying = in.readBoolean();
					if(isPlaying == false && playingWith != null) {
						// Game just ended
						playingWith = null;
					}
					updatePlayingStatus();
					break;
				case "Game Request":
					int otherID = in.readInt();
					
					it = Server.serverClass.clients.entrySet().iterator();
					while(it.hasNext()) {
						Entry<Integer, Client> c = it.next();
						Client client = c.getValue();
						
						if(client.ID == otherID) {
							client.out.writeUTF("Game Request");
							client.out.writeInt(ID);
							client.out.flush();
							break;
						}
					}
					break;
				case "Game Request Answer":
					otherID = in.readInt();
					boolean answer = in.readBoolean();
					
					it = Server.serverClass.clients.entrySet().iterator();
					while(it.hasNext()) {
						Entry<Integer, Client> c = it.next();
						Client client = c.getValue();
						
						if(client.ID == otherID) {
							client.out.writeUTF("Game Request Answer");
							client.out.writeBoolean(answer);
							client.out.flush();
							
							if(answer == true) {
								// They started a game
								this.playingWith = client;
								client.playingWith = this;
								updatePlayingStatus();
							}
						}
					}
					break;
				case "Pick Game":
					playingWith.out.writeUTF(msg);
					playingWith.out.writeBoolean(in.readBoolean());
					playingWith.out.flush();
					break;
				case "Game Chosen":
					gameName = in.readUTF();
					playingWith.out.writeUTF(msg);
					playingWith.out.writeUTF(gameName);
					playingWith.out.flush();
					break;
				case "Pendu Mot Choisit":
					String motChoisit = in.readUTF();
					playingWith.out.writeUTF(msg);
					playingWith.out.writeUTF(motChoisit);
					playingWith.out.flush();
					break;
				case "Pendu Joueur Fini":
					boolean completed = in.readBoolean();
					playingWith.out.writeUTF(msg);
					playingWith.out.writeBoolean(completed);
					playingWith.out.flush();
					break;
				case "Pendu Points":
					playingWith.out.writeUTF(msg);
					playingWith.out.writeInt(in.readInt());
					playingWith.out.writeInt(in.readInt());
					playingWith.out.flush();
					break;
				case "Battleship Boat Placed":
					playingWith.out.writeUTF(msg);
					playingWith.out.flush();
					break;
				case "Battleship Boat Description":
					playingWith.out.writeUTF(msg);
					playingWith.out.writeInt(in.readInt());
					playingWith.out.writeInt(in.readInt());
					playingWith.out.writeInt(in.readInt());
					playingWith.out.writeInt(in.readInt());
					playingWith.out.flush();
					break;
				case "Battleship turn to play":
					playingWith.out.writeUTF(msg);
					playingWith.out.writeBoolean(in.readBoolean());
					playingWith.out.flush();
					break;
				case "Battleship CellBombed":
					playingWith.out.writeUTF(msg);
					playingWith.out.writeInt(in.readInt());
					playingWith.out.writeInt(in.readInt());
					playingWith.out.flush();
					break;
				case "Battleship Player won":
					playingWith.out.writeUTF(msg);
					playingWith.out.writeBoolean(in.readBoolean());
					playingWith.out.flush();
					break;
				case "Puissance4 place jeton":
					playingWith.out.writeUTF(msg);
					playingWith.out.writeInt(in.readInt());
					playingWith.out.writeInt(in.readInt());
					playingWith.out.flush();
					break;
				case "Puissance4 Gagnant":
					playingWith.out.writeUTF(msg);
					playingWith.out.writeInt(in.readInt());
					playingWith.out.flush();
					break;
				}
			} catch (IOException e) {
			}
			
			try {
				out.writeUTF("ping");
				out.flush();
			} catch (IOException e) {
				break;
			}
		}
		
		close();
	}
	
	public void updatePlayingStatus() {
		Iterator<Entry<Integer, Client>> it = Server.serverClass.clients.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, Client> c = it.next();
			if(c.getValue() == this)
				continue;
			
			Client client = c.getValue();
			try {
				client.out.writeUTF("Update Playing Status");
				client.out.writeInt(ID);
				client.out.writeBoolean(playingWith != null);
				client.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		try {
			socket.close();
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Server.serverClass.disconnected(ID);
	}
}
