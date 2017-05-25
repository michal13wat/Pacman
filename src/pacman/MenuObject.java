
package pacman;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class MenuObject extends GameObject {
    private boolean hiddenPrefix = false;

    private class MenuOption {
        // Private class for different options.
        
        public void setMenuOption(String name, Callable function) {
            this.name = name;
            this.function = function;
        }
        
        public String getName() {
            return name;
        }
        
        protected String name;
        protected Callable function;
        
        public Callable getFunction() {
            return function;
        }
    }

    private class ButtonPressOption extends MenuOption {
        // Activated upon pressing a button.
        
        public void setMenuOption(String name, Callable function, String button) {
            super.setMenuOption(name, function);
            this.button = button;
        }
        
        public String getButton() {
            return button;
        }
        
        protected String button;
    }
    
    private class StringInputOption extends MenuOption {
        // Modifies a tied string.
        
        public void setMenuOption(String name, Callable function, StringWrapper value, String regex, int limit) {
            super.setMenuOption(name, function);
            this.value = value;
            this.regex = regex;
            this.limit = limit;
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
        
        public int getLimit() {
            return limit;
        }
        
        public boolean canAcceptChar(char c)
        {
            return (((c >= 'a') && (c <= 'z'))
                || ((c >= 'A') && (c <= 'Z'))
                || ((c >= '0') && (c <= '9')));
        }
        
        protected StringWrapper value;
        protected String regex;
        protected int limit;
    }
    
    private class NumberInputOption extends StringInputOption {
        // A similar option, but this one only accepts numbers.
        
        @Override
        public boolean canAcceptChar(char c) {
            return ((c >= '0') && (c <= '9'));
        }
    }
    
    private class SpinnerOption extends MenuOption {
        // Changing the value of a variable.
        
        public void setMenuOption(String name, Callable function, IntWrapper value, int lbound, int rbound) {
            super.setMenuOption(name, function);
            this.valueWrapper = value;
            this.leftBound = lbound;
            this.rightBound = rbound;
        }
        
        public void addValue(int x) {
            valueWrapper.value += x;
            if (valueWrapper.value > rightBound) valueWrapper.value = rightBound;
            if (valueWrapper.value < leftBound) valueWrapper.value = leftBound;
        }
        
        public int getValue() {
            return valueWrapper.value;
        }
        
        public boolean spinnerFull(String dir) {
            if (dir.equals("left")) return (valueWrapper.value == leftBound);
            if (dir.equals("right")) return (valueWrapper.value == rightBound);
            
            return false;
        }
        
        protected IntWrapper valueWrapper;
        protected int leftBound, rightBound;
    }
    
    private class ImageSpinnerOption extends SpinnerOption {
        // A spinner with an image attributed to each option.
        
        public void setMenuOption(String name, Callable function, IntWrapper value,
                int lbound, int rbound, ArrayList<Sprite> sprites) {
            super.setMenuOption(name, function,value,lbound,rbound);
            this.valueWrapper = value;
            this.leftBound = lbound;
            this.rightBound = rbound;
            this.sprites = sprites;
        }
        
        public ArrayList<Sprite> getSprites(){
            return sprites;
        }
        
        public Sprite getCurrentSprite(){
            return sprites.get(valueWrapper.value);
        }
        
        public int getMaxWidth(){
            // Very important method.
            int maxWidth = 0;
            for (Sprite sprite : sprites) {
                if (sprite.getWidth() > maxWidth)
                    maxWidth = sprite.getWidth();
            }
            
            return maxWidth;
        }
        
        public int getMaxHeight(){
            // Likewise.
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
        depth = -50;
    }
    
    @Override
    public void stepEvent() {
        // Option control.
        MenuOption m = myOptions.get(cursorPos);
        
        // Keyboard input.
        if (m instanceof StringInputOption) {
            StringInputOption oo = (StringInputOption)m;
            StringWrapper s = oo.getWrapper();
            char c = game.keyboardCharCheck();
            // Deleting and adding characters.
            if ((game.keyboardCheck("backspace")) && !(game.keyboardHoldCheck("backspace")) && (s.value.length() > 0))
                s.value = s.value.substring(0,s.value.length()-1);
            else if ((c != 0) && (oo.canAcceptChar(c)) && (s.value.length() < oo.getLimit()))
                s.value += c;
        }
        
        // Choosing an option with Enter.
        if ((game.keyboardCheck("enter")) && (!game.keyboardHoldCheck("enter")) && (counter > 0)) select(m);
        else if ((game.keyboardCheck("left")) && (!game.keyboardHoldCheck("left"))) {
            // Decreasing spinner value with left.
            if (m instanceof SpinnerOption) ((SpinnerOption)m).addValue(-1);
        }
        else if ((game.keyboardCheck("right")) && (!game.keyboardHoldCheck("right"))) {
            // Increasing spinner value with right.
            if (m instanceof SpinnerOption) ((SpinnerOption)m).addValue(1);
        }
        else if ((game.keyboardCheck("up")) && (!game.keyboardHoldCheck("up"))) {
            // Changing options with up/down.
            cursorPos --;
            if (cursorPos < 0) cursorPos += myOptions.size();
        }
        else if ((game.keyboardCheck("down")) && (!game.keyboardHoldCheck("down"))) {
            // Ditto.
            cursorPos = (cursorPos+1)%myOptions.size();
        }
        
        // Hidden option checking.
        for (int i = 0; i < hiddenOptions.size(); i ++) {
            MenuOption o = hiddenOptions.get(i);
            if ((o instanceof ButtonPressOption)
            && (game.keyboardCheck(((ButtonPressOption)o).getButton()))
            && (!game.keyboardHoldCheck(((ButtonPressOption)o).getButton())))
                select(o);
        }
        
        counter++;
    }

    @Override
    public void destroyEvent() {
        if (renderer != null) renderer.destroy();
    }

    @Override
    public void drawEvent(Graphics2D g) {
        MenuOption o = null;
        
        // Setting up the TextObject.
        if (renderer == null) {
            renderer = (TextObject)createObject(TextObject.class,x,y);
            renderer.loadFont(fontSource,fontWidth,fontHeight);
        }
        
        // And one for the optional title.
        if (menuTitle != null) {
            renderer.setPrefix(" ");
            renderer.setText(menuTitle);
            renderer.setPosition(x,y-fontHeight);
            renderer.drawEvent(g);
        }
        
        // Drawing each option.
        int height = 0;
        for (int i = 0; i < myOptions.size(); i ++) {
            o = myOptions.get(i);
            height = drawOption(g,o,i,height);
        }
    }
    
    // Public methods for adding new options:
    
    public void addMenuOption(String name, Callable newFunction) {
        MenuOption o = new MenuOption();
        o.setMenuOption(name,newFunction);
        myOptions.add(o);
    }
    
    public void addButtonPressOption(String name, Callable newFunction, String button) {
        ButtonPressOption o = new ButtonPressOption();
        o.setMenuOption(name,newFunction,button);
        hiddenOptions.add(o);
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
    
    private int drawOption(Graphics2D g, MenuOption o, int i, int height) {
        // Moved here to shorten functions.
        // Returns height at which to draw next one.
        int newHeight = height;
        renderer.setPostfix("");
            
        if (o instanceof SpinnerOption) {
            // Drawing the spinner arrows.
            SpinnerOption so = (SpinnerOption)o;
            String s = "";
            if ((so.spinnerFull("left")) || (cursorPos != i)) s += " ";
            else s += "<";

            // Leaving space for the optional image.
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
        else if (o instanceof StringInputOption) {
            // Drawing the input string.
            StringInputOption io = (StringInputOption)o;
            String s = io.getStringWithRegex();
            
            if ((counter%20 < 10) && (cursorPos == i)) s = s + "_";
            
            renderer.setPostfix(s);
        }

        // Selected arrow.
        if (cursorPos == i && !hiddenPrefix) renderer.setPrefix("> ");
        else renderer.setPrefix("  ");

        renderer.setText(o.getName());
        renderer.setPosition(x,y+height);
        renderer.drawEvent(g);

        // Spinner images.
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
