
package pacman;

public class ParticleObject extends ActiveObject {
    
    @Override
    public void stepEvent() {
        super.stepEvent();
        
        subimageIndex += imageSpeed;
        if (subimageIndex >= imageCount) destroy();
    }
    
    public void setParticle(String sourceImg, int imageWidth, int imageHeight, int imageIndex, int imageCount, double imageSpeed) {
        loadSpriteSheet(sourceImg,imageWidth,imageHeight);
        
        this.subimageIndex = 0;
        
        this.imageIndex = imageIndex;
        this.imageCount = imageCount;
        this.imageSpeed = imageSpeed;
    }
    
    private double imageSpeed;
    private int imageCount;
}
