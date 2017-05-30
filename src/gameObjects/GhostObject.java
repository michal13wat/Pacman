
package gameObjects;

import java.io.Serializable;
import java.util.ArrayList;

public class GhostObject extends CharacterObject implements Serializable {
    @Override
    public void createEvent() {
        super.createEvent();
        
        setSpriteSheet("pac_ghost_sprites",16,16);
        
        myColor = 0;
        imageIndex = 0;
        subimageIndex = 0;
        
        //defaultSpeed = 1.3;
        defaultSpeed = 0.75;
        
        depth = -5;
        
        scareCounter = 0;
        hiddenCounter = 35;
        
        // Walidacja koloru.
        
        ArrayList<GameObject> allGhosts = game.getAllObjects(this.getClass());
        GhostObject otherGhost = null;
        
        for (int i = 0; i < allGhosts.size(); i++) {
            otherGhost = (GhostObject)allGhosts.get(i);
            if ((otherGhost != this) && (otherGhost.myColor == myColor)) myColor ++;
        }
    }
    
    @Override
    public void setPlayed(){
        if (game.getGhostPlayer(myColor) >= 0) {
            isPlayed = true;
            playerId = game.getGhostPlayer(myColor);
            //game.setPlayedGhostCreated(true);
            //createPin("P1");
        }
        else isPlayed = false;
    }
    
    @Override
    public void stepEvent() {
        if (firstStep){
            resetPosition();
        }
        
        if (isPlayed) stepControl();
        
        // Liczniki ukrycia / przestraszenia.
        
        if (scareCounter > 0) {
            imageIndex = 4;
            scareCounter --;
        }
        else imageIndex = myColor;
        
        if (hiddenCounter > 0) {
            visible = false;
            tangible = false;
            
            hiddenCounter --;
            if (hiddenCounter == 34) {
                ParticleObject o = (ParticleObject)createObject(ParticleObject.class,xstart,ystart);
                o.setParticle("pac_particle_sprites",16,16,2,8,0.2);
                o.setDepth(-10);
            }
            else if (hiddenCounter == 0) resetPosition();
        }
        else {
            visible = true;
            tangible = true;
        }
        
        // Łapanie PacMana.
        
        if (tangible) {
            PacmanObject obj;

            if ((obj = (PacmanObject)getColliding(PacmanObject.class)) != null) {
                if (scareCounter == 0){
                    obj.getCaught(this);
                    ArrayList<GameObject> ghosts= game.getAllObjects(GhostObject.class);
                    for (GameObject ghost : ghosts) ((GhostObject)ghost).getCaught(this);
                }
                else getCaught(obj);
            }
        }
        
        defaultSpeed = 0.75+(int)game.getScore()/150.0;
        
        super.stepEvent();
    }
    
    public void resetPosition() {
        x = xstart;
        y = ystart;
        
        if (!isPlayed){
            if (Math.random() >= 0.5) xspeed = defaultSpeed;
            else xspeed = -defaultSpeed;
        }
        
        
        
        yspeed = 0;
    }
    public void getCaught(CharacterObject obj) {
        scareCounter = 0;
        hiddenCounter = 100;
        
        ParticleObject o = (ParticleObject)createObject(ParticleObject.class,x,y);
        o.setParticle("pac_particle_sprites",16,16,1,7,0.2);
        o.setDepth(-2);
    }
    /*
    // Has to turn at all times.
    @Override
    protected boolean mustTurn() {
        return true;
    }
    
    // Alternates between L/R.
    @Override
    protected boolean willTurnLeft() {
        return (randomFactor >= 0.5);
    }
    
    @Override
    protected boolean willTurnRight() {
        return (randomFactor < 0.5);
    }
    
    // Alternates between U/D.
    @Override
    protected boolean willTurnUp() {
        return (randomFactor >= 0.5);
    }
    
    @Override
    protected boolean willTurnDown() {
        return (randomFactor < 0.5);
    }
    
    // Doesn't necessarily go into entrances.
    @Override
    protected boolean mayEnter() {
        return (randomFactor >= 0.5);
    }
    
    // But if he decides so, he doesn't differentiate.
    @Override
    protected boolean willEnterLeft() {
        return true;
    }
    
    @Override
    protected boolean willEnterRight() {
        return true;
    }
    
    @Override
    protected boolean willEnterUp() {
        return true;
    }
    
    @Override
    protected boolean willEnterDown() {
        return true;
    }
    */
   
    protected int scareCounter;
    protected int hiddenCounter;
    protected int myColor;
    
    public void scare(int time) {
        scareCounter = time;
    }
    
    public int getColor() {
        return myColor;
    }
}
