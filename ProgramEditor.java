import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;

import sprites.JSprite;
import sprites.JRectangle;
import sprites.JOval;
import sprites.JPolygon;

import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
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


class ProgramEditor extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private ArrayList<JSprite> sprites;      // contains all the sprites on the canvas.
    private JSprite focus;                   // the sprite that is currently focused.
    private boolean spriteHeld;             // whether a sprite is held (clicked and held).
    private int dx, dy;                     // x & y displacement of cursor from sprite, used to maintain
                                            // relative cursor position when moving sprites (click and drag).

    private DetailsPanel detailsPanel;      // contains information on focused sprites.
    private boolean detailsPanelVisible;    // whether the details panel is visible.

    private int dragPointHeld;              // the index of the drag point held. -1 if none are held.

    
    public ProgramEditor() {
        // Initializing variables
        sprites = new ArrayList<>();
        spriteHeld = false;
        detailsPanel = new DetailsPanel();
        detailsPanelVisible = false;

        // Configuring JPanel
        setPreferredSize(new Dimension(400, 400));
        setFocusable(true);
        setLayout(null);

        // Adding Listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        // Canvas has one initial sprite.
        createSprite("rect");
    }


    // Creates a sprite on the canvas.
    private void createSprite(String type) {
        switch(type) {
            case "rect":
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


    // Exports the canvas to a Java Swing code file.
    private void export() {
        try(PrintWriter pw = new PrintWriter(new FileWriter("out.txt"))) {
            pw.println("public void paintComponent(Graphics g)");
            pw.println("{");
            for(int i = 0; i < sprites.size(); i++) {
                JSprite s = sprites.get(i);
                pw.printf("\tg.setColor(new Color(%s));%n", s.getRGBString());
                switch(s.getType()) {
                    case "rectangle":
                        pw.printf("\tg.fillRect(%d, %d, %d, %d);%n", s.x, s.y, s.width, s.height);
                        break;
                    case "oval":
                        pw.printf("\tg.fillOval(%d, %d, %d, %d);%n", s.x, s.y, s.width, s.height);
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


    // Paints the sprites on the canvas.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // For-loop which paints each sprite individually.
        for(JSprite sprite : sprites) {
            g.setColor(sprite.getColor());
            if("rect".equals(sprite.getType())) {
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
    }


    // Implementing Mouse Listener methods 
    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            hideDetailsPanel();
            dragPointHeld = -1;
            Point p = e.getPoint();

            // First, check if the click is on one of the corner rectangles.
            if(focus != null) {
                for(int i = 0; i < focus.getDragPoints().length; i++) {
                    Rectangle r = focus.getDragPoints()[i];
                    if(r.contains(p)) {
                        dragPointHeld = i;
                    }
                }
            }

            // If the click was not on a corner rectangle, check if it was on a sprite.
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
                ProgramEditor.this.createSprite("rect");
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
                    sprites.remove(i);
                    hideDetailsPanel();
                    repaint();
                }
                break;

            // e -> export canvas to code.
            case 'e':
                export();

            // q -> toggle details panel.
            case 'q':
                if(detailsPanelVisible) {
                    hideDetailsPanel();
                }
                else {
                    showDetailsPanel(0, 0);
                }
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
        private ArrayList<Row> rows;    // Each row is assigned an attribute.

        public DetailsPanel() {
            rows = new ArrayList<>();

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBounds(0, 0, 200, 30*rows.size());
            setBackground(Color.GREEN);
        }


        // Clears child components and rows list.
        private void clearRows() {
            removeAll();
            rows.clear();
        }


        // Replaces old rows with new rows which reflect the sprite parameter.
        public void update(JSprite s) {
            clearRows();
            setSize(200, 30*s.getAttributes().size());

            for(int i = 0; i < s.getAttributes().size(); i++) {
                String attribute = s.getAttributes().get(i);

                // Create a different row depending on the attribute.
                switch(attribute) {
                    case "x":
                        rows.add(new Row(attribute, "X", 4, Integer.toString(s.x), "set x"));
                        break;
                    case "y":
                        rows.add(new Row(attribute, "Y", 4, Integer.toString(s.y), "set y"));
                        break;
                    case "width":
                        rows.add(new Row(attribute, "Width", 4,Integer.toString(s.width) ,"set width"));
                        break;
                    case "height":
                        rows.add(new Row(attribute, "Height", 4, Integer.toString(s.height), "set height"));
                        break;
                    case "color":
                        rows.add(new Row(attribute, "Color", 8, String.format("%d,%d,%d", s.getColor().getRed(), s.getColor().getGreen(), s.getColor().getBlue()), "set color"));
                        break;
                    case "type":
                        rows.add(new Row(attribute, "Type", 8, s.getType(), "set type"));
                        break;
                }
            }

            // Make rows alternating colors (orange and pink)
            for(int i = 0; i < rows.size(); i++) {
                Row r = rows.get(i);
                if(i%2 == 0) {
                    r.setBackground(Color.ORANGE);
                }
                else {
                    r.setBackground(Color.PINK);
                }

                r.textField.addActionListener(this);
                add(r);
            }
        }


        // Handle actions, primarily from Text Fields which update getAttributes().
        public void actionPerformed(ActionEvent e) {
            switch(e.getActionCommand()) {
                case "set width":
                    focus.setSize(Integer.parseInt(searchRowByAttribute("width").textField.getText()), (int)focus.getHeight());
                    ProgramEditor.this.repaint();
                    break;
                case "set height":
                    focus.setSize((int)focus.getWidth(), Integer.parseInt(searchRowByAttribute("height").textField.getText()));
                    ProgramEditor.this.repaint();
                    break;
                case "set x":
                    focus.setLocation(Integer.parseInt(searchRowByAttribute("x").textField.getText()), (int)focus.getY());
                    ProgramEditor.this.repaint();
                    break;
                case "set y":
                    focus.setLocation((int)focus.getX(), Integer.parseInt(searchRowByAttribute("y").textField.getText()));
                    ProgramEditor.this.repaint();
                    break;
                case "set color":
                    String rgbString = searchRowByAttribute("color").textField.getText();
                    String[] bits = rgbString.split(",");
                    focus.setRGBString(Integer.parseInt(bits[0]), Integer.parseInt(bits[1]), Integer.parseInt(bits[2]));
                    ProgramEditor.this.repaint();
                    break;
            }
        }


        // Returns a row given an attribute. Linear search.
        private Row searchRowByAttribute(String attribute) {
            for(Row r : rows) {
                if(r.attribute.equals(attribute)) {
                    return r;
                }
            }

            return null;
        }


        // Defines a single row.
        private class Row extends JPanel {
            private String attribute;
            private JLabel label;
            private JTextField textField;

            public Row(String attribute, String labelText, int fieldColumns, String fieldText, String command) {
                this.attribute = attribute;
                label = new JLabel(labelText);
                textField = new JTextField(fieldColumns);
                textField.setActionCommand(command);
                textField.setText(fieldText);

                add(label);
                add(textField);
            }
        }

        
    }
}
