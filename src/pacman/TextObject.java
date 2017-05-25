
package pacman;

import java.awt.*;
import java.io.*;
import javax.imageio.*;

public class TextObject extends GameObject {
    
    @Override
    public void createEvent() {
        ourText = "";
        ourPrefix = "";
        ourPostfix = "";
        font = null;
        fontWidth = 0;
        fontHeight = 0;
        
        depth = -50;
    }

    @Override
    public void stepEvent() {
        //
    }
    
    @Override
    public void drawEvent(Graphics2D graphics) {
        if (visible == false)
        {return;}
        
        int c = 0, tmp_x = 0, tmp_y = 0;
        Point fontPos = null;
        
        for (int i = 0; i < ourText.length(); i ++) {
            c = ourText.charAt(i);
            
            if (c == '\n') {
                tmp_x = 0;
                tmp_y += fontHeight;
            }
            else {
                fontPos = charToPos(c);
                drawSprite(graphics,font,x+tmp_x,y+tmp_y,fontPos.x,fontPos.y,fontWidth,fontHeight);
                tmp_x += fontWidth;
            }
        }
    }
    
    @Override
    public void destroyEvent() {
        //
    }
    
    public void loadFont(String src, int width, int height) {
        try {
            font = ImageIO.read(getClass().getResource(src));
        }
        catch (IOException i) {
            font = null;
        }
        
        fontWidth = width;
        fontHeight = height;
    }

    protected Point charToPos(int c) {
        // row 0 - Uppercase, row 1 - Lowercase, row 2 - Digits, row 3 - Misc
        if (c == ' ') return new Point(10,2);
        if ((c >= 'A') && (c <= 'Z')) return new Point(c-'A',0);
        if ((c >= 'a') && (c <= 'z')) return new Point(c-'a',1);
        if ((c >= '0') && (c <= '9')) return new Point(c-'0',2);
        
        switch (c) {
            case '!': return new Point(0,3);
            case '"': return new Point(1,3);
            case '#': return new Point(2,3);
            case '$': return new Point(3,3);
            case '%': return new Point(4,3);
            case '&': return new Point(5,3);
            case '\'': return new Point(6,3);
            case '(': return new Point(7,3);
            case ')': return new Point(8,3);
            case '*': return new Point(9,3);
            case '+': return new Point(10,3);
            case ',': return new Point(11,3);
            case '-': return new Point(12,3);
            case '.': return new Point(13,3);
            case '/': return new Point(14,3);
            case ':': return new Point(15,3);
            case ';': return new Point(16,3);
            case '<': return new Point(17,3);
            case '=': return new Point(18,3);
            case '>': return new Point(19,3);
            case '?': return new Point(20,3);
            case '_': return new Point(25,2);
            case '`': return new Point(21,3); // Special character for Pacman symbol.
        }
        
        return new Point(10,2);
    }

    protected String ourText, ourPrefix, ourPostfix;
    protected Image font;
    protected int fontWidth, fontHeight;

    //public String getText()
    //{return ourPrefix+ourText+ourPostfix;}

    public void setText(String s) {
        ourText = ourPrefix+s+ourPostfix;
    }
    
    public void setPrefix(String s) {
        ourPrefix = s;
    }
    
    public void setPostfix(String s) {
        ourPostfix = s;
    }

}
