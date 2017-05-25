
package pacman;

public class TestObject extends ActiveObject {
    @Override
    public void createEvent() {
        x = (int)(System.currentTimeMillis()/100)%512;
        y = 120;
        
        yspeed = -1;
        ypull = 0.35;
        xpull = -0.001;
    }
    
    @Override
    public void stepEvent() {
        if ((y > 224) && (yspeed > 0)) {
            y = 224;
            yspeed = -yspeed/1.01;
        }
        
        super.stepEvent();
    }
}
