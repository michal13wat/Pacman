
package pacman;

public class DotObject extends CollectibleObject
{
    @Override
    public void createEvent() {
        super.createEvent();
        
        pointReward = 1;
        
        imageIndex = 0;
        subimageIndex = 0;
    }
}
