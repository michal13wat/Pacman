

package menuAndInterface;

import java.awt.Graphics2D;
import java.util.HashMap;

import gameObjects.GameObject;
import pacman.*;

// Ta klasa służy do rysowania podanych jej danych na temat
// podłączonych do serwera graczy - tzn. ich numery, nicki i postacie.

public class PlayerDisplayObject extends TextObject {
    
    @Override
    public void drawEvent(Graphics2D graphics) {
        if (visible == false)
        {return;}
        ClientGame clientGame = null;
        if (game instanceof ClientGame)
        {clientGame = (ClientGame)game;}
        
        int k = 1;
        x = xstart;
        y = ystart;
        
        ourText = "Players:";
        drawMyText(graphics);
        
        if ((clientGame != null) && (clientGame.isReady())) {
            // Rozpoczynając od siebie...
            y = ystart + k*fontHeight;

            ourText = k + " - " + game.playerName.value + " " + (char)(201+game.chosenCharacter.value);
            drawMyText(graphics);

            k++;
        }
        
        for (Integer id : game.getPlayerIds()) {
            // Rysujemy wpis danego gracza na odpowiedniej pozycji.
            int character = game.getPlayerCharacter(id);
            String nick = game.getPlayerName(id);
            
            y = ystart + k*fontHeight;
            
            ourText = k + " - " + nick + " " + (201+character);
            drawMyText(graphics);
            
            k++;
        }
    }
}
