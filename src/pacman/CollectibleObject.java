
package pacman;

abstract public class CollectibleObject extends ActiveObject
{
    @Override
    public void createEvent()
    {
        super.createEvent();
        
        loadSpriteSheet("/resources/pac_collectible_sprites.png",16,16);
        
        bboxLeft = 6;
        bboxRight = 10;
        bboxTop = 6;
        bboxBottom = 10;
        
        imageIndex = 0;
        subimageIndex = 0;
    }
    
    @Override
    public void stepEvent() {
        // Memory savin'
    }
    
    public void getCollected(CharacterObject obj) {
        // Update score.
        game.addScore(pointReward);
        
        ParticleObject o = (ParticleObject)createObject(ParticleObject.class,x,y);
        
        o.setParticle("/resources/pac_particle_sprites.png",16,16,0,4,0.6);
        o.setDepth(-1);
        
        destroy();
    }
    
    protected int pointReward;
}
