package _serverV2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ServerReceiver extends Thread {
	private Socket socket = null;
	protected boolean loopdaloop = true;

    private volatile clientAndServer.PackToSendToServer packIn;
    //private clientAndServer.PackToSendToServer prevPackIn = null;
	
	public ServerReceiver(Socket socket) {
		super("EchoServerThread");
		this.socket = socket;
	}

	public void run() {

		try (
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		) {
			//clientID = socket.getRemoteSocketAddress().toString();

			while (loopdaloop){
                try {
                    packIn = (clientAndServer.PackToSendToServer)in.readObject();
                    ServerBrain.recPacks.addLast(packIn);
                    //System.out.print("Client ID = " + packIn.getPlayersId() + " " + packIn.getPlayersName()
                    //        + " press button: " + packIn.getPressedKey() + "\n");
                } catch (IOException | ClassNotFoundException e){
                    e.printStackTrace();
                    break;
                }
			}
		} catch (IOException e) {
			try {
				socket.close();
				System.out.println("Connection to client closed.");
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}

	}

	// zamykanie polaczenia
	public void closeSocket(){
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}