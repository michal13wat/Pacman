
package pacman;

import java.awt.Graphics2D;
import java.awt.Image;

abstract public class GameObject {
    public GameObject() {
        firstStep = true;
        visible = true;
        
        destroyed = false;
        //createEvent();
    }
    
    abstract public void createEvent();
    abstract public void stepEvent();
    abstract public void drawEvent(Graphics2D g);
    
    abstract public void destroyEvent();
    
    public void setPlayed(){
        //Nop()
    }
    
    public void destroy() {
        destroyed = true;
    }
    
    protected GameObject createObject(Class ourClass, int x, int y) {
        // Calls upon the Game object.
        
        GameObject o = game.createObject(ourClass);
        
        o.setPosition(x,y);
        o.setOrigin(xorigin,yorigin);
        o.setCollisionMap(this.collisionMap);
        
        return o;
    }
    
    protected int bounds(int x, int y, int z) {
        return Math.min(Math.max(y,x),z);
    }
    
    protected int approach(int from, int to, int by) {
        if (from > to) return (Math.max(to,from-by));
        
        return (Math.min(to,from+by));
    }
    
    protected double approach(double from, double to, double by) {
        if (from > to) return (Math.max(to,from-by));
        
        return (Math.min(to,from+by));
    }
    
    protected void drawSprite(
            Graphics2D g, Image src, int x, int y, double xat, double yat, int width, int height) {
        g.drawImage(
            src,
            (int)(scaleMod*(x+xorigin))+screenCenterX,
            (int)(scaleMod*(y+yorigin))+screenCenterY,
            (int)(scaleMod*(x+xorigin+width))+screenCenterX,
            (int)(scaleMod*(y+yorigin+height))+screenCenterY,
            width*(int)Math.floor(xat),height*(int)Math.floor(yat),
            width*((int)Math.floor(xat)+1),height*((int)Math.floor(yat)+1),
            null);
    }
    
    protected void drawSprite(
            Graphics2D g, Sprite spr, int x, int y) {
        drawSprite(g,spr.getSrc(),x,y,spr.getXat(),spr.getYat(),
                     spr.getWidth(),spr.getHeight());
    }
    
    protected int depth = 0;
    
    protected int x, y, xstart, ystart;
    protected int xorigin, yorigin;
    protected int counter;
    protected boolean firstStep;
    protected boolean visible;
    
    protected double scaleMod = 1;
    protected int screenCenterX = 0, screenCenterY = 0;
    protected Game game;
    
    protected boolean destroyed;
    
    protected LabyrinthObject collisionMap = null;
    
    public boolean isDestroyed() {
        return destroyed;
    }
    
    public int getX() {
        return x;
    }
    public int getY(){
        return y;
    }
    public int getXorigin() {
        return xorigin;
    }
    public int getYorigin(){
        return yorigin;
    }
    public boolean getVisible(){
        return visible;
    }
    public void setVisible(boolean v){
        visible = v;
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        
        if (firstStep) {
            this.xstart = x;
            this.ystart = y;
        }
    }
    
    public void setScale(double s){
        scaleMod = s;
    }
    
    public void setCenter(int x, int y) {
        this.screenCenterX = x;
        this.screenCenterY = y;
    }
    
    public void setOrigin(int x, int y) {
        this.xorigin = x;
        this.yorigin = y;
    }
    
    public void setDepth(int d) {
        depth = d;
    }
    
    public int getDepth() {
        return depth;
    }
    
    public void setCollisionMap(LabyrinthObject collisionMap) {
        this.collisionMap = collisionMap;
    }
}
