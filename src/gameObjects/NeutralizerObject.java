
package gameObjects;

import gameObjects.GhostObject;
import gameObjects.CollectibleObject;
import java.io.Serializable;
import java.util.ArrayList;

public class NeutralizerObject extends CollectibleObject implements Serializable {
    @Override
    public void createEvent() {
        super.createEvent();
        
        pointReward = 0;
        
        imageIndex = 2;
        subimageIndex = 0;
    }
    
    @Override
    public void stepEvent() {
        subimageIndex = (counter%30)/10.0;
        counter ++;
    }
    
    /*@Override
    public boolean sendMe() {
        if (sent == false) {
            sent = true;
            return true;
        }
        return false;
    }*/
    
    @Override
    public void getCollected(CharacterObject obj) {
        super.getCollected(obj);
        
        // Przestrasza wszystkie duchy.
        
        ArrayList<GameObject> l = game.getAllObjects(GhostObject.class);
        GhostObject o = null;
        
        for (int i = 0; i < l.size(); i ++) {
            o = (GhostObject)l.get(i);
            o.scare(300);
        }
    }
}
