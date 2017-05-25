
package gameObjects;

import gameObjects.ActiveObject;
import java.io.Serializable;

public class TestObject extends ActiveObject implements Serializable {
    @Override
    public void createEvent() {
        setSpriteSheet("pac_hero_sprites",16,16);
        
        imageIndex = 0;
        subimageIndex = 0;
        
        xstart = 64+(int)(System.currentTimeMillis()/100)%128;
        ystart = 120;
        
        //yspeed = -1;
        //ypull = 0.35;
        //xpull = -0.001;
    }
    
    @Override
    public void stepEvent() {
        /*if ((y > 224) && (yspeed > 0)) {
            y = 224;
            yspeed = -yspeed/1.01;
        }*/
        x = xstart+(int)(64.0*Math.cos(myCounter/8));
        y = ystart-(int)(64.0*Math.sin(myCounter/8));
        myCounter += mySpeed;
        mySpeed = Math.max(Math.min(mySpeed+1.0*Math.random()-0.5,4.0),-4.0);
        super.stepEvent();
    }
    
    double myCounter = 0;
    double mySpeed = 0;
}
