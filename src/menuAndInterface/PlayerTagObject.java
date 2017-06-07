

package menuAndInterface;

import java.awt.Graphics2D;
import java.util.HashMap;

import gameObjects.*;
import java.util.ArrayList;
import pacman.*;

// Klasa zastępuje PinnedTextObject, służy do automatycznego
// wyświetlania tagów graczy nad postaciami nie-komputerowymi.

public class PlayerTagObject extends TextObject {
    
    @Override
    public void createEvent() {
        super.createEvent();
        visibleSwitchCounter = 120;
        invisibleCounter = 0;
    }
    
    @Override
    public void stepEvent() {
        
        // Rysujemy tylko w niektórych klatkach.
        counterStuff();
    }
    
    @Override
    public void drawEvent(Graphics2D graphics) {
        
        if (visible == false)
        {return;}
        
        drawSingleTag(graphics,1,game.chosenCharacter.value);
        
        int k = 2;
        for (Integer id : game.getPlayerIds()) {
            drawSingleTag(graphics,k,game.getPlayerCharacter(id));
            k++;
        }
    }
    
    protected void drawSingleTag(Graphics2D graphics, int k, int character) {
        
        if (character == 0)
            for (GameObject o : game.getAllObjects(PacmanObject.class)) {
                xorigin = o.getXorigin();
                yorigin = o.getYorigin();
                x = o.getX();
                y = o.getY()-8;
                ourText = "P"+k;
                drawMyText(graphics);
            }
        else
            for (GameObject o : game.getAllObjects(GhostObject.class)) {
                GhostObject ghost = (GhostObject)o;
                if (ghost.getColor() == character-1) {
                    xorigin = o.getXorigin();
                    yorigin = o.getYorigin();
                    x = o.getX();
                    y = o.getY()-8;
                    ourText = "P"+k;
                    drawMyText(graphics);
                }
            }
    }
    
    protected void counterStuff() {
        
        //System.out.println(visibleSwitchCounter + " " + invisibleCounter);
        
        if (visibleSwitchCounter > 0) {
            if (visibleSwitchCounter%20 == 0) visible = !visible;
            
            visibleSwitchCounter--;
            if (visibleSwitchCounter == 0)
                invisibleCounter = 420;
        }
        
        if (invisibleCounter > 0) {
            visible = false;
            
            invisibleCounter--;
            if (invisibleCounter == 0)
                visibleSwitchCounter = 120;
        }
    }
    
    int visibleSwitchCounter;
    int invisibleCounter;
}
