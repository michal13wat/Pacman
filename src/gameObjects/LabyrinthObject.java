
package gameObjects;

import gameObjects.SpawnerObject;
import menuAndInterface.TextObject;
import gameObjects.NeutralizerObject;
import gameObjects.PacmanObject;
import gameObjects.GhostObject;
import gameObjects.DotObject;
import gameObjects.CherryObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;

import java.io.*;
import javax.imageio.ImageIO;
import pacman.ClientGame;
import pacman.ServerGame;

public class LabyrinthObject extends GameObject implements Serializable {
    @Override
    public void createEvent() {
        x = 16;
        y = 16;
        
        sizeMod = 16;
    }
    
    @Override
    public void stepEvent() {
        if (sourceFile == null) return;
        
        // Ładowanie poziomu.
        if (counter == 0) labyrinthInit();
        
        // Zwycięstwo.
        if ((game.getAllObjects(DotObject.class).isEmpty()) && (finished == false)) {
            finished = true;
            
            ArrayList<GameObject> l = game.getAllObjects(GhostObject.class);
            GhostObject o = null;
            
            for (int i = 0; i < l.size(); i ++) {
                o = (GhostObject)l.get(i);
                o.scare(9999);
            }
            
            PacmanObject pacman = (PacmanObject)game.getObject(PacmanObject.class);
            pacman.getCaught(null);
        }
        
        // Wyjście.
        if (game.keyboardCheck("q")) {
            game.setPlayedGhostCreated(false);
            destroy();
        }
        
        counter ++;
    }
    
    @Override
    public void drawEvent(Graphics2D g) {
        if (scoreDisplay != null) scoreDisplay.setText(String.format("%03d",game.getScore()));
        if (livesDisplay != null) {
            String s = "";
            for (int i = 0; i < game.getLives(); i ++) s += (char)201;
            livesDisplay.setText(s);
        }
        if (endDisplay != null) {
            endDisplay.setText("");
            if (counter%10 >= 5) {
                if (game.getAllObjects(DotObject.class).isEmpty()){
                    if ((game instanceof ClientGame) || (game instanceof ServerGame))
                    {endDisplay.setText("PACMAN WINS!!!");endDisplay.setPosition(endDisplay.getX(),48);}
                    else if (game.getPacmanPlayer() == 0)endDisplay.setText("YOU WIN !!!");
                    else endDisplay.setText("YOU LOSE !!!");
                }
                if (game.getLives() == 0) { ////////////////////// TUTAJ SIĘ ZAWIESZA(Ł) SERWER!!!!!!! ///////////////////////////////////////
                    if ((game instanceof ClientGame) || (game instanceof ServerGame))
                    {endDisplay.setText("GHOSTS WIN!!!");endDisplay.setPosition(endDisplay.getX(),48);}
                    else if (game.getPacmanPlayer() == 0)endDisplay.setText("YOU LOSE !!!");
                    else endDisplay.setText("YOU WIN !!!");
                }
            }
        }
        
        if (tileset == null) {
            g.setColor(Color.WHITE);
            for (int i = 0; i < width; i++){
                for (int j = 0; j < height; j++) {
                    if (myCollisionMap[i][j] == true) g.fillRect(x+sizeMod*i,y+sizeMod*j,16,16);
                }
            }
        }
        else {
            for (int i = 0; i < width; i++){
                for (int j = 0; j < height; j++) {
                    if (myCollisionMap[i][j]) drawBlock(g,i,j,getFragmentLocations(i,j));
                }
            }
        }
    }
    
    @Override
    public void destroyEvent() {
        ArrayList<GameObject> l = game.getAllObjects(GameObject.class);
        
        for (int i = 0; i < l.size(); i ++) {
            l.get(i).destroy();
        }
        
        /*nameDisplay.destroy();
        scoreDisplay.destroy();
        livesDisplay.destroy();
        endDisplay.destroy();*/
        
        game.gotoMenu("stage_select");
    }
    
    /*@Override
    public boolean sendMe() {
        if ((sent == false) && (sourceFile != null)) {
            System.out.println("SERWER - MOŻNA PRZESŁAĆ LABIRYNT");
            sent = true;
            return true;
        }
        return false;
    }*/
    
    public boolean checkCollision(int x, int y) {
        return (myCollisionMap[bounds(0,x,width-1)][bounds(0,y,height-1)]);
    }
    
    public boolean checkCollisionScaled(double x, double y) {
        return (myCollisionMap
                [bounds(0,(int)Math.floor(x/sizeMod),width-1)]
                [bounds(0,(int)Math.floor(y/sizeMod),height-1)]);
    }
    
    private void labyrinthInit() {
        // Początkowe załadowanie labiryntu.
        try {
            InputStream fi = game.loadLabyrinth(sourceFile,fromJar);
            if (fi == null) return;
            sizeInput(fi);
            fi.close();
            
            fi = game.loadLabyrinth(sourceFile,fromJar);
            if (fi == null) return;
            layoutInput(fi);
            fi.close();
        }
        catch (Exception e) {}
        
        // Ustawienie wyświetlaczy tesktu.
        nameDisplay = (TextObject)createObject(TextObject.class,width+1,0);
        nameDisplay.loadFont("pac_font_sprites",8,8);
        nameDisplay.setText(labyrinthName);
        endDisplay = (TextObject)createObject(TextObject.class,width+1,3);
        endDisplay.loadFont("pac_font_sprites",8,8);

        scoreDisplay = (TextObject)createObject(TextObject.class,width+1,1);
        scoreDisplay.loadFont("pac_font_sprites",8,8);
        scoreDisplay.setPrefix("Score:");
        livesDisplay = (TextObject)createObject(TextObject.class,width+1,2);
        livesDisplay.loadFont("pac_font_sprites",8,8);

        // Inicjalizacja poziomu.
        game.setScore(0);
        game.setLives(game.getStartingLives());
        if ((game.chosenCharacter.value != -1)
                && !((game instanceof ClientGame) || (game instanceof ServerGame)))
            game.chooseCharacter(true,0);

        tileset = "pac_labyrinth_tileset";
    }
    
    private void sizeInput(InputStream in) {
        // Alokacja tablicy labiryntu.=
        int c, tmp_width = -1;
        width = -1;
        height = 1;
        
        try {
            while ((c = in.read()) != '\n') {/**/}
            
            while ((c = in.read()) != -1) {
                // Nowy znak.
                if (c != '\n') {
                    if (width == -1) tmp_width ++;
                }
                // Nowa linia.
                else {
                    if (width == -1) width = tmp_width;
                    height ++;
                }
            }
            
            width++;
            myCollisionMap = new boolean[width][height];
        } catch (IOException i) {
            myCollisionMap = new boolean[1][1];
            myCollisionMap[0][0] = false;
        }
        
        try {
            in.close();
        } catch (IOException i) {
            return;
        }
        
        System.out.println("Labyrinth is " + width + " x " + height);
    }
    
    private void layoutInput(InputStream in) {
        // Czytanie układu poziomu do tablicy.
        int c = 0;
        
        try {
            labyrinthName = "";
            while ((c = in.read()) != '\n') labyrinthName += Character.toString((char)c);
            
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    // Domyślnie, każde pole jest puste.
                    c = in.read();
                    myCollisionMap[i][j] = false;
                    
                    switch (c)
                    {
                        // Ściana 
                        case '0': myCollisionMap[i][j] = true;
                        break;
                        
                        // Mały punkt
                        case '-': createObject(DotObject.class,i,j);
                        break;
                        
                        // Neutralizator duchów
                        case '+': createObject(NeutralizerObject.class,i,j);
                        break;
                        
                        // Spawner wisienek
                        case '$': {
                            SpawnerObject o = (SpawnerObject)createObject(SpawnerObject.class,i,j);
                            o.setSpawner(CherryObject.class,1,600,450+450*j/height,true);
                        }
                        break;
                        
                        // Pacman 
                        case '#': createObject(PacmanObject.class,i,j);
                        break;
                        
                        // Spawner duchów
                        case '^': {
                            SpawnerObject o = (SpawnerObject)createObject(SpawnerObject.class,i,j);
                            o.setSpawner(GhostObject.class,game.getNumberOfGhosts(),100,60,false);
                        }
                        break;
                    }
                }

                // Nowa linia.
                while (c != -1) {
                    if ((c = in.read()) == '\n') break;
                }
            }
        } catch (IOException i) {}
        
        try {
            in.close();
        } catch (IOException i) {
            return;
        }
    }
    
    private ArrayDeque<Point> getFragmentLocations(int x, int y) {
        ArrayDeque<Point> v = new ArrayDeque<>();
        
        for (int j = 0; j < 2; j++){
            for (int i = 0; i < 2; i++) {
                // Top/Bottom
                if (checkCollision(x,y-1+2*j)) {
                    // Top & Side
                    if (checkCollision(x-1+2*i,y)) {
                        // Solid 
                        if (checkCollision(x-1+2*i,y-1+2*j)) v.add(new Point(6,0));
                        // Little corner
                        else v.add(new Point(4+i,0+j));
                    }
                    // Vertical Wall 
                    else v.add(new Point(0+i,0+i));
                }
                // No Top
                else {
                    // Horizontal Wall 
                    if (checkCollision(x-1+2*i,y)) v.add(new Point(1-j,0+j));
                    // Big Corner 
                    else v.add(new Point(2+i,0+j));
                }
            }
        }
        
        return v;
    }
    
    private void drawBlock(Graphics2D g, int blockX, int blockY, ArrayDeque<Point> v) {
        // Tworzenie kompozytowego bloczka z czterech elementów 8x8.
        Point tmpPoint;
        int tilesetX, tilesetY;
        
        for (int j = 0; j < 2; j++){
            for (int i = 0; i < 2; i++) {
                tmpPoint = v.remove();
                tilesetX = tmpPoint.x;
                tilesetY = tmpPoint.y;

                g.drawImage(
                    getSprites(tileset),
                    (int)(scaleMod*(x+sizeMod*(2*blockX+i)/2))+screenCenterX,
                    (int)(scaleMod*(y+sizeMod*(2*blockY+j)/2))+screenCenterY,
                    (int)(scaleMod*(x+sizeMod*(2*blockX+i+1)/2))+screenCenterX,
                    (int)(scaleMod*(y+sizeMod*(2*blockY+j+1)/2))+screenCenterY,
                    8*tilesetX,8*tilesetY,8*tilesetX+8,8*tilesetY+8,
                    null);
            }
        }
    }
    
    @Override
    protected GameObject createObject(Class ourClass, int i, int j) {
        // Wzywa GameObject do tego.
        GameObject o = game.createObject(ourClass);
        
        o.setCollisionMap(this);
        o.setPosition(sizeMod*i,sizeMod*j);
        o.setOrigin(x,y);
        
        return o;
    }
    
    int width, height, sizeMod;
    boolean myCollisionMap[][];
    
    String sourceFile;
    String labyrinthName;
    
    TextObject nameDisplay = null;
    TextObject livesDisplay = null;
    TextObject scoreDisplay = null;
    TextObject endDisplay = null;
    String tileset;
    
    boolean finished = false;
    boolean fromJar = false;
    
    public void setSource(String f, boolean fromJar) {
        System.out.println("LABIRYNTH loading as " + f);
        this.sourceFile = f;
        this.fromJar = fromJar;
        this.counter = 0;
    }
    
    public int getWidth() {
        return (width+1)*sizeMod;
    }
    
    public int getHeight() {
        return (height+1)*sizeMod;
    }
}
