
package gameObjects;

import gameObjects.CollectibleObject;
import java.io.Serializable;

public class DotObject extends CollectibleObject implements Serializable
{
    @Override
    public void createEvent() {
        super.createEvent();
        
        pointReward = 1;
        
        imageIndex = 0;
        subimageIndex = 0;
    }
    
    /*@Override
    public boolean sendMe() {
        if (sent == false) {
            sent = true;
            return true;
        }
        return false;
    }*/
}
