
package gameObjects;

import java.io.Serializable;

public class ParticleObject extends ActiveObject implements Serializable {
    
    @Override
    public void stepEvent() {
        super.stepEvent();
        
        subimageIndex += imageSpeed;
        if (subimageIndex >= imageCount) destroy();
    }
    
    public void setParticle(String sourceImg, int imageWidth, int imageHeight, int imageIndex, int imageCount, double imageSpeed) {
        setSpriteSheet(sourceImg,imageWidth,imageHeight);
        
        this.subimageIndex = 0;
        
        this.imageIndex = imageIndex;
        this.imageCount = imageCount;
        this.imageSpeed = imageSpeed;
    }
    
    /*@Override
    public boolean sendMe() {
        if (sent == false) {
            sent = true;
            return true;
        }
        return false;
    }*/
    
    private double imageSpeed;
    private int imageCount;
}
