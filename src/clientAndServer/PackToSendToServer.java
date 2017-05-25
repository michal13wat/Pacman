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
    private String playersName;
    private String character;
    private String pressedKey;

    public PackToSendToServer(String playersName, String character, String pressedKey){
        this.playersName = playersName;
        this.character = character;
        this.pressedKey = pressedKey;
    }

    public void setPlayersName(String playersName) {
        this.playersName = playersName;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public void setPressedKey(String pressedKey) {
        this.pressedKey = pressedKey;
    }


    public String getPlayersName() {
        return playersName;
    }

    public String getCharacter() {
        return character;
    }

    public String getPressedKey() {
        return pressedKey;
    }
}