package clientAndServer;

/**
 * Created by User on 2017-04-17.
 */
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class Server extends Thread {

    private static int clientAmount;

    ArrayList<ServerThread> serverThreads = new ArrayList<>();

    public Server(int port, int clientAm) {
        clientAmount = clientAm;
        ServerThread.setPort(port);
        for (int i = 0; i < clientAmount; i++) {
            ServerThread temp = new ServerThread();
//            temp.setPriority(Thread.MAX_PRIORITY);
            serverThreads.add(temp);
            temp.start();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                System.out.print("złapano wyjątek związany z opóźnieniem tworzenia wątku klienta\n");
            }
        }
    }

    public static int getClientAmount() {
        return clientAmount;
    }
}
