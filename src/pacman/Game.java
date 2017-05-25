
package pacman;

import clientAndServer.*;
import sun.security.x509.IPAddressName;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import java.util.*;

import java.io.*;

import java.awt.event.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Game extends Thread
{
    class GameObjectComparator implements Comparator<GameObject> {
        // A private class made for sorting objects by depth.
        
        @Override
        public int compare(GameObject obj1, GameObject obj2){
            int d1 = obj1.getDepth(), d2 = obj2.getDepth();
            
            if (d1 > d2) return -1;
            else if (d1 < d2) return 1;
            else return 0; 
        }
    }
    
    public void init(){
        // Game parameters.

        sprites.add(new Sprite("/resources/pac_hero_sprites.png",0,0,16,16));
        sprites.add(new Sprite("/resources/pac_ghost_sprites.png",0,0,16,16));
        sprites.add(new Sprite("/resources/pac_ghost_sprites.png",0,1,16,16));
        sprites.add(new Sprite("/resources/pac_ghost_sprites.png",0,2,16,16));
        sprites.add(new Sprite("/resources/pac_ghost_sprites.png",0,3,16,16));

        running = true;
        
        framesPerSecond = 60;
        framesSkip = 1000/framesPerSecond;
        max_render_skip = 10;
        
        startingLives = new IntWrapper(3);
        playersAmount = new IntWrapper(2);
        playerNumber = new IntWrapper(1);
        isPacmanPlayed = new IntWrapper(0);
        ipString = new StringWrapper("192168110");
        portString = new StringWrapper("8080");
        playerName = new StringWrapper("");
        
        objectList = new ArrayList();
        
        // Window init.
        windowInit(true);
        // Keyboard init.
        keyboardInit();
        
        gotoMenu("start");
        //createObject(LabyrinthObject.class);
        
        globalCounter = 0;
        gameLoop();
    }
    
    public GameObject createObject(Class ourClass){
        // Full procedure for adding a new object into the game.
        
        GameObject gameObject;
        
        try{
            gameObject = (GameObject) ourClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException i) {
            gameObject = new TestObject();
        }
        
        gameObject.game = this;
        gameObject.createEvent();
        gameObject.setPlayed();
        objectList.add(gameObject);
        
        return gameObject;
    }
    
    public GameObject getObject(Class ourClass){
        // Returns first added object of said class.
        // If there are none, returns null.
        
        GameObject gameObject;

        for (int i = 0; i < objectList.size(); i ++) {
            gameObject = objectList.get(i);
            if (objectList.get(i).getClass() == ourClass) return gameObject;
        }
        
        return null;
    }
    
    public ArrayList<GameObject> getAllObjects(Class ourClass)
    {
        // Similar to getObject, returns a list of all said objects.
        
        GameObject gameObject;
        ArrayList<GameObject> ourList = new ArrayList();

        for (int i = 0; i < objectList.size(); i ++){
            gameObject = objectList.get(i);
            if (ourClass.isInstance(gameObject)) ourList.add(gameObject);
        }
        
        return ourList;
    }
    
    public boolean keyboardCheck(String key){
        // Returns whether key is pressed.
        switch (key){
            case "left": return leftPressed;
            case "right": return rightPressed;
            case "up": return upPressed;
            case "down": return downPressed;
            case "escape": return escapePressed;
            case "enter": return enterPressed;
            case "q": return qPressed;
            case "backspace": return backspacePressed;
        }
        
        return false;
    }
    
    public boolean keyboardHoldCheck(String key){
        // Returns whether key is pressed.
        switch (key) {
            case "left": return leftHold;
            case "right": return rightHold;
            case "up": return upHold;
            case "down": return downHold;
            case "escape": return escapeHold;
            case "enter": return enterHold;
            case "q": return qHold;
            case "backspace": return backspaceHold && (backspaceHoldCounter > 0);
        }
        return false;
    }
    
    public char keyboardCharCheck() {
        if (prevKeyChar == keyChar) return 0;
        return keyChar;
    }
    
    public void gotoMenu(String which){
        switch (which){
            case "start":{
                startMenu();
            }
            break;
            case "stage_select":{
                stageSelectMenu();
            }
            break;
            case "server_setup":{
                serverSetupMenu();
            }
            break;
            case  "create_game":{
                createGameMenu();
            }
            break;
            case  "join_game":{
                joinGameMenu();
            }
            break;
            case "display_connected_players": {
                displayConnectedClients();
            }
            break;
        }
    }

    private String checkPressedKeys(){
        String pressed = new String();
        if (leftPressed) pressed += "l";       // left arrow
        if (rightPressed) pressed += "r";      // right arrow
        if (upPressed)  pressed += "u";        // up arrow
        if (downPressed) pressed += "d";       // down arrow
        if (escapePressed)  pressed += "x";    // EXIT
        if (enterPressed) pressed += "e";      // eneter
        if (qPressed) pressed += "q";          // q letter
        if (backspacePressed) pressed += "b";  // backspace
        return pressed;
    }


    //public  void gotoSubMenu(String w)
    
    public void endGame(boolean victory) {
        isPlayedGhostCreated = false;
        GameObject labirynt = getObject(LabyrinthObject.class);
        labirynt.destroy();
        
        gotoMenu("stage_select");
    }
    
    private void gameLoop() {
        // Handles consistent FPS rate.
        double nextStep = System.currentTimeMillis();
        int loops;
        
        while (running){
            loops = 0;
            
            while ((System.currentTimeMillis() > nextStep) && (loops < max_render_skip)) {
                gameStep();
                
                nextStep += framesSkip;
                globalCounter ++;
                loops ++;
            }            
            if (running) gameDraw();
            if (escapePressed) running = false;
        }
        
        gameWindow.setVisible(false);
        gameWindow.dispose();
    }
    
    private void gameStep() {
        // Runs the designated Step code for each and every object.
        // Deletes the "destroyed" ones.
        
        GameObject gameObject;

        for (int i = 0; i < objectList.size(); i++) {
            gameObject = objectList.get(i);

            if (!gameObject.isDestroyed()) gameObject.stepEvent();
        }
        
        for (int i = 0; i < objectList.size(); i++) {
            gameObject = objectList.get(i);
            
            if (gameObject.isDestroyed()) {
                gameObject.destroyEvent();
                objectList.remove(i);
            }
        }
        
        keyboardSetHold();
    }
    
    private void gameDraw() {
        // Same as game_step, but triggers Draw code.
        // Pastes drawn frame onto JPanel.
        
        BufferedImage buf = new BufferedImage(
                gameWindow.getSize().width,gameWindow.getSize().height,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D)buf.getGraphics();
        
        // Sort the list according to depth.
        Collections.sort(objectList,new GameObjectComparator());
        
        setSize();
        
        GameObject gameObject;
        for (int i = 0; i < objectList.size(); i++) {
            gameObject = objectList.get(i);

            gameObject.setScale(drawScale);
            gameObject.setCenter(drawCenterX,drawCenterY);
            gameObject.drawEvent(graphics);
        }
        
        graphics = (Graphics2D)gameRenderer.getGraphics();
        graphics.drawImage(buf,0,0,gameRenderer);
    }
    
    private void setSize() {
        // Setting the size according to window dimensions and labyrinth / menu height.
        LabyrinthObject labirynth;
        MenuObject menu;
        
        drawScale = 1.0;
        drawCenterX = 0;
        drawCenterY = 0;
        
        labirynth = (LabyrinthObject)getObject(LabyrinthObject.class);
        
        if (labirynth != null) {
            if (labirynth.getHeight() >= labirynth.getWidth()) {
                drawScale = (double)gameWindow.getSize().height/(
                        labirynth.getHeight()+labirynth.getY()+16);
                drawCenterX = (int)(gameWindow.getSize().width/2.0-drawScale*(
                        labirynth.getWidth()+labirynth.getX()+72)/2.0);
                drawCenterY = (int)(gameWindow.getSize().height/2.0-drawScale*(
                        labirynth.getHeight()+labirynth.getY()+16)/2.0);
            }
            else {
                drawScale = (double)gameWindow.getSize().width/(
                        labirynth.getWidth() + labirynth.getX()+88);
                drawCenterX = (int)(gameWindow.getSize().width / 2.0 - drawScale * (
                        labirynth.getWidth() + labirynth.getX()+88)/2.0);
                drawCenterY = (int)(gameWindow.getSize().height / 2.0 - drawScale * (
                        labirynth.getHeight() + labirynth.getY()+16)/2.0);
            }
        }
        else {
            menu = (MenuObject)getObject(MenuObject.class);

            try{
                drawScale = (double)gameWindow.getSize().height/(Math.max(
                        96,menu.getMenuHeight())+16);
                drawCenterX = (int)(gameWindow.getSize().width/2.0-drawScale*(
                        menu.getMenuWidth()+menu.getX())/2.0);
                drawCenterY = (int)(gameWindow.getSize().height/2.0-drawScale*(
                        Math.max(96,menu.getMenuHeight())-menu.getY())/2.0);
            }catch (NullPointerException e){
                System.out.print("########  Złąpano wyjątek związany ze skalowaniem!!!! #########\n");
            }
        }
    }
    
    private void startMenu() {
        MenuObject startMenu = (MenuObject)createObject(MenuObject.class);
        startMenu.setFont("/resources/pac_font_sprites.png",8,8);
        startMenu.setTitle("PACMAN");
        
        startMenu.addMenuOption("Single player",() -> {
                    gotoMenu("stage_select");
                    return 1;
                });
        startMenu.addMenuOption("Multiplayer",() -> {
                    gotoMenu("server_setup");
                    return 1;
                });
        
        startMenu.addMenuOption("EXIT", () -> {
                running = false;
                return 1;
            });
        startMenu.addButtonPressOption("exitOnQ",()-> {
                    running = false;
                    return 1;
                }, "q" );
    }
    
    private void stageSelectMenu() {
        MenuObject stageSelectMenu = (MenuObject)createObject(MenuObject.class);
        stageSelectMenu.setFont("/resources/pac_font_sprites.png",8,8);
        stageSelectMenu.setTitle("SINGLE PLAYER");


        /*sprites.add(new Sprite("/resources/pac_font_sprites.png",21,3,8,8));
        sprites.add(new Sprite("/resources/pac_font_sprites.png",22,3,8,8));
        sprites.add(new Sprite("/resources/pac_font_sprites.png",23,3,8,8));
        sprites.add(new Sprite("/resources/pac_font_sprites.png",24,3,8,8));
        sprites.add(new Sprite("/resources/pac_font_sprites.png",25,3,8,8));*/
        stageSelectMenu.addImageSpinnerOption("Character ", null, isPacmanPlayed, 0, 4, sprites);

        stageSelectMenu.addSpinnerOption("Lives ",null,startingLives,1,5);
        stageSelectMenu.addSpinnerOption("Ghosts ", null, playersAmount, 1, 4);

        // Loading all .txt files in "/resources/labyrinths" as stages.
        File folder = new File("src/resources/stages");
        File[] allLabyrinths = folder.listFiles();

        InputStream in;
        String stageName;
        int c;

        for (File f : allLabyrinths) {
            try {
                // Reading the stage name.
                in = new FileInputStream(f);

                stageName = "";
                while ((c = in.read()) != '\n') 
                    stageName += Character.toString((char)c);
            } catch (FileNotFoundException e){
                System.err.println("Error: File not found");
                stageName = "ERROR";
            } catch (IOException e){
                System.err.println("Exception: IOException");
                stageName = "ERROR";
            }

            // New callable.
            final File finalFile = f;
            stageSelectMenu.addMenuOption(stageName,() -> {
                        LabyrinthObject l = (LabyrinthObject)createObject(LabyrinthObject.class);
                        l.setSource(finalFile);
                        return 1;
                    });
        }

        stageSelectMenu.addMenuOption("BACK", () -> {
                gotoMenu("start");
                return 1;
            });

        stageSelectMenu.addButtonPressOption("exitOnQ",()-> {
                    running = false;
                    return 1;
                }, "q" );
    }
    
    private void serverSetupMenu() {
        MenuObject menu = (MenuObject)createObject(MenuObject.class);
        menu.setFont("/resources/pac_font_sprites.png",8,8);
        menu.setTitle("MULTIPLAYER");

        menu.addMenuOption("Create Game", () -> {
            gotoMenu("create_game");
            return 1;
        });
        menu.addMenuOption("Join Game", () -> {
            gotoMenu("join_game");
            return 1;
        });
        menu.addMenuOption("BACK", () -> {
                gotoMenu("start");
                return 1;
            });
        menu.addButtonPressOption("exitOnQ",()-> {
                    running = false;
                    return 1;
                }, "q" );
    }

    private void displayConnectedClients() {
        PackReceivedFromServer pack;
        while (packReceivedFromServer == null){   // czekaj aż klient coś odbierze z serwera i dopiero to wypisz
        }
        pack = packReceivedFromServer;

        MenuObject menu = (MenuObject)createObject(MenuObject.class);
        menu.hidePrefixMenu();
        menu.setFont("/resources/pac_font_sprites.png",8,8);
        menu.setTitle("Connected:");
        for (int i = 0; i < pack.getConnectedClients().size(); i++){
            menu.addMenuOption("- " + pack.getConnectedClients().get(i), null);
        }
        for (int i = 0; i < pack.getNotConnectedClients(); i++){
            menu.addMenuOption("- ", null);
        }
//        menu.addMenuOption("- Michal", null);
//        menu.addMenuOption("- Jan", null);
//        menu.addMenuOption("- Jakub", null);
//        menu.addMenuOption("- ", null);
        menu.addMenuOption("", null);       //  enter
        menu.addMenuOption("", null);       //  enter
        menu.addMenuOption("Waiting for: " + pack.getNotConnectedClients(), null);
        menu.addMenuOption("player", null);
    }


    private void createGameMenu() {
        MenuObject menu = (MenuObject)createObject(MenuObject.class);
        menu.setFont("/resources/pac_font_sprites.png",8,8);
        menu.setTitle("CREATE GAME");

        menu.addImageSpinnerOption("Character ", null, isPacmanPlayed, 0, 4, sprites);

        /* Prócz uruchomienia servera trzeba tutaj uruchomić jednego klienta lokalnie */
        menu.addMenuOption("Start", ()-> {
            executor.submit(callableStartSever);
            ipString.value = "localhost";
            //portString.value - takie jak zostało odczytane z MENU, czyli bez zmian
            playerNumber.value = 0;
            executor.submit(callableStartClient);
            gotoMenu("display_connected_players");
            return 1;
        });

        menu.addSpinnerOption("Plrs Amout: ", null, playersAmount, 2, 4);
        menu.addStringInputOption("Name: ", null, playerName, null, 7);
        menu.addNumberInputOption("Port: ",null,portString,null,4);

        menu.addMenuOption("BACK", () -> {
            gotoMenu("server_setup");
            return 1;
        });
        menu.addButtonPressOption("exitOnQ",()-> {
            running = false;
            return 1;
        }, "q" );
    }

//    Callable<Void> callableDisplayConnectedClients = () -> {
//        displayConnectedClients(packReceivedFromServer);
//        return null;
//    };

    Callable<Void> callableStartSever = () -> {
        startServer();
        return null;
    };

    Callable<Void> callableStartClient = () -> {
        startClient(ipString.value, portString.value, playerNumber.value);
        return null;
    };


    private void startClient(String addressIP, String port, int playerID){
        Client client = new Client(addressIP, new Integer(port), playerID);

        String name;
        String character;
        String pressedKey;

        while (true){
            name = playerName.value;
            // TODO - zrobić jak będzie działał wybór postaci
            character = "ZASLEPKA";
            pressedKey = checkPressedKeys();

            // TODO - wywalić to opóźnienie
            try{
                 sleep(2000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            client.setObjToSendToServer(new PackToSendToServer(name, character, pressedKey));

            // odebranie obiektów od serwera i symulacja wyświetlenia obiektów na mapie
            while (!client.getListInputPackages().isEmpty()){
                packReceivedFromServer = client.getListInputPackages().getLast();
                client.getListInputPackages().removeLast();
//                ArrayList<TestObjectToSend> objList = temp.getObjectsList();
//                for (TestObjectToSend obj : objList){
//                    System.out.print("objReceivedFromServer.ilosc = " + obj.ilosc
//                            + " objReceivedFromServer.nazw = " + obj.nazwa + "\n");
//                }
                System.out.print("Connected clients: \n");
                for ( int i = 0; i < packReceivedFromServer.getConnectedClients().size(); i++){
                    System.out.print("\t- " + packReceivedFromServer.getConnectedClients().get(i) + "\n");
                }
                System.out.print("Waiting for: " + packReceivedFromServer.getNotConnectedClients() +
                        " players\n");
            }
        }
    }

    private void  startServer(){
        int port = new Integer(portString.value);
        listening = true;
        Server server = new Server(port, playersAmount.value);
        TestObjectToSend testObj = new TestObjectToSend();
        ArrayList<TestObjectToSend> objList = new ArrayList<>();
        ServerThread.setServerIntoUnlockMode();
        packOutToClient = new PackReceivedFromServer<>();
        while (listening) {
            if (ServerThread.getObjReceived() != null){
                // odbieranie obiektu
                putToArrayDataReceivedFromServer(ServerThread.getObjReceived());
                for (PackToSendToServer pack : arrayWithDataFromPlayers){
                    System.out.print("name = " + pack.getPlayersName() + ", character = " +
                    pack.getCharacter() + ", pressedKey = " + pack.getPressedKey() + "\n");
                }
//                System.out.print("Name = " + ServerThread.getObjReceived().getPlayersName()
//                        + ", Character = " + ServerThread.getObjReceived().getCharacter()
//                        + ", PressedKey = " + ServerThread.getObjReceived().getPressedKey() + "\n");
//
                // symulacja przetwarznia obiektu
                testObj.ilosc = 0;
                testObj.nazwa = ("asdf " +  ServerThread.getObjReceived().getPlayersName()
                        + " hwdp " + ServerThread.getObjReceived().getCharacter()
                        + " jp100 " + ServerThread.getObjReceived().getPressedKey() );
                objList.clear();
                for (int i = 0; i < 4; i ++){
                    objList.add(testObj);
                }
                // wysyłanie obiektu
                packOutToClient.addList(objList);
                ServerThread.setObjToSend(packOutToClient);


                ServerThread.setObjReceived(null);
            }
        }
    }

    private void stopServer(){
        listening = false;
    }

    synchronized private void putToArrayDataReceivedFromServer
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
            packOutToClient.setNotConnectedClients(Server.getClientAmount()
                    - ServerThread.getConnectedClients());
        }
        else{
            arrayWithDataFromPlayers.set(positionInArray, packReceivedFromclient);
        }
        // nie jest to klientom do niczego potrzebne, tak tylko testowo to przesyłam...
        packOutToClient.setAdditionalInfo(packReceivedFromclient.getPressedKey());
    }

    synchronized private PackToSendToServer getDataReceivedFromServer(int index){
        return arrayWithDataFromPlayers.get(index);
    }

    private void joinGameMenu() {
        MenuObject menu = (MenuObject)createObject(MenuObject.class);
        menu.setFont("/resources/pac_font_sprites.png",8,8);
        menu.setTitle("JOIN GAME");

        //menu.addNumberInputOption("IP: ",null,ipString,"xxx.xxx.x.xx",9);
        menu.addImageSpinnerOption("Character ", null, isPacmanPlayed, 0, 4, sprites);
        menu.addMenuOption("Join",() -> {
            // TODO - UWAGA - na koniec wywalić poniższą linijkę, bo docelowo ma być bez zmian!!!
            // TODO - takie jak zostało odczytane z MENU !!!
            ipString.value = "localhost";
//            portString.value - takie jak zostało odczytane z MENU, czyli bez zmian
//            playerNumber.value - takie jak zostało odczytane z MENU, czyli bez zmian
            executor.submit(callableStartClient);
            gotoMenu("display_connected_players");
            return 1;
        });
        menu.addStringInputOption("Name: ", null, playerName, null, 7);
        menu.addSpinnerOption("Player ID: ", null, playerNumber, 1, 3);
        menu.addNumberInputOption("IP: ",null,ipString,"xxx.xxx.x.xx",9);
        menu.addNumberInputOption("Port: ",null,portString,null,4);
        menu.addMenuOption("BACK", () -> {
            gotoMenu("server_setup");
            return 1;
        });
        menu.addButtonPressOption("exitOnQ",()-> {
            running = false;
            return 1;
        }, "q" );
    }
    
    private void windowInit(boolean fullscreen) {
        // Adds a new JFrame with a single JPanel.
        
        gameWindow = new JFrame("Testing");
        
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Insets tmp_ins = gameWindow.getInsets();
        gameWindow.setSize(
                2*256+tmp_ins.left+tmp_ins.right,
                2*224+tmp_ins.top+tmp_ins.bottom);
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setVisible(true);
        
        gameRenderer = new JPanel();
        gameWindow.add("Center",gameRenderer);
        
        // Fullscreen.
        
        if (fullscreen) gameWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private void keyboardInit() {
        // Adds a new listener for following keys:
        
        gameWindow.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                keyChar = e.getKeyChar();
                switch (e.getKeyCode()) {
                    case (KeyEvent.VK_LEFT): leftPressed = true;
                        break;
                    case (KeyEvent.VK_RIGHT): rightPressed = true;
                        break;
                    case (KeyEvent.VK_UP): upPressed = true;
                        break;
                    case (KeyEvent.VK_DOWN): downPressed = true;
                        break;
                    case (KeyEvent.VK_ESCAPE): escapePressed = true;
                        break;
                    case (KeyEvent.VK_ENTER): enterPressed = true;
                        break;
                    case (KeyEvent.VK_Q): qPressed = true; 
                        break;
                    case (KeyEvent.VK_BACK_SPACE): backspacePressed = true; 
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (keyChar == e.getKeyChar()) keyChar = 0;
                switch (e.getKeyCode()) {
                    case (KeyEvent.VK_LEFT): leftPressed = false; 
                        break;
                    case (KeyEvent.VK_RIGHT): rightPressed = false; 
                        break;
                    case (KeyEvent.VK_UP): upPressed = false;
                        break;
                    case (KeyEvent.VK_DOWN): downPressed = false;
                        break;
                    case (KeyEvent.VK_ESCAPE): escapePressed = false;
                        break;
                    case (KeyEvent.VK_ENTER): enterPressed = false;
                        break;
                    case (KeyEvent.VK_Q): qPressed = false;
                        break;
                    case (KeyEvent.VK_BACK_SPACE): backspacePressed = false; 
                        break;
                }
            }
        });
    }
    
    private void keyboardSetHold() {
        if ((!backspaceHold) && (backspacePressed))  backspaceHoldCounter = 20;
        else {
            if (backspaceHoldCounter > 0) backspaceHoldCounter--;
        }
        
        leftHold = leftPressed;
        rightHold = rightPressed;
        upHold = upPressed;
        downHold = downPressed;
        escapeHold = escapePressed;
        enterHold = enterPressed;
        qHold = qPressed;
        backspaceHold = backspacePressed;
        
        prevKeyChar = keyChar;
    }
    
    public static void main(String[] args) {
        Game new_game = new Game();
        new_game.init();
    }
    
    private JFrame gameWindow;
    private JPanel gameRenderer;
    private boolean running;
    
    private int globalCounter;
    
    private ArrayList<GameObject> objectList;
    
    private int framesPerSecond;
    private int framesSkip;
    private int max_render_skip;
    
    private static volatile StringWrapper ipString;
    private static volatile StringWrapper portString;
    private StringWrapper playerName;
    
    private IntWrapper isPacmanPlayed;
    private IntWrapper playersAmount;
    private static volatile IntWrapper playerNumber;
   
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean downPressed;
    private boolean escapePressed;
    private boolean enterPressed;
    private boolean qPressed;
    private boolean backspacePressed;

    private boolean leftHold;
    private boolean rightHold;
    private boolean upHold;
    private boolean downHold;
    private boolean escapeHold;
    private boolean enterHold;
    private boolean qHold;
    private boolean backspaceHold;
    private int backspaceHoldCounter;
    
    private char prevKeyChar;
    private char keyChar;
    
    private int gameScore;
    private int gameLives;
    private IntWrapper startingLives;
    
    private double drawScale;
    private int drawCenterX, drawCenterY;
    
    private boolean isPlayedGhostCreated = false;

    private boolean listening = false;


    ArrayList<Sprite> sprites = new ArrayList();

    private ExecutorService executor = Executors.newFixedThreadPool(4);

    private ArrayList<PackToSendToServer> arrayWithDataFromPlayers = new ArrayList<>();

    // TODO - zmienić typ argumentu na chyba GameObject
    private PackReceivedFromServer<TestObjectToSend> packOutToClient;
    private static volatile PackReceivedFromServer<TestObjectToSend> packReceivedFromServer;


    public boolean isPlayedGhostCreated(){
        return isPlayedGhostCreated;
    }
    
    public void setPlayedGhostCreated(boolean is){
        isPlayedGhostCreated = is;
    }
    
    public void close(){
        running = false;
    }
    
    public void addLives(int p){
        gameLives += p;
        if (gameLives < 0) gameLives = 0;
    }
    
    public int getLives() {
        return gameLives;
    }
    
    public void setLives(int p){
        if(p > 0) gameLives = p;
    }
    
    public int getStartingLives(){
        return startingLives.value;
    }
    
    public boolean isPacmanPlayed(){
        return (isPacmanPlayed.value == 0);
    }
    
    public int getNumberOfGhosts(){
        return playersAmount.value;
    }
    
    public void addScore(int p){
        gameScore += p;
        if (gameScore < 0) gameScore = 0;
    }
    
    public int getScore() {
        return gameScore;
    }
    
    public void setScore(int s) {
        if (s > 0) gameScore = s;
        else gameScore = 0;
    }
}

