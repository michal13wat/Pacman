package clientAndServer;

/**
 * Created by User on 2017-04-17.
 */
import com.sun.javafx.scene.control.skin.VirtualFlow;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;


public class Client extends Thread {
    private SocketChannel sChannel = null;
    private volatile VirtualFlow.ArrayLinkedList<PackReceivedFromServer> listInputPackages =
            new VirtualFlow.ArrayLinkedList<>();
    private volatile PackToSendToServer objToSendToServer = null;

    ObjectOutputStream oos;
    ObjectInputStream ois;
    
    // Player Number is ID Client ( 0 - n), where: n = PlayersAmount - 1
    public Client(String address, int port, int playerNumber){
        int port1 = port;// + playerNumber;
        
        try{
            sChannel = SocketChannel.open();
            sChannel.configureBlocking(true);
            sChannel.connect(new InetSocketAddress(address, port1));
        }catch(IOException e){
            System.out.print("Nazwiązanie połączenia z serwerame nie powiodło się!");
        }
        System.out.print("Klient podłączony na porcie " + port1 + "\n");

        try {
            oos = new ObjectOutputStream(sChannel.socket().getOutputStream());
            ois = new ObjectInputStream(sChannel.socket().getInputStream());
        }
        catch (Exception e) {}

        try{
            sChannel.socket().setSoTimeout(15);
        }catch (SocketException e){
            System.out.print("Złapano wyjątek związany z timeout-em w kliencie\n");
        }
        setName("CLIENT");
        
        start();
    }

    @Override
    public void run() {
        try {
            while (running){
                if (sChannel.isConnected()){
                    receiveObjectFromServer(sChannel);
                    if (objToSendToServer != null){
                        sendObjectToServer(sChannel, objToSendToServer);
                        objToSendToServer = null;
                    }
                }
            }
        }
        catch (Exception e)
        {close();}
        
        System.out.println("Zamykamy klienta!");
    }

    private void sendObjectToServer(SocketChannel sChannel, PackToSendToServer packOut){
        try{
            oos.writeObject(packOut);
            oos.flush();
        } catch (IOException e){
            System.out.print("Złapano wyjątek w wysłającej metodzie klienta\n");
        }
    }

    private void receiveObjectFromServer(SocketChannel sChannel){
        try{
            ois = new ObjectInputStream(sChannel.socket().getInputStream());
           listInputPackages.addFirst((PackReceivedFromServer)ois.readObject());
        } catch (IOException | ClassNotFoundException e){
           System.out.print("Złapano wyjątek w odbierającej metodzie klienta: " + e.toString()+ "\n");
        }
    }

    public VirtualFlow.ArrayLinkedList<PackReceivedFromServer> getListInputPackages() {
        return listInputPackages;
    }

    public synchronized void setObjToSendToServer(PackToSendToServer objToSendToServer) {
        this.objToSendToServer = objToSendToServer;
    }
    
    public void close() {
        running = false;
        
        try {
            sChannel.close();
            oos.close();
            ois.close();
        }
        catch (Exception e) {sChannel = null;}
    }
    
    boolean running = true;
}