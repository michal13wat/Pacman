package _serverV2;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerSender extends Thread {

	private Socket socket = null;
	protected boolean loopdaloop = true;
	private final int DELAY = 1000/60;//1;
    private int thradID;

	
	public ServerSender(Socket socket, int thradID) {
		super("ServerSender");
		this.socket = socket;
        this.thradID = thradID;
	}

	public void run() {
		try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     //BufferedOutputStream bos = new BufferedOutputStream(out);
                        ) 
		{
                    int pseudoTimer = 0;
                    System.out.println("ServerSender in thread " + thradID + " estabilished.");
		while (loopdaloop) {
                    //out.flush();
                    /*if(!ServerBrain.checkIfPackWasSendByThisThread(thradID)){
                        out.reset();

                        //ServerBrain.packOut.setAdditionalInfo(temp);
                        //bos.writeObject(ServerBrain.packOut);
                        out.writeUnshared(ServerBrain.packOut);
                        //Thread.sleep(5);
                        out.flush();
                        ServerBrain.lockBufferingToSendByThisThread(thradID);
                        ServerBrain.thisThreadReadPackToSend(thradID);  // readPrevPack = false;
                }*/
                //////////////////////////////////////////////////////////////////
                //      UWGAA - POD ŻADNYM POZOREM NIE WYWALAĆ STĄD             //
                //      PONIŻSZEGO IF-A. JEST ON ODPOWIEDZILANY                 //
                //      ZA WYSŁANIE CO 3sek. DANYCH DO KLIENTÓW                 //
                //      KIEDY WSZYCY KLIENCI NIE SĄ PODŁĄCZENI                  //
                //////////////////////////////////////////////////////////////////
                if(!ServerBrain.checkIfAllClientsAreConnected()){//) && pseudoTimer > 1000/60){
                    out.reset();

                    ServerBrain.packOut.setNotConnectedClients(ServerBrain.notConnectedClients);
                    ServerBrain.packOut.setConnectedClients(ServerBrain.connectedClients);
                    if(ServerBrain.bytesOut != null){
                        // TODO - odkomentować linijke poniżej !!!
                        //out.write(ServerBrain.bytesOut);
                        out.writeUnshared(ServerBrain.packOut);
                    }else {
                        System.out.println("=========  Pusty bufor z danymi wyjściowymi!!!  =========");
                    }
                    //Thread.sleep(5);
                    out.flush();
                    pseudoTimer = 0;
                }
				Thread.sleep(DELAY);
                //pseudoTimer++;
			}

		} catch (IOException e) {
			try {
				socket.close();
				System.out.println("ServerSender in thread: " + thradID + " stopped: " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void closeSocket(){
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}