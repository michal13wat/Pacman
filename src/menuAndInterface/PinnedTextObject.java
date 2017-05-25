
package menuAndInterface;

import menuAndInterface.TextObject;
import gameObjects.GameObject;
import java.io.Serializable;

public class PinnedTextObject extends TextObject implements Serializable {
    // Rozszerzona wersja TextObject, która może być
    // "przypięta" do jakiegoś innego GameObject.
    
    @Override
    public void stepEvent() {
        super.stepEvent();
        this.xorigin = pin.getXorigin();
        this.yorigin = pin.getYorigin();
        x = pin.getX()+addx;
        y = pin.getY()+addy;
        //visible = pin.getVisible();
    }
    
    public void setPin(GameObject pin, int addx, int addy) {
        // Doczepienie do jakiegoś obiektu.
        this.pin = pin;
        this.addx = addx;
        this.addy = addy;
        
        this.xorigin = pin.getXorigin();
        this.yorigin = pin.getYorigin();
    }
    
    GameObject pin;
    int addx, addy;
}
