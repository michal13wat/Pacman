package clientAndServer;

/**
 * Created by User on 2017-04-17.
 */
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public class ServerThread extends Thread {
    private static volatile int port;
    private static int threadCnt = 0;
    private int threadNum;
    private static int connectedClients = 0;
    private static boolean unlock = false;

    private static volatile byte[] bytesToSend = null;
    private static volatile PackReceivedFromServer objToSend = null;
    public static volatile PackToSendToServer objReceived = null;
    
    ObjectOutputStream oos = null;
    BufferedOutputStream bos = null;
    ObjectInputStream ois = null;
    
    SocketChannel playerWhiteSocket;
    static boolean[] verifySendObject = null;
    
    boolean running = true;

    public ServerThread() {
        threadNum = threadCnt;
        threadCnt++;
        verifySendObject = new boolean[MyServer.getClientAmount()];
        for (int i = 0; i < verifySendObject.length; i++){
            verifySendObject[i] = false;
        }
        
        setName("SERVER THREAD " + threadNum);
    }

    public ServerThread(SocketChannel socket, int port) {
        this.playerWhiteSocket = socket;
        this.port = port;
        
        threadNum = threadCnt;
        threadCnt++;
        verifySendObject = new boolean[MyServer.getClientAmount()];
        for (int i = 0; i < verifySendObject.length; i++){
            verifySendObject[i] = false;
        }
    }
    
    @Override
    public void run() {
        try
        {
            System.out.print("Wątek serwerowy nr: " + threadNum + "\n");
            
            try{
                playerWhiteSocket.socket().setSoTimeout(25);
            }catch (SocketException e){
                System.out.print("Złapano wyjątek związany z timeout-em w serwerze\n");
            }
            
            System.out.print("Podłączono klienta \n");
            
            oos = new ObjectOutputStream(playerWhiteSocket.socket().getOutputStream());
            bos = new BufferedOutputStream(playerWhiteSocket.socket().getOutputStream());
            ois = new ObjectInputStream(playerWhiteSocket.socket().getInputStream());
            
            while (running){
                if (isSentByXThread(threadNum)){
                    System.out.print("Rozopoczynam wysłanie obiektu \n");
                    sendObject(playerWhiteSocket, objToSend, bytesToSend);

                    threadXSendObject(threadNum);
                    System.out.print("Wysłano obiekt \n");
                }
                
                receiveObject(playerWhiteSocket);
//                System.out.print("Pętla wątku obierającego nr.: " + threadNum + " \n");
            }
        }
        catch(IOException ex) {
            System.out.print("Złapano wyjątek w wysyłającym wątku serwera!!!");
            ex.printStackTrace();
        }
        
        System.out.println("Zamykamy wątek serwerowy nr. " + threadNum + "!");
    }
    
    public void stopThread() {
        running = false;
        try {
            playerWhiteSocket.close();
            oos.close();
            bos.close();
            ois.close();
        }
        catch (Exception e) {}
    }

    private void sendObject(SocketChannel sChannel, PackReceivedFromServer object, byte[] bytes) throws IOException
    {
        bos.write(bytes);
        bos.flush();
        //oos.writeObject(object);
        //oos.flush();
    }

    synchronized private void receiveObject (SocketChannel sChannel)
    {
        try {
            objReceived = (PackToSendToServer)ois.readObject();
            //if (objReceived != null)
            //{System.out.print("SERWER - Metoda odbierająca... \n");}
        } catch (IOException | ClassNotFoundException e){
            // Tutaj cały czas wywala wyjątek związany z timeout-em Socket-a, ale nie należy się tym przejnować.
            // Niestety zrobiłem to tak trochę dziadowo i właśnie na tej zasadzie to działa...
//            System.out.print("Błąd serwera - odbieranie obiektu od klienta nie powiodło się!!! \n\n");
//            e.printStackTrace();
        }
    }

    // Jeśli obiekt został wysłany przez wyszystkie wątki, to wtedy ustaw kolejny do wysłania
    public static synchronized void setObjToSend(PackReceivedFromServer objToSend, byte[] bytesToSend) {
        if (isSent() || unlock){
            ServerThread.objToSend = objToSend;
            ServerThread.bytesToSend = bytesToSend;
            for (int i = 0; i < verifySendObject.length; i++){
                verifySendObject[i] = true;
            }
        }
    }

    private static synchronized boolean isSent(){
        for (int i = 0; i < verifySendObject.length; i++){
            if (verifySendObject[i] == true)
                return false;
        }
        return true;
    }

    private static synchronized boolean isSentByXThread(int x){
        return verifySendObject[x];
    }

    private static synchronized void threadXSendObject(int x){
        verifySendObject[x] = false;
    }

    public static synchronized PackToSendToServer getObjReceived() {
        return objReceived;
    }

    public static synchronized void setObjReceived(PackToSendToServer objReceived) {
        ServerThread.objReceived = objReceived;
    }

    public static void setPort(int port) {
        ServerThread.port = port;
    }

    /* Przełącz serwer w tryb blokujący, tj. żeby mógł wysyłać tylko, gdy poprzednie dane roześle do
     * wszystkich klientów. */
    public static void setServerIntoLockMode(){
        unlock = false;
    }

     public static void setServerIntoUnlockMode(){
        unlock = true;
    }

    public static int getConnectedClients() {
        return connectedClients;
    }
}