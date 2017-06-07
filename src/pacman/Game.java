package pacman;

//import _serverV2.ServerBrain;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import java.util.*;

import java.io.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gameObjects.*;
import java.net.URL;
import javax.imageio.ImageIO;
import menuAndInterface.*;

public class Game extends Thread
{
    public static void main(String[] args) {
        Game new_game = new Game();
        new_game.init();
    }
    
    class GameObjectComparator implements Comparator<GameObject> {
        // Klasa do sortowania obiektów po głębokości rysowania.
        
        @Override
        public int compare(GameObject obj1, GameObject obj2){
            int d1 = obj1.getDepth(), d2 = obj2.getDepth();
            
            if (d1 > d2) return -1;
            else if (d1 < d2) return 1;
            else return 0; 
        }
    }
    
    //////////////////////////////////////////////////////////////////////
    // Inicjalizacja.
    //////////////////////////////////////////////////////////////////////
    
    public void init(){
        // Parametry gry.
        running = true;
        
        framesPerSecond = 60;
        framesSkip = 1000/framesPerSecond;
        max_render_skip = 10;
        
        ipString = new StringWrapper("127.0.0.1");
        portString = new StringWrapper("7171");
        
        wrapperInit();
        
        objectList = new ArrayList();
        
        // Window init.
        windowInit(false);
        preloadSprites();
        // Keyboard init.
        keyboardControl.keyboardInit();
        
        gotoMenu("start");
        //createObject(LabyrinthObject.class);
        
        globalCounter = 0;
        gameLoop();
    }
    
    protected void windowInit(boolean fullscreen) {
        // Nowe JFrame z pojedynczym JPanel'em.
        gameWindow = new JFrame("PACMAN");
        
        URL dirURL = getClass().getResource("/resources/stages");
        isJar = (dirURL.getProtocol().equals("jar"));
        
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Insets tmp_ins = gameWindow.getInsets();
        gameWindow.setSize(
                2*256+tmp_ins.left+tmp_ins.right,
                2*224+tmp_ins.top+tmp_ins.bottom);
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setVisible(true);
        
        gameRenderer = new JPanel();
        gameWindow.add("Center",gameRenderer);
        
        // Pełen ekran.
        if (fullscreen) gameWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    protected void wrapperInit() {
        
        playerName = new StringWrapper("PLAYER");
        chosenCharacter = new IntWrapper(0);

        startingLives = new IntWrapper(3);
        playersAmount = new IntWrapper(4);
        ghostsAmount = new IntWrapper(4);
        playerNumber = new IntWrapper(1);

        pacmanPlayer = new IntWrapper(-1);
        ghostPlayer = new IntWrapper[4];
        for (int i = 0; i < 4; i++)
            ghostPlayer[i] = new IntWrapper(-1);
    }
    
    protected void preloadSprites() {
        
        /*sprites.add(new Sprite("/resources/pac_hero_sprites.png",0,0,16,16));
        sprites.add(new Sprite("/resources/pac_ghost_sprites.png",0,0,16,16));
        sprites.add(new Sprite("/resources/pac_ghost_sprites.png",0,1,16,16));
        sprites.add(new Sprite("/resources/pac_ghost_sprites.png",0,2,16,16));
        sprites.add(new Sprite("/resources/pac_ghost_sprites.png",0,3,16,16));*/
        
        try {
            
            spriteSheets.put("pac_hero_sprites",ImageIO.read(getClass().getResource("/resources/pac_hero_sprites.png")));
            spriteSheets.put("pac_ghost_sprites",ImageIO.read(getClass().getResource("/resources/pac_ghost_sprites.png")));
            spriteSheets.put("pac_particle_sprites",ImageIO.read(getClass().getResource("/resources/pac_particle_sprites.png")));
            spriteSheets.put("pac_collectible_sprites",ImageIO.read(getClass().getResource("/resources/pac_collectible_sprites.png")));
            spriteSheets.put("pac_labyrinth_tileset",ImageIO.read(getClass().getResource("/resources/pac_labyrinth_tileset.png")));
            spriteSheets.put("pac_font_sprites",ImageIO.read(getClass().getResource("/resources/pac_font_sprites.png")));
        }
        catch (Exception e) {}
        
    }
    
    //////////////////////////////////////////////////////////////////////
    // Główne metody pętli gry.
    //////////////////////////////////////////////////////////////////////
    
    protected void gameLoop() {
        // Konsystentne FPS.
        double nextStep = System.currentTimeMillis();
        int loops;
        
        while (running){
            loops = 0;
            
            while ((System.currentTimeMillis() > nextStep) && (loops < max_render_skip) && (!halted)) {
                gameStep();
                
                nextStep += framesSkip;
                globalCounter ++;
                loops ++;
                
                if ((System.currentTimeMillis() <= nextStep) || (loops >= max_render_skip)) {
                    if (running) gameDraw();
                }
                
                if (keyboardCheck("escape")) running = false;
            }
        }
        
        System.out.println("Zamykamy grę!");
        gameWindow.setVisible(false);
        gameWindow.dispose();
    }
    
    protected void gameStep() {
        // Uruchamia kod "stepEvent" dla każdego obiektu,
        // zmieniając jego stan. Kasuje "zniszczone" obiekty.
        
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
        
        keyboardControl.keyboardSetHold();
        for (int player : keyboardControlRemote.keySet())
            keyboardControlRemote.get(player).keyboardSetHold();
    }
    
    protected void gameDraw() {
        // Podobnie do gameStep, tyle, że rysuje wszystkie
        // obiekty zamiast wywoływać kod do zmiany ich stanów.
        if ((clientGame != null) || (halted)) return;
        
        BufferedImage buf = new BufferedImage(
                gameWindow.getSize().width,gameWindow.getSize().height,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D)buf.getGraphics();
        
        // Sortowanie wg. głębokości rysowania.
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
    
    //////////////////////////////////////////////////////////////////////
    // Inne metody związane z pętlą gry.
    //////////////////////////////////////////////////////////////////////
    
    protected void setSize() {
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
                //System.out.print("########  Złąpano wyjątek związany ze skalowaniem!!!! #########\n");
            }
        }
    }
    
    //////////////////////////////////////////////////////////////////////
    // Metody manipulacji obiektami.
    //////////////////////////////////////////////////////////////////////
    
    public GameObject createObject(Class ourClass){
        // Full procedure for adding a new object into the game.
        
        GameObject gameObject;
        
        try{
            gameObject = (GameObject) ourClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException i) {
            gameObject = new TestObject();
        }
        
        gameObject.setGame(this);
        
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
    
    //////////////////////////////////////////////////////////////////////
    // Konkretne metody do różnych celów.
    //////////////////////////////////////////////////////////////////////
    
    public InputStream loadLabyrinth(String fileName, boolean fromJar) {
        // Jednorazowe ładowanie labiryntu.
        InputStream fi = null;
        
        System.out.println("Labirynt: "+fileName);
        
        if (fromJar == true) {
            try {fi = (getClass().getResourceAsStream(fileName));}
            catch (Exception e) {System.out.println("Błąd w ładowaniu labiryntu.");}
        }
        else {
            try {fi = new FileInputStream(fileName);}
            catch (Exception e) {System.out.println("Błąd w ładowaniu labiryntu.");}
        }
        
        return fi;
    }
    
    public void halt() {
        // Zatrzymuje grę, tak, że nie robi żadnych Step/Draw Events.
    }
    
    public void endGame(boolean victory) {
        isPlayedGhostCreated = false;
        GameObject labirynt = getObject(LabyrinthObject.class);
        labirynt.destroy();
        
        gotoMenu("stage_select");
    }
    
    public void chooseCharacter(boolean resetPreviousChoices, int forPlayer) {
        // Przetwarza wartości z chosenCharacter na wybór konkretnej postaci
        // dla konkretnego gracza. Odpala odpowiednie metody u postaci, aby zatwierdzić.
        System.out.println("Przypisywanie postaci rozpoczęte.");
        
        if (resetPreviousChoices) {
            pacmanPlayer.value = -1;
            for (int i = 0; i < 4; i++)
                ghostPlayer[i].value = -1;
        }
        
        if (chosenCharacter.value == 0) {
            // Wybraliśmy Pacmana.
            System.out.println("Pacman przypisany dla gracza " + forPlayer);
            pacmanPlayer.value = forPlayer;
        }
        else if (chosenCharacter.value > 0) {
            // Wybraliśmy któregoś z duchów.
            System.out.println("Duszek przypisany dla gracza " + forPlayer);
            ghostPlayer[chosenCharacter.value-1].value = forPlayer;
        }
        
        for (GameObject o : getAllObjects(PacmanObject.class))
            ((PacmanObject)o).setPlayed();
        for (GameObject o : getAllObjects(GhostObject.class))
            ((PacmanObject)o).setPlayed();
        
        System.out.println("Przypisywanie postaci zakończone.");
    }
    
    //////////////////////////////////////////////////////////////////////
    // Dostęp do menu i klawiatury.
    //////////////////////////////////////////////////////////////////////
    
    public void gotoMenu(String which){
        menuControl.gotoMenu(which);
    }
    
    public KeyboardControl getKeyboard(int i) {
        if (keyboardControlRemote.containsKey(i)) return keyboardControlRemote.get(i);
        return keyboardControl;
    }
    
    // Lokalna klawiatura.
    public boolean keyboardCheck(String key){///!!!!!!!!!!!!!!
        return keyboardCheck(key,0);
    }
    
    public boolean keyboardHoldCheck(String key){///!!!!!!!!!!!!!!
        return keyboardHoldCheck(key,0);
    }
    
    public char keyboardCharCheck() {
        return keyboardCharCheck(0);
    }

    protected String checkPressedKeys(){  ///!!!!!!!!!!!!!!
        return checkPressedKeys(0);
    }
    
    // Osobne klawiatury.
    public boolean keyboardCheck(String key, int player){
        return getKeyboard(player).keyboardCheck(key);
    }
    
    public boolean keyboardHoldCheck(String key, int player){
        return getKeyboard(player).keyboardHoldCheck(key);
    }
    
    public char keyboardCharCheck(int player) {
        return getKeyboard(player).keyboardCharCheck();
    }

    protected String checkPressedKeys(int player){
        return getKeyboard(player).checkPressedKeys();
    }
    
    //////////////////////////////////////////////////////////////////////
    // Sprawy serwerowe.
    //////////////////////////////////////////////////////////////////////
    
//    Callable<Void> callableDisplayConnectedClients = () -> {
//        displayConnectedClients(packReceivedFromServer);
//        return null;
//    };
    
    public Callable<Void> callableStartSever = () -> {
        //halt();
        startServer();
        return null;
    };

    //public Callable<Void> callableStartClient = () -> {
        //startClient(ipString.value, portString.value, playerNumber.value);
        //return null;
    //};
    
    public void startClient(String addressIP, String port, int playerID){
        /*clientBrain = new _clientV2.ClientBrain(addressIP, port, port + 1,
                playerName.value, playerID);
        clientBrain.start();*/

        //gotoMenu("display_connected_players");
        //while (true){}  // zablokoowanie programu

        // TODO - odkomentować poniże dwie linijki i wywalić to co powyżej
        clientGame = new ClientGame(gameWindow,gameRenderer,playerName,chosenCharacter);
        clientGame.init();
        
        // Jak skończymy działanie klienta, to kasujemy też serwer.
        if (serverGame != null) {
            serverGame.stopGame();
            serverGame = null;
        }
        
        clientGame = null;
        executor.shutdownNow();
        executor = Executors.newFixedThreadPool(4);
        System.out.println("Wracamy do menu.");
        gotoMenu("server_setup");
    }

    protected void  startServer(){
        int port = new Integer(portString.value);
        //serverBrain = new _serverV2.ServerBrain(port, port + 1, playersAmount.value);
        //serverBrain.start();

        /*String temp;

        try{
            Scanner sc = new Scanner(new File("src\\resources\\KrotnieDane5K.txt"));
            while (sc.hasNextLine()){
                if (!_serverV2.ServerBrain.checkIfAllThreadReadPrevPack()){
                    temp = sc.next();
                    while (true){
                        if (!_serverV2.ServerBrain.recPacks.isEmpty()){
                            temp += "\t\tPressedKey = " + _serverV2.ServerBrain.recPacks.getFirst().getPressedKey();
                            _serverV2.ServerBrain.recPacks.removeFirst();
                            break;
                        }
                        try{
                            Thread.sleep(3);
                        }catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("wpisanie do bufora wysłającego");
                    _serverV2.ServerBrain.packOut.setAdditionalInfo(temp);
                    _serverV2.ServerBrain.lockReadingNextPack();                //readPrevPack = true;
                    _serverV2.ServerBrain.lockBufferingToSend();

                    /*
                     Im to opóźnienie jest większe, tym mniej pakietów
                     jest gubionych po drodze. Dla:
                     10 ms gubionych jest ok 0,225%  - 60 FPS powinno spokojnie pociągnąć
                     20 ms gubionych jest ok 0,025%  - 30 FPS powinno działać
                            W sumie to nawet chyba mniej (wydaje mi się, że tylko
                            pierwszy pakiet)...
                     50 ms nic nie jest gubione ale działa powoli - max ~15 FPS

                     Czym to jest zasadniczo spowodowane?
                     Nie jestem pewnien, ale prawie na pewno chodzi o ustawianie i kasowanie
                     flag w kolekcjach:
                     sendPrevPack  i  readPrevPack, które są odpowiedzialne za synchronizację
                     i blokowanie wątków. Niestety nie potrafię sobie z tym poradzić
                    * */
                    /*try{
                        Thread.sleep(20);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Czytanie z pliku zakończone!");
        }catch (FileNotFoundException e){
            System.out.println("=========  Otwieranie pliku nie powiodło się!");
        }*/
        //while (true){}      // TODO - wywalić to

        // TODO - odkomentować poniże dwie linijki i wywalić to co powyżej
        serverGame = new ServerGame(portString,playersAmount);
        serverGame.init();
        /*int port = new Integer(portString.value);
        listening = true;
        MyServer server = new MyServer(port, playersAmount.value);*/
    }

    protected void stopServer(){
        listening = false;
    }
    
    //////////////////////////////////////////////////////////////////////
    // Pola obiektu.
    //////////////////////////////////////////////////////////////////////
    
    static boolean isJar;
    
    // Podstawowe rzeczy do gry.
    ArrayList<GameObject> objectList;
    
    boolean running;
    boolean halted;
    int globalCounter;
    
    int framesPerSecond;
    int framesSkip;
    int max_render_skip;
    
    // Podwykonawcy.
    ExecutorService executor = Executors.newFixedThreadPool(4);
    KeyboardControl keyboardControl = new KeyboardControl(this);
    MenuControl menuControl = new MenuControl(this);
    Random random = new Random();
    
    // Sprawy graficzne.
    HashMap<String,Image> spriteSheets = new HashMap<>();
    ArrayList<Sprite> sprites = new ArrayList();
    JFrame gameWindow;
    JPanel gameRenderer;
    
    double drawScale;
    int drawCenterX, drawCenterY;

    // Wrappery i zmienne samej rozgrywki.
    public StringWrapper playerName;
    public IntWrapper chosenCharacter;

    public IntWrapper startingLives;
    public IntWrapper playersAmount;
    public IntWrapper ghostsAmount;
    
    public IntWrapper pacmanPlayer;
    public IntWrapper[] ghostPlayer;
    
    public ArrayList<IntWrapper> characterBlocked;
    
    int gameScore;
    int gameLives;
    
    boolean isPlayedGhostCreated = false;
    boolean listening = false;
    
    // Sprawy serwerowe.
    public static volatile IntWrapper playerNumber;
    public static volatile StringWrapper ipString;
    public static volatile StringWrapper portString;
    ServerGame serverGame;
    ClientGame clientGame;
    // ----------------------------------------------
    _serverV2.ServerBrain serverBrain;
    _clientV2.ClientBrain clientBrain;
    
    // PO JEDNYM DLA POŁĄCZONEGO GRACZA!!!
    HashMap <Integer,KeyboardControlRemote> keyboardControlRemote = new HashMap<>();
    HashMap <Integer,Integer> playerNumbers;
    HashMap <Integer,String> playerNames;
    HashMap <Integer,Integer> playerCharacters;
    HashMap <Integer,Boolean> playerReady;
    
    //////////////////////////////////////////////////////////////////////
    // Akcesory i inne śmieci.
    //////////////////////////////////////////////////////////////////////
    
    public boolean isPlayedGhostCreated(){
        return isPlayedGhostCreated;
    }
    
    public void setPlayedGhostCreated(boolean is){
        isPlayedGhostCreated = is;
    }
    
    public boolean isRunning(){
        return running;
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
    
    public int getNumberOfGhosts(){
        return ghostsAmount.value;
    }
    
    public int getMaxPlayers(){
        return playersAmount.value;
    }
    
    public void addScore(int p){
        gameScore += p;
        if (gameScore < 0) gameScore = 0;
    }
    
    public int getPacmanPlayer() {
        return pacmanPlayer.value;
    }
    
    public int getGhostPlayer(int color) {
        return ghostPlayer[color].value;
    }
    
    public int getScore() {
        return gameScore;
    }
    
    public void setScore(int s) {
        if (s > 0) gameScore = s;
        else gameScore = 0;
    }
    
    public Image getSpriteSheet(String name) {
        return spriteSheets.get(name);
    }
    
    public JFrame getGameWindow() {
        return gameWindow;
    }
    
    public ExecutorService getExecutor() {
        return executor;
    }
    
    public ArrayList<Integer> getPlayerIds() {
        ArrayList<Integer> sortedIds = new ArrayList<>();
        for (int id : playerNumbers.keySet()) {
            if (playerReady.get(id) == true)
                sortedIds.add(/*playerNumbers.get(id),*/id);
        }
        return sortedIds;
    }
    
    public String getPlayerName(int id) {
        return playerNames.get(id);
    }
    
    public int getPlayerCharacter(int id) {
        return playerCharacters.get(id);
    }
}
