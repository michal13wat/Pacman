package pacman;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 2017-03-01.
 */
public class GameConfiguration extends JFrame implements ActionListener  {
    public enum Character {PACMAN, REDGHOST, YELLOWGHOST, BLUEGHOST, PURPLEGHOST}

    private static Character chosenCharacter = null;
    private static Character tempChosenCharacter = null;


    private JButton okButtonn;
    private JButton cancelButton;
    private String pacman= "Pacman";
    private String redGhost = "Czerwony duszek";
    private String yellowGhost = "Żółty duszek";
    private String blueGhost = "Niebieski duszek";
    private String purpleGhost = "Różowy duszek";

    private JRadioButton pacmanButton = new JRadioButton(pacman);
    private JRadioButton redGhostButton = new JRadioButton(redGhost);
    private JRadioButton yellowGhostButton = new JRadioButton(yellowGhost);
    private JRadioButton blueGhostButton = new JRadioButton(blueGhost);
    private JRadioButton purpleGhostButton = new JRadioButton(purpleGhost);

    private JPanel icon;
    private JPanel butt = new JPanel();
    private JPanel buttAndIcons = new JPanel();

    private ImageIcon image;
    private JLabel label;


    private class ImagePanel extends JPanel{

        private BufferedImage image;

        public ImagePanel(String fileName) {
//            super();
            try {
                image = ImageIO.read(new File(fileName));
                System.out.print("konstruktor ImagePanel\n");
            } catch (IOException ex) {
                // handle exception...
                System.out.print("nie można otowrzyć ikony!");
            }
        }

//        public BufferedImage refreshImage() {
//            return  image;
//        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int x = appointCenter(image.getWidth(), this.getWidth());
            int y = appointCenter(image.getHeight(), this.getHeight());
            g.drawImage(image, x, y, this); // see javadoc for more info on the parameters
            System.out.print("właśnie powinien zostać narysowany nowy obraz!\n");
        }

        private int appointCenter(int elementDim, int componentDim){
            return (componentDim - elementDim)/2;
        }
    }

//    private class ImagePanel2 extends ImageIcon{
//        public ImagePanel2(){
//
//        }
//
//    }


    public GameConfiguration (){
        super("Konfiguracja gry");
        setPreferredSize(new Dimension(300, 220));
        //setSize(new Dimension(300, 100));
        setLocation(400, 200);
        setResizable(false);

        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        okButtonn = new JButton("OK");
        cancelButton = new JButton("Anuluj");



        pacmanButton.setMnemonic(KeyEvent.VK_M);
        pacmanButton.setActionCommand(pacman);
        //pacmanButton.setSelected(true);

        redGhostButton.setMnemonic(KeyEvent.VK_F);
        redGhostButton.setActionCommand(redGhost);

        yellowGhostButton.setMnemonic(KeyEvent.VK_S);
        yellowGhostButton.setActionCommand(yellowGhost);

        blueGhostButton.setMnemonic(KeyEvent.VK_T);
        blueGhostButton.setActionCommand(blueGhost);

        purpleGhostButton.setMnemonic(KeyEvent.VK_F);
        purpleGhostButton.setActionCommand(purpleGhost);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(pacmanButton);
        group.add(redGhostButton);
        group.add(yellowGhostButton);
        group.add(blueGhostButton);
        group.add(purpleGhostButton);


        //Register a listener for the radio buttons.
        pacmanButton.addActionListener(this);
        redGhostButton.addActionListener(this);
        yellowGhostButton.addActionListener(this);
        blueGhostButton.addActionListener(this);
        purpleGhostButton.addActionListener(this);

        butt.setBackground(Color.blue);
        butt.setSize(200, 300);
        butt.add(pacmanButton);
        butt.add(redGhostButton);
        butt.add(yellowGhostButton);
        butt.add(blueGhostButton);
        butt.add(purpleGhostButton);

        butt.setLayout(new GridLayout(5, 1));

        //icon = new ImagePanel("src/resources/chose_character_init.png");
        //icon.setBackground(Color.white);

        image = new ImageIcon("src/resources/chose_character_init.png");
        label = new JLabel("", image, JLabel.CENTER);

        //buttAndIcons.setBackground(Color.green);
        buttAndIcons.add(butt);
        buttAndIcons.add(label);

        add(buttAndIcons);
        buttAndIcons.setLayout(new GridLayout(1, 2));

        okButtonn.addActionListener(this);
        cancelButton.addActionListener(this);

        setLayout(new FlowLayout());

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        add(cancelButton);
        add(okButtonn);
    }

    public Character getChosenCharacter() {
        return chosenCharacter;
    }

    private void showCharactersIcon () {
        int chosedIconGhostOrPacman = 0;
        int indexGhostsIcon = 0;

        BufferedImage charactersIcon = null;
        BufferedImage pacmanIcon = null;
        try {
            charactersIcon = ImageIO.read(new File("src/resources/pac_ghost_sprites.png"));
            pacmanIcon = ImageIO.read(new File("src/resources/pac_hero_sprites.png"));
        }
        catch (IOException ex){
            System.out.print("nie można otworzyć pliku z ikonam!\n");
        }

//        charactersIcon.createGraphics().setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//        charactersIcon.createGraphics().setColor(Color.red);
//        int[] tab_x = new int[4];
//        int[] tab_y = new int[4];
//        tab_x[0] = 0;
//        tab_y[0] = 0;
//        tab_x[1] = 0;
//        tab_y[1] = 10;
//        tab_x[0] = 10;
//        tab_y[0] = 0;

        //charactersIcon.createGraphics().drawPolygon(tab_x, tab_y, 3);


        //charactersIcon.createGraphics().;

        //charactersIcon.createGraphics().drawLine(0, 0, 16, 16);


        if(this.tempChosenCharacter == GameConfiguration.Character.PACMAN){
            chosedIconGhostOrPacman = 2;
        }else if(this.tempChosenCharacter == GameConfiguration.Character.REDGHOST){
            indexGhostsIcon = 0;
            chosedIconGhostOrPacman = 1;
        }else if(this.tempChosenCharacter == GameConfiguration.Character.YELLOWGHOST){
            indexGhostsIcon = 1;
            chosedIconGhostOrPacman = 1;
        }else if(this.tempChosenCharacter == GameConfiguration.Character.BLUEGHOST){
            indexGhostsIcon = 2;
            chosedIconGhostOrPacman = 1;
        }else if(this.tempChosenCharacter == GameConfiguration.Character.PURPLEGHOST){
            indexGhostsIcon = 3;
            chosedIconGhostOrPacman = 1;
        }

        if (chosedIconGhostOrPacman > 0){
            if (chosedIconGhostOrPacman == 1)
                label.setIcon(new ImageIcon(new ImageIcon(charactersIcon
                        .getSubimage(0,indexGhostsIcon*16, 16, 16)).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT)));
            if (chosedIconGhostOrPacman == 2)
                label.setIcon(new ImageIcon(new ImageIcon(pacmanIcon
                        .getSubimage(0,0, 16, 16)).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT)));
        }
    }

    public void refreshChoice (){
        if (this.chosenCharacter == GameConfiguration.Character.PACMAN) {
            pacmanButton.doClick();
        } else if (this.chosenCharacter == GameConfiguration.Character.REDGHOST) {
            redGhostButton.doClick();
        } else if (this.chosenCharacter == GameConfiguration.Character.YELLOWGHOST) {
            yellowGhostButton.doClick();
        } else if (this.chosenCharacter == GameConfiguration.Character.BLUEGHOST) {
            blueGhostButton.doClick();
        } else if (this.chosenCharacter == GameConfiguration.Character.PURPLEGHOST) {
            purpleGhostButton.doClick();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source == okButtonn){
            chosenCharacter = tempChosenCharacter;
            super.hide();
        }
        else if(source == cancelButton)
            super.hide();
        else if (e.getActionCommand() == "Pacman"){
            tempChosenCharacter = Character.PACMAN;
            //System.out.print("Wybrano Pacmana! ");
        } else if (e.getActionCommand() == "Czerwony duszek"){
            tempChosenCharacter = Character.REDGHOST;
            //System.out.print("Wybrano Czerwonego duszka! ");
        } else if (e.getActionCommand() == "Żółty duszek") {
            tempChosenCharacter = Character.YELLOWGHOST;
            //System.out.print("Wybrano Zoltego duszka! ");
        } else if (e.getActionCommand() == "Niebieski duszek") {
            tempChosenCharacter = Character.BLUEGHOST;
            //System.out.print("Wybrano Niebieskiego duszka! ");
        } else if (e.getActionCommand() == "Różowy duszek") {
            tempChosenCharacter = Character.PURPLEGHOST;
            //System.out.print("Wybrano Różowego duszka! ");
        }

        showCharactersIcon();
    }
}
