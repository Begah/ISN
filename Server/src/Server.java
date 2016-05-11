import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Server extends Thread {
	static final int PORT = 20016;
	
	public static boolean serverRunning = true;
	
	HashMap<Integer, Client> clients = new HashMap<Integer, Client>();
	int nextID = 0;
	
	public void run() {
		ServerSocket server = null;
		try {
			server = new ServerSocket(PORT);
			server.setSoTimeout(10000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(Inet4Address.getLocalHost());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		while(serverRunning) {
			System.out.println("Clients : " + String.valueOf(clients.size()));
			try {
	            Socket socket = server.accept();
	            System.out.println("Client connected");
	            Client cl = new Client(socket, nextID);
	            clients.put(nextID++, cl);
	            cl.start();
	         }catch(SocketTimeoutException s) {
	            continue;
	         }catch(IOException e) {
	            e.printStackTrace();
	            continue;
	         }
		}
		
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnected(int id) {
		Client client = clients.remove(id);
		
		Iterator<Entry<Integer, Client>> it = clients.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, Client> c = it.next();
			try {
				c.getValue().out.writeUTF("Disconnection");
				c.getValue().out.writeInt(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(client.playingWith != null) {
			client.playingWith.playingWith = null;
			client.playingWith.updatePlayingStatus();
		}
	}
	
	public void connected(int id) {
		Iterator<Entry<Integer, Client>> it = clients.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, Client> c = it.next();
			
			if(c.getKey() == id) // Make sure to not send it to the client who just connected
				continue;
			try {
				c.getValue().out.writeUTF("Connection");
				c.getValue().out.writeInt(id);
				c.getValue().out.writeUTF(clients.get(id).username);
				
				c.getValue().out.writeBoolean(false); // Upon connected, new client is no yet playing
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Server serverClass;
	
	public static void main(String[] args) {
		serverClass = new Server();
		serverClass.start();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverRunning = false;
	}
}
