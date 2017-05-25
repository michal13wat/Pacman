
package pacman;

import java.util.ArrayList;

public class NeutralizerObject extends CollectibleObject {
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
    
    @Override
    public void getCollected(CharacterObject obj) {
        super.getCollected(obj);
        
        // Make all ghosts scared.
        
        ArrayList<GameObject> l = game.getAllObjects(GhostObject.class);
        GhostObject o = null;
        
        for (int i = 0; i < l.size(); i ++) {
            o = (GhostObject)l.get(i);
            o.scare(300);
        }
    }
}
