package clientAndServer;

import java.io.Serializable;

/**
 * Created by User on 2017-04-17.
 */
/*
 *  Nazwa klasy jest logiczna jeżeli patrzy się od strony klieta.
 *  Żeby nie toworzyć nowych klas osobno dla servera, użyję tych.
 *  */

public class PackToSendToServer implements Serializable {
    private int playersId;
    private String playersName;
    private int character;
    private String pressedKey;

    public PackToSendToServer(String playersName, int character, String pressedKey, int playersId){
        this.playersName = playersName;
        this.character = character;
        this.pressedKey = pressedKey;
        this.playersId = playersId;
    }

    public void setPlayersId(int playersId) {
        this.playersId = playersId;
    }
    
    public void setPlayersName(String playersName) {
        this.playersName = playersName;
    }

    public void setCharacter(int character) {
        this.character = character;
    }

    public void setPressedKey(String pressedKey) {
        this.pressedKey = pressedKey;
    }


    public int getPlayersId() {
        return playersId;
    }
    
    public String getPlayersName() {
        return playersName;
    }

    public int getCharacter() {
        return character;
    }

    public String getPressedKey() {
        return pressedKey;
    }
}