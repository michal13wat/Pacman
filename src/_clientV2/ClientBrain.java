package _clientV2;

import clientAndServer.PackToSendToServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by User on 2017-05-28.
 */
public class ClientBrain extends Thread {
    public ClientSender clientSender;
    public ClientReceiver receiver;
    public PackToSendToServer packOut
            = new PackToSendToServer("name", 0, "direction", 0, false);  // jakiś tam testowy pakiet na początek
    public static clientAndServer.PackReceivedFromServer<gameObjects.GameObject> recPac;
    public static volatile ArrayList<String> connectedClientsBuffer = new ArrayList<>();
    public static volatile int notConnectedClientsAmountBuffer = -1;
    private volatile String addressIP;
    private volatile int portSending;
    private volatile int portReceiving;

    boolean running = true;
    
    public ClientBrain(String addressIP, int portSending, int portReceving,
                       String clientName, int clientID){
        System.out.println("Client starting");
        
        this.addressIP = precessAddressIP(addressIP);
        this.portSending = portSending;
        this.portReceiving = portReceving;
        
        System.out.println("Klient podłącza się do adresu: " + this.addressIP);
        System.out.println("Klient podłącza się na porcie: " + this.portSending);
        //"127.0.0.1"
        // 7171
        packOut.setPlayersName(clientName);
        packOut.setPlayersId(clientID);
        clientSender = new ClientSender(this.addressIP, portSending, packOut);
        clientSender.start();
        try{
            receiver = new ClientReceiver(this.addressIP, portReceving); // 7172
            receiver.start();
        }catch (IOException e){
            System.err.print("Wyjątek w klasie odbierającej dane od serwera!\n");
        }
    }

    @Override
    public void run() {
        String keyName;
        Scanner keyboard = new Scanner(System.in);
        while (running){
            clientSender.setPackOut(packOut);
            /*System.out.println("Podaj nazwę klawisza do wysłania do serwara: ");
            keyName = keyboard.next();
            packOut.setPressedKey(keyName);*/
        }
    }

    public void close() {running = false;}
    
    public synchronized static void writeToBufConnClients(ArrayList<String> recConnClients, int notConnected){
        for(int i = 0; i < recConnClients.size(); i++){
            if(!connectedClientsBuffer.contains(recConnClients.get(i))){
                connectedClientsBuffer.add(recConnClients.get(i));
            }
        }
        notConnectedClientsAmountBuffer = notConnected;
    }

    public static int getNotConnectedClientsAmountBuffer() {
        return notConnectedClientsAmountBuffer;
    }

    public static ArrayList<String> getConnectedClientsBuffer() {
        return connectedClientsBuffer;
    }

    private String precessAddressIP(String addressIP){
        if (addressIP == "localhost"){
            return addressIP;
        }
        int length = addressIP.length();
        String IP = new String();
        if (length < 8 || length > 11){
            IP = addressIP;
        }else {
            if (addressIP.matches("\\d+")){
                //System.out.println("To jest adres IP: " + addressIP);
                IP = addressIP.substring(0, 3);
                IP += ".";
                IP += addressIP.substring(3, 6);
                IP += ".";
                IP += addressIP.substring(6, 7);
                IP += ".";
                IP += addressIP.substring(7);
                //System.out.println("Po przetworzeniu:" + IP);
            }

        }
        return IP;
    }
}
