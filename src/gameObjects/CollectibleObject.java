
package gameObjects;

import java.io.Serializable;

abstract public class CollectibleObject extends ActiveObject implements Serializable
{
    @Override
    public void createEvent()
    {
        super.createEvent();
        
        setSpriteSheet("pac_collectible_sprites",16,16);
        
        bboxLeft = 6;
        bboxRight = 10;
        bboxTop = 6;
        bboxBottom = 10;
        
        imageIndex = 0;
        subimageIndex = 0;
    }
    
    @Override
    public void stepEvent() {
        firstStep = false;
        // Memory savin'
    }
    
    public void getCollected(CharacterObject obj) {
        // Aktualizacja wyniku.
        game.addScore(pointReward);
        
        ParticleObject o = (ParticleObject)createObject(ParticleObject.class,x,y);
        
        o.setParticle("pac_particle_sprites",16,16,0,4,0.6);
        o.setDepth(-1);
        
        destroy();
    }
    
    protected int pointReward;
}
