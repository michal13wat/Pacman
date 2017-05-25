
package pacman;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Sprite {
    // An aggregation of all the values necessary
    // to draw a single frame of a sprite.
    
    public Sprite(String src, double xat, double yat, int width, int height) {
        // Draw a specific frame.
        try {
            this.src = ImageIO.read(getClass().getResource(src));
        } catch (IOException i) {
            this.src = null;
        }
        
        this.xat = xat;
        this.yat = yat;
        this.width = width;
        this.height = height;
    }
    
    public Sprite(Image src, double xat, double yat, int width, int height) {
        // Draw a specific frame.
        this.src = src;
        this.xat = xat;
        this.yat = yat;
        this.width = width;
        this.height = height;
    }
    
    public Sprite(Image src) {
        // Draw an entire image.
        this.src = src;
        this.xat = 0;
        this.yat = 0;
        
        ImageIcon icon = new ImageIcon(src);
        this.width = icon.getIconWidth();
        this.height = icon.getIconHeight();
    }
    
    Image src;
    double xat;
    double yat;
    int width;
    int height;
    
    Image getSrc()
    {return src;}
    double getXat()
    {return xat;}
    double getYat()
    {return yat;}
    int getWidth()
    {return width;}
    int getHeight()
    {return height;}
}
