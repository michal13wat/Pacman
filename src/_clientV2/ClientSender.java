package _clientV2;

import clientAndServer.PackToSendToServer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSender extends Thread {
	volatile String hostName = null;
	int portNumber = -1;
    private volatile PackToSendToServer packOut = null;
    private clientAndServer.PackToSendToServer prevPackOut = null;

	public ClientSender(String hostName, int portNumber, PackToSendToServer packOut) {
		this.hostName = hostName;
		this.portNumber = portNumber;
        this.packOut = packOut;
	}

	
	public void run() {
		try (Socket socket = new Socket(hostName, portNumber);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		) {
            //System.out.println("Wątek wysłający klienta podłączony do adresu: " + hostName);
            while (true){
                Thread.sleep(5*1000/60);
                if(!packOut.isEquals(prevPackOut)){
                    out.flush();
                    prevPackOut = packOut.copy();
                    out.writeUnshared(packOut);
                    //out.flush();
                }
            }
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName + "at ECl");
//			System.exit(1);
		} catch (IOException e) {
//			System.err.println("Couldn't get I/O for the connection to " + hostName + " at ECl");

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

    public synchronized void setPackOut(PackToSendToServer packOut) {
        this.packOut = packOut;
    }
}
