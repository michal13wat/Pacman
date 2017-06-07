package _serverV2;

import clientAndServer.TestObjectToSend;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import gameObjects.GameObject;

public class ServerBrain extends Thread {

    public static int clientAmount; // = 4;
    protected static ArrayList<ServerReceiver> clientList = new ArrayList<>();
    protected static ArrayList<ServerSender> brcList = new ArrayList<>();
    public static volatile ArrayList<String> connectedClients = new ArrayList<>();
    public static volatile int notConnectedClients = clientAmount;
    private int recPort; // = 7171;
    private int brcPort; // = 7172;

    public static LinkedBlockingDeque<clientAndServer.PackToSendToServer> recPacks
            = new LinkedBlockingDeque<>();
    
    public static clientAndServer.PackReceivedFromServer<GameObject> packOut
            = new clientAndServer.PackReceivedFromServer<>();
    public static byte[] bytesOut;
    
    public volatile static ArrayList<Boolean> sendPrevPack = new ArrayList<>();
    public volatile static ArrayList<Boolean> readPrevPack = new ArrayList<>();

    boolean running = true;
    
    public ServerBrain(int recPort, int brcPort, int clientAmount) {
        System.out.println("Server starting...");
        this.recPort = recPort;
        this.brcPort = brcPort;
        this.clientAmount = clientAmount;
    }

    public void run(){
        int threadCounter = 0;

        //lockBufferingToSend();

        System.out.println("Listening for connections.");
        try (ServerSocket serverSocket = new ServerSocket(recPort);
             ServerSocket broadcastSocket = new ServerSocket(brcPort);) {
            while (running) {
                if (!checkIfAllClientsAreConnected()) {
                    sendPrevPack.add(Boolean.TRUE);
                    readPrevPack.add(Boolean.FALSE);
                    ServerReceiver newClient = new ServerReceiver(serverSocket.accept());
                    newClient.start();
                    clientList.add(newClient);

                    ServerSender serverSenderClient = new ServerSender(broadcastSocket.accept(), threadCounter);
                    serverSenderClient.start();
                    brcList.add(serverSenderClient);
                    threadCounter++;
                } else {
                    System.out.print("Wszyscy klienci podłączeni.\n");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(
                    "Exception caught when trying to listen on port " + recPort + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    public void close() {running = false;}
    
    public static void disconnectAll() throws InterruptedException {
        for (int i = 0; i < clientList.size(); i++) {
            clientList.get(i).closeSocket();
            brcList.get(i).closeSocket();
        }
        Thread.sleep(150);
    }

    //public static boolean forceStart = false;
    public synchronized static boolean checkIfAllClientsAreConnected() {
        return (clientList.size() == clientAmount);// || (forceStart));
    }

    public synchronized static boolean checkIfPackWasSendByThisThread(int threadID){
        if (threadID > sendPrevPack.size()) return false;
        return sendPrevPack.get(threadID);
    }

    public synchronized static void lockBufferingToSend(){
        for (int i = 0; i < sendPrevPack.size(); i++){
            sendPrevPack.set(i, false);
        }
    }

    public synchronized static void  lockBufferingToSendByThisThread(int threadID){
        if (threadID <= sendPrevPack.size()){
            sendPrevPack.set(threadID, true);
        }
    }

    public synchronized static void thisThreadReadPackToSend(int threadID){
        readPrevPack.set(threadID, false);
    }

    public synchronized static boolean checkIfAllThreadReadPrevPack(){
        for (Boolean aReadPrevPack : readPrevPack) if (!aReadPrevPack) return false;
        return true;
    }

    public synchronized static void lockReadingNextPack(){
        for (int i = 0; i < readPrevPack.size(); i++){
            readPrevPack.set(i, true);
        }
    }
}
