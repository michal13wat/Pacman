
package pacman;

import clientAndServer.MyServer;
import clientAndServer.PackReceivedFromServer;
import clientAndServer.PackToSendToServer;
import gameObjects.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URL;

import menuAndInterface.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import menuAndInterface.TextObject;

import _serverV2.*;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public class ServerGame extends Game {
    
    public ServerGame(StringWrapper portString, IntWrapper playersAmount) {
        //this.portString = portString;
        this.playersAmount = playersAmount;
    }
    
    @Override
    public void init(){
        System.out.println("Inicjalizacja ServerGame.");
        // Parametry gry.
        running = true;
        gameStarted = false;
        
        framesPerSecond = 60;
        framesSkip = 1000/framesPerSecond;
        max_render_skip = 10;
        
        wrapperInit();
        startingLives.value = 1;
        
        objectList = new ArrayList<>();
        menuControl = new MenuControl(this);
        keyboardControl = new KeyboardControl(this);
        
        playerNumbers = new HashMap<>();
        playerNames = new HashMap<>();
        playerCharacters = new HashMap<>();
        keyboardControlRemote = new HashMap<>();
        playerReady = new HashMap<>();
        
        int port = new Integer(Game.portString.value);
        listening = true;
        
        server = new ServerBrain(port, port+1, playersAmount.value);
        server.start();
        
        packOutToClient = new PackReceivedFromServer<>();
        
        //gotoMenu("test");
        endGame(false);
        
        //createObject(TestObject.class);
        /*TextObject o = (TextObject)createObject(TextObject.class);
        o.setPosition(0, 0);
        o.loadFont("pac_font_sprites",8,8);
        o.setText("AAA");*/
        
        globalCounter = 0;
        System.out.println("Inicjalizacja ServerGame zakończona.");
        gameLoop();
    }
    
    @Override
    protected void wrapperInit() {
        
        playerName = new StringWrapper("SERVER");
        chosenCharacter = new IntWrapper(-1);

        startingLives = new IntWrapper(3);
        playerNumber = new IntWrapper(1);
        ghostsAmount = new IntWrapper(4);

        pacmanPlayer = new IntWrapper(-1);
        ghostPlayer = new IntWrapper[4];
        for (int i = 0; i < 4; i++)
            ghostPlayer[i] = new IntWrapper(-1);
    }
    
    @Override
    public void endGame(boolean victory) {
        
        System.out.println("NEW STAGE");
        for (GameObject o : objectList) o.destroy();
        objectList.clear();
        
        LabyrinthObject l = (LabyrinthObject)createObject(LabyrinthObject.class);
        
        double stage = Math.random();
        System.out.println(stage);
        
        if (Game.isJar) {
            if (stage < 1/3.0) l.setSource("/resources/stages/pac_layout_0.txt",true);
            else if (stage < 2/3.0) l.setSource("/resources/stages/pac_layout_2.txt",true);
            else l.setSource("/resources/stages/pac_layout_3.txt",true);
        }
        else {
            URL file1, file2, file3;
            file1 = getClass().getResource("/resources/stages/pac_layout_0.txt");
            file2 = getClass().getResource("/resources/stages/pac_layout_2.txt");
            file3 = getClass().getResource("/resources/stages/pac_layout_3.txt");
            System.out.println(file1.getPath());
            if (stage < 1/3.0) l.setSource(file1.getPath(),false);
            else if (stage < 2/3.0) l.setSource(file2.getPath(),false);
            else l.setSource(file3.getPath(),false);
        }
        
        
        gameStep();
        
        PlayerDisplayObject playerDisplay = (PlayerDisplayObject)createObject(PlayerDisplayObject.class);
        playerDisplay.loadFont("pac_font_sprites",8,8);
        playerDisplay.setPosition(l.getWidth()+16,80);
        playerDisplay.setAllConnected();
        
        PlayerTagObject tagDisplay = (PlayerTagObject)createObject(PlayerTagObject.class);
        tagDisplay.loadFont("pac_font_sprites",8,8);
        tagDisplay.setPosition(l.getWidth()+16,64);
    }
    
    @Override
    protected void gameLoop() {
        // Konsystentny FPS.
        double nextStep = System.currentTimeMillis();
        boolean kickstarted = false;
        int loops;
        
        while (running){
            loops = 0;
            
            //System.out.println("SERWER - W tej chwili " + objectList.size() + " obiektów.");
            
            while ((System.currentTimeMillis() > nextStep) && (loops < max_render_skip)) {
                
                if (gameStarted) gameStep();
                
                //if (ServerBrain.getObjReceived() != null) {
                    receiveInput();
                    sendObjects();
                //}
                
                nextStep += framesSkip;
                globalCounter ++;
                loops ++;
                
                if (keyboardCheck("escape")) running = false;
            }
        }
        
        try {
            server.disconnectAll();
            server.close();
        }
        catch (Exception e) {}
        
        System.out.println("Server closing!");
    }
    
    protected void receiveInput() {
        // odbieranie obiektu
        for (PackToSendToServer pack : ServerBrain.recPacks)
            putToArrayDataReceivedFromServer(pack);
        
        ArrayList<Integer> servedIds = new ArrayList<>();
        
        for (PackToSendToServer pack : arrayWithDataFromPlayers){
            if ((!playerNumbers.containsKey(pack.getPlayersId())) && (pack.getPlayersId() > 0)) {
                // NOWY GRACZ SIĘ PODŁĄCZYŁ!!!
                System.out.print("NOWY GRACZ!!! - name = " + pack.getPlayersName() + "\n");
                
                playerNumbers.put(pack.getPlayersId(), ++playersConnected);
                playerNames.put(pack.getPlayersId(), pack.getPlayersName());
                playerCharacters.put(pack.getPlayersId(), pack.getCharacter());
                keyboardControlRemote.put(pack.getPlayersId(), new KeyboardControlRemote(this));
                playerReady.put(pack.getPlayersId(),false);
                
                // Ustawianie postaci.
                chosenCharacter.value = pack.getCharacter();
                chooseCharacter(false,pack.getPlayersId());
                
                for (Integer i : keyboardControlRemote.keySet())
                System.out.println("new remote keyboard - " + i);
            }
           
            servedIds.add(pack.getPlayersId());
            //System.out.print("SERWER " + ((allReady) ? ("[OK] ") : "") + "- name = " + pack.getPlayersName() + ((pack.isPlayerReady()) ? (" [OK] ") : "")
            //+ " id = " + pack.getPlayersId() + " character = " + pack.getCharacter() + ", pressedKey = " + pack.getPressedKey() + "\n");
            
            if (pack.isPlayerReady() == true)
                playerReady.put(pack.getPlayersId(), true);
            else
                playerCharacters.put(pack.getPlayersId(), pack.getCharacter());
            
            // Ustawianie odpowiednich wejść z klawiatury.
            ((KeyboardControlRemote)getKeyboard(pack.getPlayersId())).feedInput(pack.getPressedKey());
        }
        
        allReady = (playerNumbers.size() > 0);
        for (Integer id : playerReady.keySet()){
<<<<<<< HEAD
=======
            
            System.out.print("SERWER " + ((allReady) ? ("[OK] ") : "") + "- name = " + playerNames.get(id) + ((playerReady.get(id)) ? (" [OK] ") : "")
            + " id = " + id + " character = " + playerCharacters.get(id)/* + ", pressedKey = " + pack.getPressedKey() + "\n"*/);
            
            if (servedIds.contains(id)) System.out.println(" [RECV]");
            
            System.out.println("\n");
            
>>>>>>> 3b3557dc3a05c85b3c207f91006196031452faf4
            if (!playerReady.get(id))
                allReady = false;
        }
        
        // Jeżeli wszyscy gracze są gotowi, to zaczynamy.
        if ((allReady == true) && (gameStarted == false)) {
            
            // Resetowanie postaci.
            pacmanPlayer.value = -1;
            for (int i = 0; i < 4; i++)
                ghostPlayer[i].value = -1;
            
            //ServerBrain.forceStart = true;
            
            for (Integer id : playerNumbers.keySet()){
                System.out.println("Gracz " + id + " - " + playerCharacters.get(id));
                chosenCharacter.value = playerCharacters.get(id);
                chooseCharacter(false,id);
            }
            
            System.out.println("SERWER - ZACZYNAMY GRĘ!!!");
            gameStarted = true;
        }
        
        // Usunięte gdyż:
        // Za bardzo zaśmiecało konsolę.
        
        /*System.out.print("Name = " + assdfsdf.getObjReceived().getPlayersName()
                + ", Character = " + assdfsdf.getObjReceived().getCharacter()
                + ", PressedKey = " + assdfsdf.getObjReceived().getPressedKey() + "\n");*/
        
        //assdfsdf.setObjReceived(null);
    }
    
    protected synchronized void sendObjects() {
        // symulacja przetwarznia obiektu
        
        // Usunięte gdyż:
        // to tylko symulacja.
        
        /*testObj.ilosc = 0;
        testObj.nazwa = ("asdf " +  assdfsdf.getObjReceived().getPlayersName()
                + " hwdp " + assdfsdf.getObjReceived().getCharacter()
                + " jp100 " + assdfsdf.getObjReceived().getPressedKey() );
        objList.clear();
        for (int i = 0; i < 4; i ++){
            objList.add(testObj);
        }*/
        
        if (allReady)
        {
            // wysyłanie obiektu
            packOutToClient.clear();
            //packOutToClient.addList(objectList);
            packOutToClient.addDeletedList(deletedIds);
            deletedIds.clear();

            // Wysyłanie obiektów zaczyna się dopiero od momentu początku gry.
            if (gameStarted) {
                for (GameObject o : objectList)
                    if (o.sendMe())
                        packOutToClient.addObject(o);
            }
        }
        
        // Wysyłanie z powrotem wejść od wszystkich klientów.
        packOutToClient.addFeedbacks(arrayWithDataFromPlayers);
        
        packOutToClient.gameScore = gameScore;
        packOutToClient.gameLives = gameLives;
        packOutToClient.maxPlayers = playersAmount.value;
        
        // serializacji paczki
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        
        try {
            // TUTAJ SERIALIZUJEMY,
            // Gdyż (ponoć) serializacja nie jest bezpieczna dla wątków.
            // I rzeczywiście, jak zostawiłem to w assdfsdf i tutaj
            // odpalałem gameStep, to co chwila się wywracał.
            out = new ObjectOutputStream(bos);   
            out.writeObject(packOutToClient);
            out.flush();
            
            byte[] bytesToSend = bos.toByteArray();
            bos.close();
            
            //PackReceivedFromServer packClone = deserialize(bytesToSend);
            
            //Hello h2;
            ByteArrayInputStream bis = new ByteArrayInputStream(bytesToSend);
            ObjectInput in = new ObjectInputStream(bis);
            PackReceivedFromServer packClone = (PackReceivedFromServer) in.readObject();
            
            
            //System.out.println(bytesToSend);
            
            ServerBrain.packOut = packClone;
            ServerBrain.bytesOut = bytesToSend;
        }
        catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    synchronized protected void putToArrayDataReceivedFromServer
            (PackToSendToServer packReceivedFromclient){
        
        boolean newPlayer = true;
        int positionInArray = 0;
        for(int i = 0; i < arrayWithDataFromPlayers.size(); i++){
            if (arrayWithDataFromPlayers.get(i).getPlayersName().
                    equals(packReceivedFromclient.getPlayersName())){
                newPlayer = false;
                positionInArray = i;
                break;
            }
        }
        
        if (newPlayer) {
            arrayWithDataFromPlayers.add(packReceivedFromclient);
            packOutToClient.addConnectedClient(packReceivedFromclient.getPlayersName());
            packOutToClient.setNotConnectedClients(MyServer.getClientAmount()
                    - ServerBrain.connectedClients.size());
        }
        else{
            arrayWithDataFromPlayers.set(positionInArray, packReceivedFromclient);
        }
        // nie jest to klientom do niczego potrzebne, tak tylko testowo to przesyłam...
        packOutToClient.setAdditionalInfo(packReceivedFromclient.getPressedKey());
    }
    
    public void stopGame() {
        running = false;}
    public boolean isRunning() {
        return running;}
    
    ArrayList<Integer> deletedIds = new ArrayList<>();
    ArrayList<PackToSendToServer> arrayWithDataFromPlayers = new ArrayList<>();
    PackReceivedFromServer<GameObject> packOutToClient;
    
    // Ten oryginalnie tu był dodany, ale potrzebny był też w ClientGame,
    // więc przeniosłem go po prostu do Game.
    //HashMap <Integer,KeyboardControlRemote> keyboardControlRemote;
    
    int playersConnected = 0;
    boolean gameStarted;
    boolean allReady = false;
    
    ServerBrain server;
}
