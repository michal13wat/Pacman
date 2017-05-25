
package pacman;

// Ta klasa zastępuje KeyboardControl w ServerGame. Należy przekazać jej
// otrzymany string, aby móc normalnie sprawdzać naciśnięte klawisze.

public class KeyboardControlRemote extends KeyboardControl {
    
    public KeyboardControlRemote(Game game) {
        super(game);
    }
    
    @Override
    public void keyboardInit() {
        // Tutaj nie mamy żadnych listenerów!!!
    }
    
    public void feedInput(String keys) {
        // Ustawianie na sztywno wciśniętych klawiszy.
        leftPressed = keys.contains("l");
        rightPressed = keys.contains("r");
        upPressed = keys.contains("u");
        downPressed = keys.contains("d");
        escapePressed = keys.contains("x");
        enterPressed = keys.contains("e");
        qPressed = keys.contains("q");
        backspacePressed = keys.contains("b");
    }
}
