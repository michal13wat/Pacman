
package pacman;

import clientAndServer.Client;
import clientAndServer.PackReceivedFromServer;
import clientAndServer.PackToSendToServer;
import gameObjects.GameObject;
import gameObjects.LabyrinthObject;
import java.util.ArrayList;
import static java.util.Collections.list;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import menuAndInterface.*;
import static pacman.Game.ipString;
//import static pacman.Game.packReceivedFromServer;
import static pacman.Game.playerNumber;
import static pacman.Game.portString;

public class ClientGame extends Game {
    
    public ClientGame(JFrame gameWindow, JPanel gameRenderer, StringWrapper playerName, IntWrapper chosenCharacter) {
        this.gameWindow = gameWindow;
        this.gameRenderer = gameRenderer;
        this.playerName = playerName;
        this.chosenCharacter = chosenCharacter;
        //this.ipString = ipString;
        //this.portString = portString;
        //this.playerNumber = playerNumber;
    }
    
    @Override
    public void init(){
        Random random = new Random();
        clientId = (int)Math.abs(random.nextInt());
        random = null;
        
        // Parametry gry.
        running = true;
        ready = false;
        
        wrapperInit();
        
        framesPerSecond = 60;
        framesSkip = 1000/framesPerSecond;
        max_render_skip = 10;
        objectList = new ArrayList();
        
        playerNumbers = new HashMap<>();
        playerNames = new HashMap<>();
        playerCharacters = new HashMap<>();
        keyboardControlRemote = new HashMap<>();
        playerReady = new HashMap<>();
        
        // Klawiatura.
        keyboardControl.keyboardInit();
        keyboardControlRemote = new HashMap<>();
        // Sprite'y.
        preloadSprites();
        
        // Wątek klienta.
        String addressIP = Game.ipString.value;
        String port = Game.portString.value;
        int playerID = Game.playerNumber.value;
        client = new Client(addressIP, new Integer(port), playerID);
        
        gotoMenu("game_lobby");
        
        PlayerDisplayObject playerDisplay = (PlayerDisplayObject)createObject(PlayerDisplayObject.class);
        playerDisplay.loadFont("pac_font_sprites",8,8);
        playerDisplay.setPosition(0,48);
        
        globalCounter = 0;
        gameLoop();
    }
    
    @Override
    protected void wrapperInit() {
        
        startingLives = new IntWrapper(3);
        playersAmount = new IntWrapper(4);
        ghostsAmount = new IntWrapper(4);
        playerNumber = new IntWrapper(1);
        
        pacmanPlayer = new IntWrapper(-1);
        ghostPlayer = new IntWrapper[4];
        for (int i = 0; i < 4; i++)
            ghostPlayer[i] = new IntWrapper(-1);
    }
    
    @Override
    protected void gameLoop() {
        // FPS.
        double nextStep = System.currentTimeMillis();
        int loops;
        
        while (running){
            loops = 0;
            
            //System.out.println("KLIENT - W tej chwili " + objectList.size() + " obiektów.");

            while ((System.currentTimeMillis() > nextStep) && (loops < max_render_skip)) {
                sendInput();
                receiveObjects();
                if (!ready) gameStep();
                
                nextStep += framesSkip;
                globalCounter ++;
                loops ++;
                
                if ((System.currentTimeMillis() <= nextStep) || (loops >= max_render_skip)) {
                    if ((running) && (!halted)) {
                        //System.out.println("KLIENT - RYSUJĘ");
                        gameDraw();
                        //System.out.println("KLIENT - SKOŃCZYŁEM");
                    }
                }
            
                if ((keyboardCheck("escape")) || (keyboardCheck("q"))) running = false;
            }
        }
        
        client.close();
    }
    
    @Override
    public GameObject createObject(Class ourClass){
        // Obiekty, które tworzymy na kliencie nie mają prawa istnieć.
        GameObject obj = super.createObject(ourClass);
        obj.dispose();
        return obj;
    }
    
    protected void sendInput()
    {
        String name;
        int character;
        String pressedKey;
        
        name = playerName.value;
        // TODO - zrobić jak będzie działał wybór postaci
        character = chosenCharacter.value;
        pressedKey = checkPressedKeys();
        System.out.println("KLIENT " + name + (ready ? ("[OK] ") : "") + " " + pressedKey);
        
        // TODO - wywalić to opóźnienie
        
        // Usunięte gdyż:
        // TODO tak kazał.
        /*try{
             sleep(2000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }*/
        
        client.setObjToSendToServer(new PackToSendToServer(
                name, character, pressedKey, clientId, ready));
    }
    
    protected void receiveObjects()
    {
        // odebranie obiektów od serwera i symulacja wyświetlenia obiektów na mapie
        while (!client.getListInputPackages().isEmpty()) {
            packReceivedFromServer = client.getListInputPackages().getLast();
            client.getListInputPackages().removeLast();
//                ArrayList<TestObjectToSend> objList = temp.getObjectsList();
//                for (TestObjectToSend obj : objList){
//                    System.out.print("objReceivedFromServer.ilosc = " + obj.ilosc
//                            + " objReceivedFromServer.nazw = " + obj.nazwa + "\n");
//                }
            System.out.print("KLIENT - Odebrano obiekty. \n");
            System.out.print("KLIENT - Connected clients: \n");
            for ( int i = 0; i < packReceivedFromServer.getConnectedClients().size(); i++){
                System.out.print("\t- " + packReceivedFromServer.getConnectedClients().get(i) + "\n");
            }
            System.out.print("KLIENT - Waiting for: " + packReceivedFromServer.getNotConnectedClients() +
                    " players\n");
            
            gameScore = packReceivedFromServer.gameScore;
            gameLives = packReceivedFromServer.gameLives;
            playersAmount.value = packReceivedFromServer.maxPlayers;
            
            // Przybieranie nowej listy jako własna.
            overlapIds(packReceivedFromServer.getObjectsList());
            deleteIds(packReceivedFromServer.getDeletedList());
            LabyrinthObject labyrinth = null;
            
            if (packReceivedFromServer.getRandomizer() != null) random = packReceivedFromServer.getRandomizer();
            
            // Ustawianie wszystkim obiektom tej gry jako bazowej.
            for (GameObject o : objectList) {
                //System.out.println("K " + o.getClass() + " " + o.getX() + "," + o.getY());
                o.setGame(this);
                
                // Szukanie labirytnu.
                if (o instanceof LabyrinthObject) {
                    labyrinth = (LabyrinthObject)o;
                }
            }
            
            if (labyrinth != null) {
                // Powiadamianie obiektów o labiryncie.
                for (GameObject o : objectList) {
                    o.setCollisionMap(labyrinth);
                }
            }
            
            for (PackToSendToServer pack : packReceivedFromServer.getClientFeedback()){
                
                if (pack.getPlayersId() != clientId) {
                    if (!playerNumbers.containsKey(pack.getPlayersId())) {
                        // Tutaj jakiś błąd chyba...
                        System.out.print("Klient poznał nowego gracza - name = " + pack.getPlayersName()
                                + ", ID = " + pack.getPlayersId() + "\n");
                        
                        playerNumbers.put(pack.getPlayersId(), ++playersConnected);
                        playerNames.put(pack.getPlayersId(), pack.getPlayersName());
                        playerCharacters.put(pack.getPlayersId(), pack.getCharacter());
                        keyboardControlRemote.put(pack.getPlayersId(), new KeyboardControlRemote(this));
                        playerReady.put(pack.getPlayersId(),false);
                        
                        // Ustawianie postaci.
                        chosenCharacter.value = pack.getCharacter();
                        chooseCharacter(false,pack.getPlayersId());
                        
                        for (Integer i : keyboardControlRemote.keySet())
                            System.out.println("KLIENT - new remote keyboard - " + i);
                    }
                    
                    if (pack.isPlayerReady() == true)
                        playerReady.put(pack.getPlayersId(), true);
                    else
                        playerCharacters.put(pack.getPlayersId(), pack.getCharacter());
                    
                    // Ustawianie odpowiednich wejść z klawiatury
                    try
                    {((KeyboardControlRemote)getKeyboard(pack.getPlayersId())).feedInput(pack.getPressedKey());}
                    catch (Exception e) {System.out.println("WRYYYYYYYYYY");}
                }
            }
        }
    }
    
    private void overlapIds(ArrayList<GameObject> newList) {
        System.out.print("KLIENT - MAMY " + objectList.size() + " ODEBRANE: " + newList.size());
        for (GameObject oo : newList)
            for (ListIterator<GameObject> iter = objectList.listIterator(); iter.hasNext(); ) {
                GameObject o = iter.next();
                if ((o.getId() == oo.getId()) || (o.isDisposable()))
                    iter.remove();
            }
        //objectList.clear();
        for (GameObject oo : newList) {
            objectList.add(oo);
            //System.out.print(oo.getClass().getName() + " ");
        }
        System.out.print("\n");
        //for (GameObject o : objectList)
        //    System.out.println(o);
    }
    
    private void deleteIds(ArrayList<Integer> newList) {
        for (int id : newList)
            for (ListIterator<GameObject> iter = objectList.listIterator(); iter.hasNext(); ) {
                GameObject o = iter.next();
                if (o.getId() == id)
                    iter.remove();
            }
    }
    
    @Override
    public KeyboardControl getKeyboard(int i) {
        if (i == clientId) return keyboardControl;
        if (keyboardControlRemote.containsKey(i)) return keyboardControlRemote.get(i);
        //System.out.println(i);
        return keyboardControl;
    }
    
    static volatile PackReceivedFromServer<GameObject> packReceivedFromServer;
    
    Client client;
    int playersConnected = 0;
    int clientId;
    
    boolean ready;
    
    public void setReady(boolean x) {ready = x;}
    public boolean isReady() {return ready;}
}
