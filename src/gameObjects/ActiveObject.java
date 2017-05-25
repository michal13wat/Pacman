
package gameObjects;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import javax.imageio.ImageIO;

abstract public class ActiveObject extends GameObject implements Serializable {
    @Override
    public void createEvent(){
        // Inicjalizacja zmiennych.
        tangible = true;
        visible = true;
        
        wraparoundEnabled = true;
        wallContact = false;
        
        x = 0;
        y = 0;
        xstart = x;
        ystart = y;
        
        xspeed = 0;
        yspeed = 0;
        
        xadd = 0;
        yadd = 0;
        
        xpull = 0;
        ypull = 0;
        
        depth = -1;
    }
    
    @Override
    public void stepEvent() {
        firstStep = false;
        counter ++;
        
        moveObject();
        
        if (wraparoundEnabled) {
            x = wraparound(x,false);
            y = wraparound(y,true);
        }
    }
    
    @Override
    public void drawEvent(Graphics2D g) {
        if ((visible == false) || (destroyed == true)) return;
        
        if (spriteSheet == null) {
            g.setColor(Color.WHITE);
            g.fillRect(screenCenterX+(int)(scaleMod*(x+xorigin)),(int)(scaleMod*(y+yorigin)),16,16);
        }
        else {
            if (wraparoundEnabled){
                int xoverflow = collisionMap.getWidth()-(x+imageWidth);
                int yoverflow = collisionMap.getHeight()-(y+imageHeight);
                
                drawSpriteChopped(g,x,y,0,0,bounds(0,xoverflow,imageWidth),bounds(0,yoverflow,imageHeight));
                drawSpriteChopped(g,x,0,0,bounds(0,yoverflow+imageHeight,imageHeight),imageWidth,imageHeight);
                drawSpriteChopped(g,0,y,bounds(0,xoverflow+imageWidth,imageWidth),0,imageWidth,imageHeight);
                drawSpriteChopped(g,0,0,bounds(0,xoverflow+imageWidth,imageWidth),bounds(0,yoverflow+imageHeight,imageHeight),imageWidth,imageHeight);
            }
            else drawSpriteFull(g);
        }
    }
    
    @Override
    public void destroyEvent() {
        //
    }
    
    public void setSpriteSheet(String name, int imageWidth, int imageHeight) {
        // Zmienia wyznaczony zestaw sprite'ów.
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        
        this.spriteSheet = name;
        
        bboxLeft = 0;
        bboxRight = imageWidth;
        bboxTop = 0;
        bboxBottom = imageHeight;
    }
    
    protected void drawSpriteFull(Graphics2D g) {
        drawSprite(g,getSprites(spriteSheet),x,y,subimageIndex,imageIndex,imageWidth,imageHeight);
    }
        
    protected void drawSpriteChopped(Graphics2D g, int x, int y, int x1, int y1, int x2, int y2) {
        g.drawImage(
            getSprites(spriteSheet),
            (int)(scaleMod*(x+xorigin))+screenCenterX,
            (int)(scaleMod*(y+yorigin))+screenCenterY,
            (int)(scaleMod*(x+x2-x1+xorigin))+screenCenterX,
            (int)(scaleMod*(y+y2-y1+yorigin))+screenCenterY,
            imageWidth*(int)Math.floor(subimageIndex)+x1,imageHeight*(int)Math.floor(imageIndex)+y1,
            imageWidth*(int)Math.floor(subimageIndex)+x2,imageHeight*(int)Math.floor(imageIndex)+y2,
            null);
    }
    
    protected ActiveObject getColliding(Class ourClass) {
        // Zwraca ActiveObject, z którym w tej chwili kolidujemy.
        // Jeśli nie ma żadnego, zwraca null.
        
        ArrayList<GameObject> l = game.getAllObjects(ourClass);
        ActiveObject obj;
        
        for (int i = 0; i < l.size(); i++) {
            obj = (ActiveObject)l.get(i);
            if ((obj.isTangible()) && (!obj.isDestroyed()) && (collisionCheck(obj))) return obj;
        }
        
        return null;
    }
    
    protected boolean collisionCheck(int xcheck, int ycheck) {
        // Sprawdzanie kolizji, bazowane na zmiennych bbox.
        // Niezbyt precyzyjne.
        
        if ((wallContact == false) || (collisionMap == null)) return false;
        
        int i = bboxLeft, j;
        
        while (i < bboxRight) {
            j = bboxTop;
            
            while (j < bboxBottom) {
                if (collisionMap.checkCollisionScaled(wraparound(xcheck+i,false),wraparound(ycheck+j,true)))
                    return true;
                
                if (j == bboxBottom-1) j = bboxBottom;
                else j = (int)Math.min(j+(bboxBottom-bboxTop-1)/2,bboxBottom-1);
            }
            
            if (i == bboxBottom-1) i = bboxRight;
            else i = (int) Math.min(i+(bboxRight-bboxLeft-1)/2,bboxRight-1);
        }
        
        return false;
    }
    
    protected boolean collisionCheck(ActiveObject other) {
        // Sprawdzanie kolizji z innymi obiektami.
        
        if ((other == null) || (other == this)) return false;
        
        if ((x+this.bboxRight-1 >= other.getX()+other.getBbox("left"))
            && (x+this.bboxLeft <= other.getX()+other.getBbox("right")-1)) {
            if ((y+this.bboxTop <= other.getY()+other.getBbox("bottom")-1)
                    && (y+this.bboxBottom-1 >= other.getY()+other.getBbox("top")))
                return true;
        }
        
        return false;
    }
    
    protected void moveObject() {
        // Generalny kod do ruchu.
        // Unika nie-całkowitych pozycji.
        
        openOnLeft = !collisionCheck(x-1,y);
        openOnTop = !collisionCheck(x,y-1);
        openOnRight = !collisionCheck(x+1,y);
        openOnBottom = !collisionCheck(x,y+1);
        
        xadd += xspeed;
        yadd += yspeed;
        
        while (Math.abs(xadd) >= 1) {
            if (collisionCheck(x+(int)Math.signum(xadd),y)) {
                hitWall(false);
                xadd = 0;
            }
            else {
                x += (int)Math.signum(xadd);
                xadd = approach(xadd,0,1);
                
                if (((!collisionCheck(x,y-1)) && (!openOnTop))
                        || ((!collisionCheck(x,y+1)) && (!openOnBottom)))
                    passedEntrance(true);
            }
        }
        
        while (Math.abs(yadd) >= 1) {
            if (collisionCheck(x,y+(int)Math.signum(yadd))) {
                hitWall(true);
                yadd = 0;
            }
            else {
                y += (int)Math.signum(yadd);
                yadd = approach(yadd,0,1);
                
                if (((!collisionCheck(x-1,y)) && (!openOnLeft))
                  || ((!collisionCheck(x+1,y)) && (!openOnRight))) passedEntrance(false);
            }
        }
        
        xspeed += xpull;
        yspeed += ypull;
    }
    
    protected int wraparound(int coord, boolean isVertical) {
        // Zwraca koordynaty "owinięte" wokół brzegów mapy.
        
        if ((wraparoundEnabled == true) && (collisionMap != null)) {
            if (isVertical) {
                if (coord > collisionMap.getHeight()) return coord-collisionMap.getHeight();
                else if (coord < 0) return coord+collisionMap.getHeight();
            }
            else {
                if (coord > collisionMap.getWidth()) return coord-collisionMap.getWidth();
                else if (coord < 0) return coord+collisionMap.getWidth();
            }
        }
        
        return coord;
    }
    
    protected void hitWall(boolean isVertical) {
        //
    }
    
    protected void passedEntrance(boolean isVertical) {
        //
    }
    
    String spriteSheet;
    protected double imageIndex, subimageIndex;
    protected int imageWidth, imageHeight;
    protected int bboxLeft, bboxRight, bboxTop, bboxBottom;
    
    protected double xspeed, yspeed, xadd, yadd, xpull, ypull;
    
    protected boolean openOnLeft, openOnTop, openOnRight, openOnBottom;
    
    protected boolean wallContact, wraparoundEnabled;
    protected boolean tangible;
    
    public int getBbox(String loc) {
        switch (loc) {
            case "left": return bboxLeft;
            case "right": return bboxRight;
            case "top": return bboxTop;
            case "bottom": return bboxBottom;
        }
        
        return 0;
    }
    
    public boolean isTangible() {
        return tangible;
    }
    
    public void setMotion(double xspeed, double yspeed) {
        this.xspeed = xspeed;
        this.yspeed = yspeed;
        this.xadd = 0;
        this.yadd = 0;
    }
    
    public void setAcceleration(double xpull, double ypull) {
        this.xpull = xpull;
        this.ypull = ypull;
    }
}
