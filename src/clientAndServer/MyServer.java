package clientAndServer;

/**
 * Created by User on 2017-04-17.
 */
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import pacman.Game;

public class MyServer extends Thread {
    
    // Klasa nasłuchująca połączeń od nowych klientów.
    // Dla każdego z nich robi nowy ServerThread.
    
    private ExecutorService executor;
    ServerSocketChannel serverSocket;
    
    int port;
    boolean running = true;
    
    private static int clientAmount;

    ArrayList<ServerThread> serverThreads = new ArrayList<>();

    public MyServer(int port, int clientAm) {
        
        clientAmount = clientAm;
        executor = Executors.newFixedThreadPool(clientAm);
        this.port = port;
        
        // Usunięte gdyż:
        // Implementuje stary sposób tworzenia wątków
        // (tzn. liczba na sztywno, każdy na innym porcie).
        
        /*ServerThread.setPort(port);
        for (int i = 0; i < clientAmount; i++) {
            ServerThread temp = new ServerThread();
//            temp.setPriority(Thread.MAX_PRIORITY);
            serverThreads.add(temp);
            temp.start();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                System.out.print("SERWER - złapano wyjątek związany z opóźnieniem tworzenia wątku klienta\n");
            }
        }*/
        
        try
        {
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(true);
            serverSocket.socket().bind(new InetSocketAddress(port));
            System.out.print("Tworzenie serwera...\n");
        }
        catch (IOException e)
        {System.out.print("SERWER - bląd w tworzeniu głównego wątku serwerowego!\n");}
        
        setName("SERVER");
        
        start();
    }

    @Override
    public void run() {
        
        // TERAZ TUTAJ TWORZYMY WĄTKI SERWEROWE!!!!
        while (running)
        {
            // Ograniczenie po prostu na podstawie zmiennej.
            if (serverThreads.size() < clientAmount)
            {
                try
                {
                    ServerThread temp = new ServerThread(serverSocket.accept(),port);
                    serverThreads.add(temp);
                    executor.submit(temp);
                }
                catch (IOException e)
                {System.out.print("SERWER - błąd w tworzeniu wątku serwerowego!\n");}
                
                try
                {TimeUnit.MILLISECONDS.sleep(100);}
                catch (InterruptedException e)
                {System.out.print("SERWER - złapano wyjątek związany z opóźnieniem tworzenia wątku klienta\n");}
            }
        }
        
        killThreads();
        System.out.println("Zamykamy serwer!");
    }
    
    public static int getClientAmount() {
        return clientAmount;
    }
    
    public void killThreads() {
        for (ServerThread thread : serverThreads) {
            thread.stopThread();
        }
        
        executor.shutdownNow();
    }
    
    public void close() {
        running = false;
        killThreads();
        try
        {serverSocket.close();}
        catch (Exception e) {}
    }
    
    public int getMaxPlayers() {
        return clientAmount;
    }
}
