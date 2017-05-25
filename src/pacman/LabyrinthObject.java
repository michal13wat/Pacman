
package pacman;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;

import java.io.*;
import javax.imageio.ImageIO;

public class LabyrinthObject extends GameObject {
    @Override
    public void createEvent() {
        x = 16;
        y = 16;
        
        sizeMod = 16;
    }
    
    @Override
    public void stepEvent() {
        if (sourceFile == null) return;
        
        // Loading the stage.
        if (counter == 0) labyrinthInit();
        
        // Victory.
        if ((game.getAllObjects(DotObject.class).isEmpty()) && (finished == false)) {
            finished = true;
            
            ArrayList<GameObject> l = game.getAllObjects(GhostObject.class);
            GhostObject o = null;
            
            for (int i = 0; i < l.size(); i ++) {
                o = (GhostObject)l.get(i);
                o.scare(9999);
            }
            
            ((PacmanObject)game.getObject(PacmanObject.class)).getCaught(null);
        }
        
        // Exit.
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
            for (int i = 0; i < game.getLives(); i ++) s += "`";
            livesDisplay.setText(s);
        }
        if (endDisplay != null) {
            endDisplay.setText("");
            if (counter%10 >= 5) {
                if (game.getAllObjects(DotObject.class).isEmpty()){
                    if (game.isPacmanPlayed())endDisplay.setText("YOU WIN !!!");
                    else endDisplay.setText("YOU LOSE !!!");
                } 
                if (game.getLives() == 0) {
                    if (game.isPacmanPlayed())endDisplay.setText("YOU LOSE !!!");
                    else endDisplay.setText("YOU WIN !!!");
                }
            }
        }
        
        if (tileset == null) {
            g.setColor(Color.WHITE);
            for (int i = 0; i < width; i++){
                for (int j = 0; j < height; j++) {
                    if (collisionMap[i][j] == true) g.fillRect(x+sizeMod*i,y+sizeMod*j,16,16);
                }
            }
        }
        else {
            for (int i = 0; i < width; i++){
                for (int j = 0; j < height; j++) {
                    if (collisionMap[i][j]) drawBlock(g,i,j,getFragmentLocations(i,j));
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
        
        game.gotoMenu("start");
    }
    
    public boolean checkCollision(int x, int y) {
        return (collisionMap[bounds(0,x,width-1)][bounds(0,y,height-1)]);
    }
    
    public boolean checkCollisionScaled(double x, double y) {
        return (collisionMap
                [bounds(0,(int)Math.floor(x/sizeMod),width-1)]
                [bounds(0,(int)Math.floor(y/sizeMod),height-1)]);
    }
    
    private void labyrinthInit() {
        // Loading the necessary files.
        try {
            sizeInput(new FileInputStream(sourceFile));
            layoutInput(new FileInputStream(sourceFile));
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found");
            destroy();
            return;
        }

        // Setting up the menu display.
        nameDisplay = (TextObject)createObject(TextObject.class,width+1,0);
        nameDisplay.loadFont("/resources/pac_font_sprites.png",8,8);
        nameDisplay.setText(labyrinthName);
        endDisplay = (TextObject)createObject(TextObject.class,width+1,3);
        endDisplay.loadFont("/resources/pac_font_sprites.png",8,8);

        scoreDisplay = (TextObject)createObject(TextObject.class,width+1,1);
        scoreDisplay.loadFont("/resources/pac_font_sprites.png",8,8);
        scoreDisplay.setPrefix("Score:");
        livesDisplay = (TextObject)createObject(TextObject.class,width+1,2);
        livesDisplay.loadFont("/resources/pac_font_sprites.png",8,8);

        // Stage initiation.

        game.setScore(0);
        game.setLives(game.getStartingLives());

        try {
            tileset = ImageIO.read(getClass().getResource("/resources/pac_labyrinth_tileset.png"));
        } catch (IOException i) {
            tileset = null;
        }
    }
    
    private void sizeInput(InputStream in) {
        // Allocating the 2D array.
        
        int c, tmp_width = -1;
        width = -1;
        height = 1;
        
        try {
            while ((c = in.read()) != '\n') {/**/}
            
            while ((c = in.read()) != -1) {
                // New character. 
                if (c != '\n') {
                    if (width == -1) tmp_width ++;
                }
                // New line.
                else {
                    if (width == -1) width = tmp_width;
                    height ++;
                }
            }
        
            collisionMap = new boolean[width][height];
        } catch (IOException i) {
            collisionMap = new boolean[1][1];
            collisionMap[0][0] = false;
        }
        
        try {
            in.close();
        } catch (IOException i) {
            return;
        }
    }
    
    private void layoutInput(InputStream in) {
        // Reading the labyrinth layout into the array.
        
        int c = 0;
        
        try {
            labyrinthName = "";
            while ((c = in.read()) != '\n') labyrinthName += Character.toString((char)c);
            
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    // By default, the square is empty.
                    c = in.read();
                    collisionMap[i][j] = false;
                    
                    switch (c)
                    {
                        // Wall 
                        case '0': collisionMap[i][j] = true;
                        break;
                        
                        // Small Point
                        case '-': createObject(DotObject.class,i,j);
                        break;
                        
                        // Neutralizer. 
                        case '+': createObject(NeutralizerObject.class,i,j);
                        break;
                        
                        // Big Point Spawner 
                        case '$': {
                            SpawnerObject o = (SpawnerObject)createObject(SpawnerObject.class,i,j);
                            o.setSpawner(CherryObject.class,1,600,450+450*j/height,true);
                        }
                        break;
                        
                        // Pacman 
                        case '#': createObject(PacmanObject.class,i,j);
                        break;
                        
                        // Ghost Spawner 
                        case '^': {
                            SpawnerObject o = (SpawnerObject)createObject(SpawnerObject.class,i,j);
                            o.setSpawner(GhostObject.class,game.getNumberOfGhosts(),100,60,false);
                        }
                        break;
                    }
                }

                // The '\n' char.
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
        // A composite block made from 4 8x8 elements.
        
        Point tmpPoint;
        int tilesetX, tilesetY;
        
        for (int j = 0; j < 2; j++){
            for (int i = 0; i < 2; i++) {
                tmpPoint = v.remove();
                tilesetX = tmpPoint.x;
                tilesetY = tmpPoint.y;

                g.drawImage(
                    tileset,
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
        // Calls upon the Game object.
        
        GameObject o = game.createObject(ourClass);
        
        o.setCollisionMap(this);
        o.setPosition(sizeMod*i,sizeMod*j);
        o.setOrigin(x,y);
        
        return o;
    }
    
    int width, height, sizeMod;
    boolean collisionMap[][];
    
    File sourceFile;
    String labyrinthName;
    
    TextObject nameDisplay = null;
    TextObject livesDisplay = null;
    TextObject scoreDisplay = null;
    TextObject endDisplay = null;
    Image tileset;
    
    boolean finished = false;
    
    public void setSource(File f) {
        sourceFile = f;
        counter = 0;
    }
    
    public int getWidth() {
        return (width+1)*sizeMod;
    }
    
    public int getHeight() {
        return (height+1)*sizeMod;
    }
    
}
