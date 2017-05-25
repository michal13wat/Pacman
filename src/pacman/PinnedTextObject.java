
package pacman;

public class PinnedTextObject extends TextObject {
    // An extended version of TextObject, that can be set to
    // draw text relative to another GameObject.
    
    @Override
    public void stepEvent() {
        super.stepEvent();
        this.xorigin = pin.getXorigin();
        this.yorigin = pin.getYorigin();
        x = pin.getX()+addx;
        y = pin.getY()+addy;
        //visible = pin.getVisible();
    }
    
    void setPin(GameObject pin, int addx, int addy) {
        // Attach to an object with added relative coordinates.
        this.pin = pin;
        this.addx = addx;
        this.addy = addy;
        
        this.xorigin = pin.getXorigin();
        this.yorigin = pin.getYorigin();
    }
    
    GameObject pin;
    int addx, addy;
}
