package _clientV2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientReceiver extends Thread {
	private boolean broadcastListening = true;
    String hostName;
    int portNumber;

	public ClientReceiver(String hostName, int portNumber) throws IOException {
		super("Dawac dane");
        this.hostName = hostName;
        this.portNumber = portNumber;
	}

	public void run() {
        int receivedPackagesCounter = 0;
		try (Socket brcSocket = new Socket(hostName, portNumber);
				ObjectInputStream in = new ObjectInputStream(brcSocket.getInputStream());

		) {
			while (broadcastListening) { try {
				_clientV2.ClientBrain.recPac = (clientAndServer.PackReceivedFromServer)in.readObject();
                _clientV2.ClientBrain.writeToBufConnClients(ClientBrain.recPac.getConnectedClients(),
                        ClientBrain.recPac.getNotConnectedClients());
                receivedPackagesCounter++; }

                catch (EOFException e) {
                        System.err.println("EOF");
            //			data = null;
                        broadcastListening = false;
                }
                 catch (NullPointerException | ClassNotFoundException e) {
                    //receive data
                         e.printStackTrace();
		        }
                /*System.out.print(_clientV2.ClientBrain.recPac.getAdditionalInfo() + "\t\t Odebrano pakietów: "
                        + receivedPackagesCounter + "\t\tConnected Clients: ");
                for (int i = 0; i < _clientV2.ClientBrain.recPac.getConnectedClients().size(); i++){
                    System.out.print(_clientV2.ClientBrain.recPac.getConnectedClients().get(i));
                }
                System.out.print(" Not connected clientes: "
                        + _clientV2.ClientBrain.recPac.getNotConnectedClients() + "\n");*/
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName + "at BrcCl");
			//System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + hostName + " at BrcCl");
//			data = null;
			broadcastListening = false;
		}
		System.out.println("BREC ENDING");

	}
}

