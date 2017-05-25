
package pacman;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Sprite {
    // Zbiór wartości niezbędnych do tego, aby narysować pojedynczą klatkę.
    
    public Sprite(String src, double xat, double yat, int width, int height) {
        // Rysowanie jednej klatki.
        /*try {
            this.src = ImageIO.read(getClass().getResource(src));
        } catch (IOException i) {
            this.src = null;
        }*/
        
        this.src = src;
        this.xat = xat;
        this.yat = yat;
        this.width = width;
        this.height = height;
    }
    
    public Sprite(String src) {
        // Rysowanie całego obrazka.
        this.src = src;
        this.xat = 0;
        this.yat = 0;
        
        ImageIcon icon = new ImageIcon(src);
        this.width = icon.getIconWidth();
        this.height = icon.getIconHeight();
    }
    
    String src;
    double xat;
    double yat;
    int width;
    int height;
    
    public String getSrc()
    {return src;}
    public double getXat()
    {return xat;}
    public double getYat()
    {return yat;}
    public int getWidth()
    {return width;}
    public int getHeight()
    {return height;}
}
