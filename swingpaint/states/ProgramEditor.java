package swingpaint.states;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.ImageIcon;

import swingpaint.sprites.JOval;
import swingpaint.sprites.JPolygon;
import swingpaint.sprites.JRectangle;
import swingpaint.sprites.JSprite;

import java.util.function.Consumer;

import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;


public class ProgramEditor extends JPanel implements MouseListener, MouseMotionListener, KeyListener, ActionListener {
    private ArrayList<JSprite> sprites;      // contains all the sprites on the canvas.
    private JSprite focus;                   // the sprite that is currently focused.
    private boolean spriteHeld;             // whether a sprite is held (clicked and held).
    private int dx, dy;                     // x & y displacement of cursor from sprite, used to maintain
                                            // relative cursor position when moving sprites (click and drag).

    private DetailsPanel detailsPanel;      // contains information on focused sprites.
    private boolean detailsPanelVisible;    // whether the details panel is visible.

    private int dragPointHeld;              // the index of the drag point held. -1 if none are held.

    Consumer<String> changeState;           // Callback function to change state.

    private BufferedImage addIcon;          // Icon to add new sprites.
    private Rectangle addIconRect;          // A rectangle that represents the position and size of the icon.
    private BufferedImage optionsIcon;      // Icon to open options.
    private Rectangle optionsIconRect;      // A rectangle that represents the position and size of the icon.

    private JComboBox<String> spriteSelect;
    private JComboBox<String> optionsSelect;

    
    public ProgramEditor(Consumer<String> changeState) {
        // Configuring JPanel
        setPreferredSize(new Dimension(400, 400));
        setFocusable(true);
        setLayout(null);

        // Adding Listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        // Initializing variables
        this.changeState = changeState;
        sprites = new ArrayList<>();
        spriteHeld = false;
        detailsPanel = new DetailsPanel();
        detailsPanelVisible = false;
        spriteSelect = new JComboBox<>(new String[]{"Select", "rectangle", "oval", "polygon"});
        spriteSelect.setActionCommand("add sprite");
        spriteSelect.addActionListener(this);
        optionsSelect = new JComboBox<>(new String[]{"Select", "Export", "Home"});
        optionsSelect.setActionCommand("execute option");
        optionsSelect.addActionListener(this);
        
        // Initalizing Images
        try {
            addIcon = ImageIO.read(new File(".\\swingpaint\\assets\\addIcon.png"));
            optionsIcon = ImageIO.read(new File(".\\swingpaint\\assets\\optionsIcon.png"));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        // Initalize rects for buttons.
        addIconRect = new Rectangle(getPreferredSize().width-addIcon.getWidth()-5, 5, addIcon.getWidth(), addIcon.getHeight());
        optionsIconRect = new Rectangle(addIconRect.x-optionsIcon.getWidth()-5, addIconRect.y, optionsIcon.getWidth(), optionsIcon.getHeight());

        // Canvas has one initial sprite.
        createSprite("rectangle");
    }


    // Creates a sprite on the canvas.
    private void createSprite(String type) {
        switch(type) {
            case "rectangle":
                sprites.add(new JRectangle(0, 0, 20, 20));
                break;
            case "oval":
                sprites.add(new JOval(0, 0, 20, 20));
                break;
            case "polygon":
                Polygon polygon = new Polygon(new int[]{0, 10, 5}, new int[]{0, 0, 10}, 3);
                sprites.add(new JPolygon(polygon));
                break;
        }
        setFocus(sprites.get(sprites.size()-1));
        repaint();
    }


    // Shows the details panel.
    private void showDetailsPanel(int x, int y) {
        detailsPanel.setLocation(x, y);
        detailsPanel.update(focus);
        add(detailsPanel);
        detailsPanel.revalidate();
        repaint();
        detailsPanelVisible = true;
    }


    // Hides the details panel.
    private void hideDetailsPanel() {
        remove(detailsPanel);
        repaint();
        detailsPanelVisible = false;
    }


    // Shows the sprite selection combo box.
    private void showSpriteSelect() {
        spriteSelect.setBounds(getWidth()-85, addIconRect.y+addIconRect.height+5, 80, 30);
        add(spriteSelect);
        spriteSelect.revalidate();
        repaint();
    }
    

    // Hides the sprite selection combo box.
    private void hideSpriteSelect() {
        remove(spriteSelect);
        repaint();
    }


    // Shows the options dropdown list.
    private void showOptions() {
        optionsSelect.setBounds(optionsIconRect.x, optionsIconRect.y+optionsIconRect.height+5, 80, 30);
        add(optionsSelect);
        optionsSelect.revalidate();
        repaint();
    }


    // Hides the options dropdown list.
    private void hideOptions() {
        remove(optionsSelect);
        repaint();
    }

    
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if("add sprite".equals(e.getActionCommand())) {
            String selection = (String)spriteSelect.getSelectedItem();
            hideSpriteSelect();
            createSprite(selection);
        }
        else if("execute option".equals(e.getActionCommand())) {
            String selection = (String)optionsSelect.getSelectedItem();
            hideOptions();
            if("Export".equals(selection)) {
                export();
            }
            else if("Home".equals(selection)) {
                changeState.accept("Home");
            }
        }
    }


    // Exports the canvas to a Java Swing code file.
    private void export() {
        try(PrintWriter pw = new PrintWriter(new FileWriter("out.txt"))) {
            boolean firstPolygon = true;    // whether the first polygon has been created.
            String prevRGBString = null;
            pw.println("public void paintComponent(Graphics g)");
            pw.println("{");
            for(int i = 0; i < sprites.size(); i++) {
                JSprite s = sprites.get(i);
                String RGBString = s.getRGBString();
                // Skip the color statement if no color change is needed.
                if(!RGBString.equals(prevRGBString)) {
                    pw.printf("\tg.setColor(new Color(%s));%n", s.getRGBString());
                }
                prevRGBString = RGBString;
                switch(s.getType()) {
                    case "rectangle":
                        pw.printf("\tg.fillRect(%d, %d, %d, %d);%n", s.x, s.y, s.width, s.height);
                        break;
                    case "oval":
                        pw.printf("\tg.fillOval(%d, %d, %d, %d);%n", s.x, s.y, s.width, s.height);
                        break;
                    case "polygon":
                        // Must declare variables if this is the first polygon.
                        if(firstPolygon) {
                            pw.println("\tint[] xpoints, ypoints;");
                            firstPolygon = false;
                        }

                        // Construct statements as strings to initialize xpoints and ypoints.
                        Polygon polygon = ((JPolygon)s).getPolygon();
                        String xPointsString = "\txpoints = new int[]{";
                        String yPointsString = "\typoints = new int[]{";
                        for(int j = 0; j < polygon.npoints; j++) {
                            xPointsString += Integer.toString(polygon.xpoints[j]) + ", ";
                            yPointsString += Integer.toString(polygon.ypoints[j]) + ", ";
                        }
                        xPointsString = xPointsString.substring(0, xPointsString.length()-2) + "};";
                        yPointsString = yPointsString.substring(0, xPointsString.length()-2) + "};";

                        pw.println(xPointsString);
                        pw.println(yPointsString);
                        pw.printf("\tg.fillPolygon(xpoints, ypoints, %d);%n", polygon.npoints);
                        break;
                }
            }
            pw.println("}");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }



    // Changes the focused sprite.
    private void setFocus(JSprite s) {
        focus = s;
        s.moveDragPoints();
    }


    // Unfocuses sprite.
    private void removeFocus() {
        focus = null;
    }


    // Removes a sprite given its index in the sprites array.
    private void removeSprite(int index) {
        sprites.remove(index);
        hideDetailsPanel();
        removeFocus();
        repaint();
    }


    // Paints the sprites on the canvas.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // For-loop which paints each sprite individually.
        for(JSprite sprite : sprites) {
            g.setColor(sprite.getColor());
            if("rectangle".equals(sprite.getType())) {
                g.fillRect(sprite.x, sprite.y, sprite.width, sprite.height);
            }
            else if("oval".equals(sprite.getType())) {
                g.fillOval(sprite.x, sprite.y, sprite.width, sprite.height);
            }
            else if("polygon".equals(sprite.getType())) {
                g.fillPolygon(((JPolygon)sprite).getPolygon());
            }
        }

        // If there is a focused sprite, paint the corner rectangles as well (for resizing).
        if(focus != null) {
            for(Rectangle r : focus.getDragPoints()) {
                g.setColor(Color.BLUE);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        }

        // Paint utility icons.
        g.drawImage(addIcon, addIconRect.x, addIconRect.y, null);
        g.drawImage(optionsIcon, optionsIconRect.x, optionsIconRect.y, null);
    }


    // Implementing Mouse Listener methods 
    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            hideDetailsPanel();
            dragPointHeld = -1;
            Point p = e.getPoint();

            // Check if the click was on a button.
            if(addIconRect.contains(p)) {
                showSpriteSelect();
                return;
            }
            else if(optionsIconRect.contains(p)) {
                showOptions();
                return;
            }


            // Check if the click is on one of the corner rectangles.
            if(focus != null) {
                for(int i = 0; i < focus.getDragPoints().length; i++) {
                    Rectangle r = focus.getDragPoints()[i];
                    if(r.contains(p)) {
                        dragPointHeld = i;
                        return;
                    }
                }
            }

            // Check if the click was on a sprite.
            if(dragPointHeld == -1) {
                for(JSprite sprite : sprites) {
                    if(sprite.contains(p)) {
                        setFocus(sprite);
                        spriteHeld = true;
                        dx = (int)p.getX() - (int)focus.getX();
                        dy = (int)p.getY() - (int)focus.getY();
                        break;
                    }
                }
                if(!spriteHeld) {
                    // no sprite was clicked, remove focus.
                    removeFocus();
                }
            }
        }

        // Show the details panel if right click on sprite.
        else if(e.getButton() == MouseEvent.BUTTON3) {
            Point p = e.getPoint();
            for(JSprite sprite : sprites) {
                if(sprite.contains(p)) {
                    setFocus(sprite);
                    showDetailsPanel(0, 0);
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();

        // Handle corner resizing.
        if(dragPointHeld != -1) {
            focus.handleDragPoint(dragPointHeld, p);
            focus.moveDragPoints();
            repaint();
        }

        // Handle sprite click-and-drag movement.
        else if(spriteHeld) {
            focus.setLocation((int)p.getX()-dx, (int)p.getY()-dy);
            focus.moveDragPoints();
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Drop sprite.
        if(e.getButton() == MouseEvent.BUTTON1) {
            spriteHeld = false;
        }
    }

    // Unused mouse listener methods. Adapter to be implemented.
    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mouseMoved(MouseEvent e) {
    }


    // Implementing Key Listener methods 
    @Override
    public void keyReleased(KeyEvent e) {
        // check for commands.
        switch(e.getKeyChar()) {
            // 1 -> create new rect.
            case '1':
                ProgramEditor.this.createSprite("rectangle");
                break;

            // 2 -> create new oval.
            case '2':
                ProgramEditor.this.createSprite("oval");
                break;

            // 3 -> create new polygon.
            case '3':
                ProgramEditor.this.createSprite("polygon");
                break;

            // d -> delete focused sprite.
            case 'd':
                int i = sprites.indexOf(focus);
                if(i != -1) {
                    removeSprite(i);
                }
                break;

            // e -> export canvas to code.
            case 'e':
                export();
                break;

            // q -> toggle details panel.
            case 'q':
                if(detailsPanelVisible) {
                    hideDetailsPanel();
                }
                else {
                    showDetailsPanel(0, 0);
                }
                break;

            // h -> return to home state.
            case 'h':
                changeState.accept("Home");
                break;
        }
    }

    // Unused key listener methods. Adapter to be implemented.
    public void keyPressed(KeyEvent e) {
    }
    public void keyTyped(KeyEvent e) {
    }





    // The details panel allows the user to view and edit getAttributes() of a focused sprite.
    private class DetailsPanel extends JPanel implements ActionListener {
        private ArrayList<AttributeRow> attributeRows;    // Each row is assigned an attribute.

        public DetailsPanel() {
            attributeRows = new ArrayList<>();

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBounds(0, 0, 200, 30*attributeRows.size());
            setBackground(Color.GREEN);
        }


        // Clears child components and rows list.
        private void clearRows() {
            removeAll();
            attributeRows.clear();
        }


        // Replaces old rows with new rows which reflect the sprite parameter.
        public void update(JSprite s) {
            clearRows();

            for(int i = 0; i < s.getAttributes().size(); i++) {
                String attribute = s.getAttributes().get(i);

                // Create a different row depending on the attribute.
                // First, check if the attribute defines a point in a polygon.
                try {
                    if("point".equals(attribute.substring(0, 5))) {
                        Polygon polygon = ((JPolygon)s).getPolygon();
                        String[] bits = attribute.split(" ");
                        int pointIndex = Integer.parseInt(bits[1]) - 1;
                        if("x".equals(bits[2])) {
                            attributeRows.add(new AttributeRow(attribute, String.format("Point %s X", bits[1]), 4, Integer.toString(polygon.xpoints[pointIndex]), "set " + attribute));
                        }
                        else if("y".equals(bits[2])) {
                            attributeRows.add(new AttributeRow(attribute, String.format("Point %s Y", bits[1]), 4, Integer.toString(polygon.ypoints[pointIndex]), "set " + attribute));
                        }

                        continue;
                    }
                }
                catch (StringIndexOutOfBoundsException e) {
                    // Do nothing.
                }

                // If not a point, then proceed normally.
                switch(attribute) {
                    case "x":
                        attributeRows.add(new AttributeRow(attribute, "X", 4, Integer.toString(s.x), "set x"));
                        break;
                    case "y":
                        attributeRows.add(new AttributeRow(attribute, "Y", 4, Integer.toString(s.y), "set y"));
                        break;
                    case "width":
                        attributeRows.add(new AttributeRow(attribute, "Width", 4,Integer.toString(s.width) ,"set width"));
                        break;
                    case "height":
                        attributeRows.add(new AttributeRow(attribute, "Height", 4, Integer.toString(s.height), "set height"));
                        break;
                    case "color":
                        attributeRows.add(new AttributeRow(attribute, "Color", 8, String.format("%d,%d,%d", s.getColor().getRed(), s.getColor().getGreen(), s.getColor().getBlue()), "set color"));
                        break;
                    case "type":
                        attributeRows.add(new AttributeRow(attribute, "Type", 8, s.getType(), "set type"));
                        break;
                }
            }

            // Make rows alternating colors (orange and pink)
            for(int i = 0; i < attributeRows.size(); i++) {
                AttributeRow r = attributeRows.get(i);
                if(i%2 == 0) {
                    r.setBackground(Color.ORANGE);
                }
                else {
                    r.setBackground(Color.PINK);
                }

                r.textField.addActionListener(this);
                add(r);
            }

            // Add button row.
            add(new ButtonRow());

            // Set the size of the container.
            setSize(getPreferredSize());
        }


        // Handle actions, primarily from Text Fields which update getAttributes().
        public void actionPerformed(ActionEvent e) {
            System.out.println(e.getActionCommand());
            String[] bits = e.getActionCommand().split(" ");
            if("set".equals(bits[0])) {
                switch(bits[1]) {
                    case "width":
                        focus.setSize(Integer.parseInt(searchRowByAttribute("width").textField.getText()), (int)focus.getHeight());
                        ProgramEditor.this.repaint();
                        break;
                    case "height":
                        focus.setSize((int)focus.getWidth(), Integer.parseInt(searchRowByAttribute("height").textField.getText()));
                        ProgramEditor.this.repaint();
                        break;
                    case "x":
                        focus.setLocation(Integer.parseInt(searchRowByAttribute("x").textField.getText()), (int)focus.getY());
                        ProgramEditor.this.repaint();
                        break;
                    case "y":
                        focus.setLocation((int)focus.getX(), Integer.parseInt(searchRowByAttribute("y").textField.getText()));
                        ProgramEditor.this.repaint();
                        break;
                    case "color":
                        String rgbString = searchRowByAttribute("color").textField.getText();
                        String[] bitss = rgbString.split(",");
                        focus.setRGBString(Integer.parseInt(bitss[0]), Integer.parseInt(bitss[1]), Integer.parseInt(bitss[2]));
                        ProgramEditor.this.repaint();
                        break;
                    case "point":
                        System.out.println("1");
                        JPolygon polygon = (JPolygon)focus;
                        int pointIndex = Integer.parseInt(bits[2]) - 1;
                        if("x".equals(bits[3])) {
                            System.out.println("2");
                            int newX = Integer.parseInt(searchRowByAttribute(String.format("%s %s %s", bits[1], bits[2], bits[3])).textField.getText());
                            polygon.movePoint(pointIndex, newX, polygon.getPolygon().ypoints[pointIndex]);
                            ProgramEditor.this.repaint();
                            break;
                        }
                        else if("y".equals(bits[3])) {
                            int newY = Integer.parseInt(searchRowByAttribute(String.format("%s %s %s", bits[1], bits[2], bits[3])).textField.getText());
                            polygon.movePoint(pointIndex, polygon.getPolygon().xpoints[pointIndex], newY);
                            ProgramEditor.this.repaint();
                            break;
                        }
                        break;
                }
            }

            // Delete focused sprite upon delete command.
            else if("delete".equals(bits[0])) {
                int i = sprites.indexOf(focus);
                if(i != -1) {
                    ProgramEditor.this.removeSprite(i);
                }
            }
        }


        // Returns a row given an attribute. Linear search.
        private AttributeRow searchRowByAttribute(String attribute) {
            for(AttributeRow r : attributeRows) {
                if(r.attribute.equals(attribute)) {
                    return r;
                }
            }

            return null;
        }


        // Defines a row which contains a configurable attribute of the focused sprite.
        private class AttributeRow extends JPanel {
            private String attribute;
            private JLabel label;
            private JTextField textField;

            public AttributeRow(String attribute, String labelText, int fieldColumns, String fieldText, String command) {
                this.attribute = attribute;
                label = new JLabel(labelText);
                textField = new JTextField(fieldColumns);
                textField.setActionCommand(command);
                textField.setText(fieldText);

                add(label);
                add(textField);
            }
        }

        // A row of buttons. Contains buttons related to sprites (delete, duplicate, etc.)
        private class ButtonRow extends JPanel {
            private JButton deleteButton;
            private ImageIcon deleteIcon;
            private JButton duplicateButton;
            private ImageIcon duplicateIcon;

            public ButtonRow() {
                setBackground(Color.GRAY);

                deleteIcon = new ImageIcon("swingpaint\\assets\\deleteButton.png");
                deleteButton = new JButton(deleteIcon);
                deleteButton.setActionCommand("delete");
                deleteButton.addActionListener(detailsPanel);
                
                add(deleteButton);
            }
        }
        
    }
}
