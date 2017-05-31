
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

import menuAndInterface.MenuControl;
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
        
        framesPerSecond = 60;
        framesSkip = 1000/framesPerSecond;
        max_render_skip = 10;
        
        wrapperInit();
        startingLives.value = 1000;
        
        objectList = new ArrayList<>();
        menuControl = new MenuControl(this);
        keyboardControl = new KeyboardControl(this);
        
        playerNumbers = new HashMap<>();
        playerNames = new HashMap<>();
        playerCharacters = new HashMap<>();
        keyboardControlRemote = new HashMap<>();
        
        int port = new Integer(Game.portString.value);
        listening = true;
        
        server = new ServerBrain(port, port+1, playersAmount.value);
        server.start();
        
        packOutToClient = new PackReceivedFromServer<>();
        
        //gotoMenu("test");
        LabyrinthObject l = (LabyrinthObject)createObject(LabyrinthObject.class);
        l.setSource("src/resources/stages/pac_layout_2.txt");
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
    protected void gameLoop() {
        // Konsystentny FPS.
        double nextStep = System.currentTimeMillis();
        int loops;
        
        while (running){
            loops = 0;
            
            //System.out.println("SERWER - W tej chwili " + objectList.size() + " obiektów.");
            
            while ((System.currentTimeMillis() > nextStep) && (loops < max_render_skip)) {
                
                for (int i = 0; i < 1; i++)
                {gameStep();}
                
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
        
        for (PackToSendToServer pack : arrayWithDataFromPlayers){
            if ((!playerNumbers.containsKey(pack.getPlayersId())) && (pack.getPlayersId() > 0)) {
                // NOWY GRACZ SIĘ PODŁĄCZYŁ!!!
                System.out.print("NOWY GRACZ!!! - name = " + pack.getPlayersName() + "\n");
                
                playerNumbers.put(pack.getPlayersId(), ++playersConnected);
                playerNames.put(pack.getPlayersId(), pack.getPlayersName());
                playerCharacters.put(pack.getPlayersId(), pack.getCharacter());
                keyboardControlRemote.put(pack.getPlayersId(), new KeyboardControlRemote(this));
                
                // Ustawianie postaci.
                chosenCharacter.value = pack.getCharacter();
                chooseCharacter(false,pack.getPlayersId());
                
                for (Integer i : keyboardControlRemote.keySet())
                System.out.println("new remote keyboard - " + i);
            }
            
            //System.out.print("SERWER - name = " + pack.getPlayersName() + " id = " + pack.getPlayersId()
            //+ " character = " + pack.getCharacter() + ", pressedKey = " + pack.getPressedKey() + "\n");
            
            // Ustawianie odpowiednich wejść z klawiatury.
            ((KeyboardControlRemote)getKeyboard(pack.getPlayersId())).feedInput(pack.getPressedKey());
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
        
        // wysyłanie obiektu
        packOutToClient.clear();
        //packOutToClient.addList(objectList);
        packOutToClient.addDeletedList(deletedIds);
        deletedIds.clear();
        
        for (GameObject o : objectList)
            if (o.sendMe())
                packOutToClient.addObject(o);
        
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
    
    // PO JEDNYM DLA POŁĄCZONEGO GRACZA!!!
    HashMap <Integer,Integer> playerNumbers;
    HashMap <Integer,String> playerNames;
    HashMap <Integer,Integer> playerCharacters;
    
    ArrayList<Integer> deletedIds = new ArrayList<>();
    ArrayList<PackToSendToServer> arrayWithDataFromPlayers = new ArrayList<>();
    PackReceivedFromServer<GameObject> packOutToClient;
    
    // Ten oryginalnie tu był dodany, ale potrzebny był też w ClientGame,
    // więc przeniosłem go po prostu do Game.
    //HashMap <Integer,KeyboardControlRemote> keyboardControlRemote;
    
    int playersConnected = 0;
    
    ServerBrain server;
}
