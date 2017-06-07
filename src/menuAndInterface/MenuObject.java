
package menuAndInterface;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import gameObjects.GameObject;
import java.io.Serializable;
import pacman.IntWrapper;
import pacman.Sprite;
import pacman.StringWrapper;

public class MenuObject extends GameObject implements Serializable {
    private boolean hiddenPrefix = false;

    private class MenuOption implements Serializable {
        // Konkretna funkcja do odegrania.
        
        public void setMenuOption(String name, Callable function) {
            this.name = name;
            this.function = function;
        }
        
        public String getName() {
            return name;
        }
        
        protected String name;
        protected transient Callable function;
        
        public Callable getFunction() {
            return function;
        }
    }

    private class ButtonPressOption extends MenuOption implements Serializable {
        // Funkcja odgrywana przy wciśnięciu przycisku.
        
        public void setMenuOption(String name, Callable function, String button) {
            super.setMenuOption(name, function);
            this.button = button;
        }
        
        public String getButton() {
            return button;
        }
        
        protected String button;
    }
    
    private class StringDisplayOption extends MenuOption implements Serializable {
        // Wyświetlanie String'a.
        
        public void setMenuOption(String name, Callable function, StringWrapper value, String regex) {
            super.setMenuOption(name, function);
            this.value = value;
            this.regex = regex;
        }
        
        public StringWrapper getWrapper() {
            return value;
        }
        
        public String getStringWithRegex() {
            if (regex == null) {return getWrapper().value;}
            if (regex == "allChars") {return getWrapper().value;}

            String s = "", val = getWrapper().value;
            
            int k = 0;
            for (int j = 0; j < val.length(); j++)
            {
                if ((k < regex.length()) && (regex.charAt(k) != ('x'))) {
                    s += regex.charAt(k);
                    j--;
                }
                else
                    s += val.charAt(j);
                k++;
                //System.out.println(s);
            }
            
            return s;
        }
        
        public int getRegexLength() {
            if (regex == null) return 0;
            int l = 0;
            for (int i = 0; i < regex.length(); i++) {
                if (regex.charAt(i) != 'x') l++;
            }
            return l;
        }
        
        protected String regex;
        protected StringWrapper value;
    }
    
    private class StringInputOption extends StringDisplayOption implements Serializable {
        // Modyfikacja String'a.
        
        public void setMenuOption(String name, Callable function, StringWrapper value, String regex, int limit) {
            super.setMenuOption(name, function);
            this.value = value;
            this.regex = regex;
            this.limit = limit;
        }
        
        public int getLimit() {
            return limit;
        }
        
        public boolean canAcceptChar(char c)
        {
            return (((c >= 'a') && (c <= 'z'))
                || ((c >= 'A') && (c <= 'Z'))
                || ((c >= '0') && (c <= '9')) || (c == '.'));
        }
        
        protected int limit;
    }
    
    private class NumberInputOption extends StringInputOption implements Serializable {
        // To samo, ale tylko z numerami.
        
        @Override
        public boolean canAcceptChar(char c) {
            return ((c >= '0') && (c <= '9'));
        }
    }
    
    private class SpinnerOption extends MenuOption implements Serializable {
        // Zmienianie wartości liczbowej zmiennej.
        
        public void setMenuOption(String name, Callable function, IntWrapper value, int lbound, int rbound) {
            super.setMenuOption(name, function);
            this.valueWrapper = value;
            this.leftBound = lbound;
            this.rightBound = rbound;
        }
        
        public void setMenuOption(String name, Callable function, IntWrapper value, ArrayList<IntWrapper> blocked,
                int lbound, int rbound) {
            super.setMenuOption(name, function);
            this.valueWrapper = value;
            this.leftBound = lbound;
            this.rightBound = rbound;
            this.blocked = blocked;
        }
        
        public void addValue(int x) {
            int tmp = valueWrapper.value;
            valueWrapper.value += x;
            
            if (valueWrapper.value > rightBound) valueWrapper.value = rightBound;
            if (valueWrapper.value < leftBound) valueWrapper.value = leftBound;
            int antiBlockCounter = 0;
            
            if (blocked != null) 
                while (blocked.get(valueWrapper.value).value == 1) {
                    valueWrapper.value += (x>0)?1:-1;
                    if (antiBlockCounter++ > 100) break;
                    if (((spinnerFull("left")) && (x < 0))
                    && ((spinnerFull("right")) && (x > 0))) {
                        valueWrapper.value = tmp;
                        break;
                    }
                }
            
            if (valueWrapper.value > rightBound) valueWrapper.value = rightBound;
            if (valueWrapper.value < leftBound) valueWrapper.value = leftBound;
        }
        
        public int getValue() {
            return valueWrapper.value;
        }
        
        public int getBlocked(int i) {
            if (blocked == null) return 0;
            return blocked.get(i).value;
        }
        
        public boolean spinnerFull(String dir) {
            // Blocking certain options.
            if (blocked != null) {
                for (int i = leftBound; i <= rightBound; i++)
                    if (blocked.get(i).value == 0) {
                        leftBound = i;
                        break;
                    }
                for (int i = rightBound; i >= leftBound; i--)
                    if (blocked.get(i).value == 0) {
                        rightBound = i;
                        break;
                    }
                //System.out.println(leftBound + " " + rightBound);
            }
            
            if (valueWrapper.value > rightBound) valueWrapper.value = rightBound;
            if (valueWrapper.value < leftBound) valueWrapper.value = leftBound;
            
            if (dir.equals("left")) return (valueWrapper.value <= leftBound);
            if (dir.equals("right")) return (valueWrapper.value >= rightBound);
            
            return false;
        }
        
        protected ArrayList<IntWrapper> blocked;
        protected IntWrapper valueWrapper;
        protected int leftBound, rightBound;
    }
    
    private class ImageSpinnerOption extends SpinnerOption implements Serializable {
        // To samo, ale z obrazkiem na każdą liczbę.
        
        public void setMenuOption(String name, Callable function, IntWrapper value,
                int lbound, int rbound, ArrayList<Sprite> sprites) {
            super.setMenuOption(name, function,value,lbound,rbound);
            this.valueWrapper = value;
            this.leftBound = lbound;
            this.rightBound = rbound;
            this.sprites = sprites;
        }
        
        public void setMenuOption(String name, Callable function, IntWrapper value, ArrayList<IntWrapper> blocked,
                int lbound, int rbound, ArrayList<Sprite> sprites) {
            super.setMenuOption(name, function);
            this.valueWrapper = value;
            this.leftBound = lbound;
            this.rightBound = rbound;
            this.blocked = blocked;
            this.sprites = sprites;
        }
        
        public ArrayList<Sprite> getSprites(){
            return sprites;
        }
        
        public Sprite getCurrentSprite(){
            return sprites.get(valueWrapper.value);
        }
        
        public int getMaxWidth(){
            // Bardzo ważna metoda.
            int maxWidth = 0;
            for (Sprite sprite : sprites) {
                if (sprite.getWidth() > maxWidth)
                    maxWidth = sprite.getWidth();
            }
            
            return maxWidth;
        }
        
        public int getMaxHeight(){
            // Także samo.
            int maxHeight = 0;
            for (Sprite sprite : sprites) {
                if (sprite.getHeight() > maxHeight)
                    maxHeight = sprite.getHeight();
            }
            
            return maxHeight;
        }
        
        ArrayList<Sprite> sprites;
    }

    @Override
    public void createEvent() {
        myOptions = new ArrayList();
        hiddenOptions = new ArrayList();
        menuTitle = null;
        visible = false;
        depth = -50;
    }
    
    @Override
    public void stepEvent() {
        if (firstStep == true) {
            visible = true;
            firstStep = false;
        }
        
        counter++;
        
        if (counter < 5)
            return;
        
        // Kontrola opcji.
        MenuOption m = myOptions.get(cursorPos);
        
        // Wejście z klawiatury.
        if (m instanceof StringInputOption) {
            StringInputOption oo = (StringInputOption)m;
            StringWrapper s = oo.getWrapper();
            char c = game.keyboardCharCheck();
            // Usuwanie i dodawanie znaków.
            if ((game.keyboardCheck("backspace")) && !(game.keyboardHoldCheck("backspace")) && (s.value.length() > 0))
                s.value = s.value.substring(0,s.value.length()-1);
            else if ((c != 0) && (oo.canAcceptChar(c)) && (s.value.length() < oo.getLimit()))
                s.value += c;
        }
        
        // Blokowanie numerków.
        if (m instanceof SpinnerOption) {
            SpinnerOption s = (SpinnerOption)m;
            while (s.getBlocked(s.getValue()) == 1) {
                if (s.spinnerFull("left"))
                    s.addValue(1);
                else
                    s.addValue(-1);
            }
        }
        
        // Wybór opcji za pomocą Enter.
        if ((game.keyboardCheck("enter")) && (!game.keyboardHoldCheck("enter")) && (counter > 0)) select(m);
        else if ((game.keyboardCheck("left")) && (!game.keyboardHoldCheck("left"))) {
            // Zmniejszanie wartości Spinnera w lewo..
            if (m instanceof SpinnerOption) ((SpinnerOption)m).addValue(-1);
        }
        else if ((game.keyboardCheck("right")) && (!game.keyboardHoldCheck("right"))) {
            // Zwiększanie wartości Spinnera w prawo..
            if (m instanceof SpinnerOption) ((SpinnerOption)m).addValue(1);
        }
        else if ((game.keyboardCheck("up")) && (!game.keyboardHoldCheck("up"))) {
            // Zmiana pozycji z góra/dół.
            cursorPos --;
            if (cursorPos < 0) cursorPos += myOptions.size();
        }
        else if ((game.keyboardCheck("down")) && (!game.keyboardHoldCheck("down"))) {
            // Ditto.
            cursorPos = (cursorPos+1)%myOptions.size();
        }
        
        // Ukryte opcje.
        if (!(m instanceof StringInputOption)) {
            for (int i = 0; i < hiddenOptions.size(); i ++) {
                MenuOption o = hiddenOptions.get(i);
                if ((o instanceof ButtonPressOption)
                && (game.keyboardCheck(((ButtonPressOption)o).getButton()))
                && (!game.keyboardHoldCheck(((ButtonPressOption)o).getButton())))
                    select(o);
            }
        }
    }

    @Override
    public void destroyEvent() {
        if (renderer != null) renderer.destroy();
    }

    @Override
    public void drawEvent(Graphics2D g) {
        MenuOption o = null;
        
        // Ustawienia TextObject.
        if (renderer == null) {
            renderer = (TextObject)createObject(TextObject.class,x,y);
            renderer.loadFont(fontSource,fontWidth,fontHeight);
        }
        
        // I jeden dla opcjonalnego tytułu menu.
        if (menuTitle != null) {
            renderer.setPrefix(" ");
            renderer.setText(menuTitle);
            renderer.setPosition(x,y-fontHeight);
            renderer.forceDraw(scaleMod,screenCenterX,screenCenterY,g);
        }
        
        // Rysowanie każdej opcji.
        int height = 0;
        for (int i = 0; i < myOptions.size(); i ++) {
            o = myOptions.get(i);
            height = drawOption(g,o,i,height);
        }
    }
    
    // Dodawanie nowych opcji:
    
    public void addMenuOption(String name, Callable newFunction) {
        MenuOption o = new MenuOption();
        o.setMenuOption(name,newFunction);
        myOptions.add(o);
    }

    public void updateMenuOption(String oldName, String newName){
        for (int i = 0; i < myOptions.size(); i++){
            if (myOptions.get(i).getName().equals(oldName)){
                MenuOption o = new MenuOption();
                o.setMenuOption(newName, null);
                myOptions.set(i, o);
                return;
            }
        }
    }
    
    public void addButtonPressOption(String name, Callable newFunction, String button) {
        ButtonPressOption o = new ButtonPressOption();
        o.setMenuOption(name,newFunction,button);
        hiddenOptions.add(o);
    }

    public void addStringDisplayOption(String name, Callable newFunction, StringWrapper value, String regex) {
        StringDisplayOption o = new StringDisplayOption();
        o.setMenuOption(name,newFunction,value,regex);
        myOptions.add(o);
    }

    public void addStringInputOption(String name, Callable newFunction, StringWrapper value, String regex, int limit) {
        StringInputOption o = new StringInputOption();
        o.setMenuOption(name,newFunction,value,regex,limit);
        myOptions.add(o);
    }
    
    public void addNumberInputOption(String name, Callable newFunction, StringWrapper value, String regex, int limit) {
        NumberInputOption o = new NumberInputOption();
        o.setMenuOption(name,newFunction,value,regex,limit);
        myOptions.add(o);
    }
    
    public void addSpinnerOption(String name, Callable newFunction, IntWrapper value, int lbound, int rbound) {
        SpinnerOption o = new SpinnerOption();
        o.setMenuOption(name,newFunction,value,lbound,rbound);
        myOptions.add(o);
    }
    
    public void addImageSpinnerOption(String name, Callable newFunction,
                IntWrapper value, int lbound, int rbound, ArrayList<Sprite> sprites) {
        ImageSpinnerOption o = new ImageSpinnerOption();
        o.setMenuOption(name,newFunction,value,lbound,rbound,sprites);
        myOptions.add(o);
    }
    
    public void addImageSpinnerOption(String name, Callable newFunction, ArrayList<IntWrapper> blocked,
                IntWrapper value, int lbound, int rbound, ArrayList<Sprite> sprites) {
        ImageSpinnerOption o = new ImageSpinnerOption();
        o.setMenuOption(name,newFunction,value,blocked,lbound,rbound,sprites);
        myOptions.add(o);
    }
    
    private int drawOption(Graphics2D g, MenuOption o, int i, int height) {
        // Przesunięte tutaj, w celu skrócenia funkcji.
        // Zwraca pozycję y, na której trzeba narysować kolejną opcję.
        int newHeight = height;
        renderer.setPostfix("");
            
        if (o instanceof SpinnerOption) {
            // Strzałki Spinnerów.
            SpinnerOption so = (SpinnerOption)o;
            String s = "";
            if ((so.spinnerFull("left")) || (cursorPos != i)) s += " ";
            else s += "<";

            // Zostawiamy miejsce na ewentualny obrazek.
            if (o instanceof ImageSpinnerOption) {
                ImageSpinnerOption oo = (ImageSpinnerOption)o;
                height += (oo.getMaxHeight()-fontHeight)/2;
                for (int j = 0; j < oo.getMaxWidth(); j += 8)
                {s += " ";}
            }
            else
            {s += String.valueOf(so.getValue());}

            if ((so.spinnerFull("right")) || (cursorPos != i)) s += " ";
            else s += ">";

            renderer.setPostfix(s);
        }
        else if ((o instanceof StringInputOption)
             || (o instanceof StringDisplayOption)) {
            // String do modyfikacji.
            StringDisplayOption io = (StringDisplayOption)o;
            String s = io.getStringWithRegex();
            
            if ((counter%20 < 10) && (cursorPos == i)) s = s + "_";
            
            renderer.setPostfix(s);
        }

        // Strzałka wyboru opcji.
        if (cursorPos == i && !hiddenPrefix) renderer.setPrefix("> ");
        else renderer.setPrefix("  ");
        
        renderer.setText(o.getName());
        renderer.setPosition(x,y+height);
        renderer.forceDraw(scaleMod,screenCenterX,screenCenterY,g);

        // Obrazki do spinnera.
        if (o instanceof ImageSpinnerOption) {
            ImageSpinnerOption oo = (ImageSpinnerOption)o;
            drawSprite(g,oo.getCurrentSprite(),
                        x+fontWidth*(3+o.getName().length()),y+newHeight);
            newHeight += oo.getMaxHeight();
        }
        else {
            newHeight += fontHeight;
        }
        
        return newHeight;
    }

    public void hidePrefixMenu(){
        hiddenPrefix = true;
    }

    public void showPrefixMenu(){
        hiddenPrefix = false;
    }
    
    private void select(MenuOption o) {
        try {
            Callable f = o.getFunction();
            if (f != null) {
                f.call();
                destroy();
            }
        }
        catch (Exception e) {}
    }
    
    protected TextObject renderer;

    private ArrayList<MenuOption> myOptions, hiddenOptions;
    protected String fontSource;
    protected String menuTitle;
    protected int fontWidth, fontHeight;
    protected int cursorPos;

    public void setFont(String src, int w, int h) {
        fontSource = src;
        fontWidth = w;
        fontHeight = h;
    }
    
    public void setTitle(String s) {
        if (menuTitle == null) y += fontHeight;
        menuTitle = s;
    }
    
    public int getMenuHeight() {
        int h = myOptions.size();
        if (menuTitle != null) h += 1;
        
        return fontHeight*h;
    }
    
    public int getMenuWidth() {
        int max = menuTitle.length(), l = 0;
        String s;
        
        for (int i = 0; i < myOptions.size(); i ++) {
            l = 0;
            
            MenuOption o = myOptions.get(i);
            s = o.getName();
            l += s.length();
            
            if (o instanceof StringInputOption) {
                StringInputOption oo = (StringInputOption)o;
                l += oo.getLimit() + oo.getRegexLength();
            }
            else if (o instanceof ImageSpinnerOption) {
                ImageSpinnerOption oo = (ImageSpinnerOption)o;
                l += oo.getMaxWidth()/fontWidth+2;
            }
            
            if (l > max)
            {max = l;}
        }
        
        return fontWidth*max;
    }
}
