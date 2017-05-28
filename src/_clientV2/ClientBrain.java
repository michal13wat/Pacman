package _clientV2;

import clientAndServer.PackToSendToServer;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by User on 2017-05-28.
 */
public class ClientBrain extends Thread {
    public ClientSender clientSender;
    public ClientReceiver receiver;
    public PackToSendToServer packOut
            = new PackToSendToServer("Michal", 0, "up", 3);  // jakiś tam testowy pakiet na początek
    public static clientAndServer.PackReceivedFromServer<clientAndServer.TestObjectToSend> recPac;

    public ClientBrain(){
        System.out.println("Client starting");
        clientSender = new ClientSender("localhost", 7171, packOut);
        clientSender.start();
        try{
            receiver = new ClientReceiver("127.0.0.1", 7172);
            receiver.start();
        }catch (IOException e){
            System.err.print("Wyjątek w klasie odbierającej dane od serwera!\n");
        }
    }

    @Override
    public void run() {
        String keyName;
        Scanner keyboard = new Scanner(System.in);
        while (true){
            clientSender.setPackOut(packOut);
            System.out.println("Podaj nazwę klawisza do wysłania do serwara: ");
            keyName = keyboard.next();
            packOut.setPressedKey(keyName);
        }
    }
}
