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
import swingpaint.sprites.JImage;
import swingpaint.sprites.JRectangle;
import swingpaint.sprites.JSprite;

import java.util.function.Consumer;
import java.util.function.BiConsumer;
import swingpaint.helpers.VoidCallback;

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


public class ProjectEditor extends JPanel implements MouseListener, MouseMotionListener, KeyListener, ActionListener {
    private ArrayList<JSprite> sprites;      // contains all the sprites on the canvas.
    private JSprite focus;                   // the sprite that is currently focused.
    private boolean spriteHeld;             // whether a sprite is held (clicked and held).
    private int dx, dy;                     // x & y displacement of cursor from sprite, used to maintain
                                            // relative cursor position when moving sprites (click and drag).

    private DetailsPanel detailsPanel;      // contains information on focused sprites.
    private boolean detailsPanelVisible;    // whether the details panel is visible.

    private int dragPointHeld;              // the index of the drag point held. -1 if none are held.

    Consumer<String> changeState;           // Callback function to change state.
    Consumer<String> setTitle;              // Callback function to set title of frame.
    VoidCallback framePack;                 // Callback function to pack frame. Used for resizing.
    Consumer<ArrayList<String>> saveProjectCallback;            // Callback function to save project.

    private BufferedImage addIcon;          // Icon to add new sprites.
    private Rectangle addIconRect;          // A rectangle that represents the position and size of the icon.
    private BufferedImage optionsIcon;      // Icon to open options.
    private Rectangle optionsIconRect;      // A rectangle that represents the position and size of the icon.

    private JComboBox<String> spriteSelect;
    private JComboBox<String> optionsSelect;

    private String projectTitle;
    private JTextField projectTitleTextField;

    private JPanel popupPanel;
    private JLabel popupPanelLabel;
    private JTextField popupPanelTextField;


    public ProjectEditor(Consumer<String> changeState, Consumer<String> setTitle, VoidCallback framePack, Consumer<ArrayList<String>> saveProjectCallback) {
        init(changeState, setTitle, framePack, saveProjectCallback);

        // Canvas has one initial sprite.
        createSprite("rectangle");

        // New projects are initially titled "Untitled".
        setProjectTitle("Untitled");
    }

    public ProjectEditor(Consumer<String> changeState, Consumer<String> setTitle, VoidCallback framePack, Consumer<ArrayList<String>> saveProjectCallback, ArrayList<String> data) {
        init(changeState, setTitle, framePack, saveProjectCallback);
        importData(data);
    }
    
    public void init(Consumer<String> changeState, Consumer<String> setTitle, VoidCallback framePack, Consumer<ArrayList<String>> saveProjectCallback) {
        // Configuring JPanel
        setPreferredSize(new Dimension(1280, 800));
        setFocusable(true);
        setLayout(null);

        // Adding Listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        // Initializing variables
        this.changeState = changeState;
        this.setTitle = setTitle;
        this.framePack = framePack;
        this.saveProjectCallback = saveProjectCallback;
        sprites = new ArrayList<>();
        spriteHeld = false;

        // Initializing menus.
        detailsPanel = new DetailsPanel();
        detailsPanelVisible = false;

        spriteSelect = new JComboBox<>(new String[]{"Select", "rectangle", "oval", "polygon", "image"});
        spriteSelect.setActionCommand("add sprite");
        spriteSelect.addActionListener(this);

        optionsSelect = new JComboBox<>(new String[]{"Select", "Export", "Home", "Set Title", "Save Project", "Resize Canvas"});
        optionsSelect.setActionCommand("execute option");
        optionsSelect.addActionListener(this);

        popupPanel = new JPanel();
        popupPanel.setBackground(Color.WHITE);
        popupPanelLabel = new JLabel();
        popupPanelTextField = new JTextField();
        popupPanelTextField.addActionListener(this);
        popupPanel.add(popupPanelLabel);
        popupPanel.add(popupPanelTextField);
        
        // Initalizing Images
        try {
            addIcon = ImageIO.read(new File(".\\swingpaint\\assets\\addIcon.png"));
            optionsIcon = ImageIO.read(new File(".\\swingpaint\\assets\\optionsIcon.png"));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        // Initalize rects for buttons.
        addIconRect = new Rectangle(0, 0, addIcon.getWidth(), addIcon.getHeight());
        optionsIconRect = new Rectangle(0, 0, optionsIcon.getWidth(), optionsIcon.getHeight());
        layoutComponents();

        // Initialize Project Title Text Field which sets the title of the working project.
        projectTitleTextField = new JTextField(projectTitle, 10);
        projectTitleTextField.addActionListener(this);
        projectTitleTextField.setActionCommand("setTitle");
    }


    // Lays out components.
    private void layoutComponents() {
        addIconRect.setLocation(getPreferredSize().width-addIcon.getWidth()-5, 5);
        optionsIconRect.setLocation(addIconRect.x-optionsIcon.getWidth()-5, addIconRect.y);
    }


    // Creates a sprite on the canvas.
    private void createSprite(String type) {
        JSprite newSprite;

        switch(type) {
            case "rectangle":
                newSprite = new JRectangle(0, 0, 20, 20);
                sprites.add(newSprite);
                setFocus(newSprite);
                break;
            case "oval":
                newSprite = new JOval(0, 0, 20, 20);
                sprites.add(newSprite);
                setFocus(newSprite);
                break;
            case "polygon":
                // Show the popup panel and ask user for number of points on polygon.
                showPopupPanel("Number of Points", "createPolygon", "3");
                break;
            case "image":
                // TODO: selection pane
                // Show the popup panel and ask user for the path to the image file.
                showPopupPanel("Image Name", "createImage", "");
                break;
        }

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
        spriteSelect.showPopup();
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
        optionsSelect.setBounds(getWidth()-85, addIconRect.y+addIconRect.height+5, 80, 30);
        add(optionsSelect);
        optionsSelect.showPopup();
        optionsSelect.revalidate();
        repaint();
    }


    // Hides the options dropdown list.
    private void hideOptions() {
        remove(optionsSelect);
        repaint();
    }


    // Changes the title of the project.
    private void setProjectTitle(String newTitle) {
        projectTitle = newTitle;

        // Change the title of the frame to match new title.
        setTitle.accept("Swing Paint - " + newTitle);
    }


    // Shows the popup panel.
    private void showPopupPanel(String labelText, String textFieldCommand, String textFieldValue) {
        popupPanelLabel.setText(labelText);
        popupPanelTextField.setColumns(10);
        popupPanelTextField.setActionCommand(textFieldCommand);
        popupPanelTextField.setText(textFieldValue);

        popupPanel.setSize(popupPanel.getPreferredSize());
        // Center the popup panel in frame.
        popupPanel.setLocation(this.getWidth()/2-popupPanel.getWidth()/2, this.getHeight()/2-popupPanel.getHeight()/2);
        add(popupPanel);
        popupPanel.revalidate();
        repaint();

        popupPanelTextField.requestFocusInWindow();
    }


    // Hides the popup panel.
    private void hidePopupPanel() {
        remove(popupPanel);
        repaint();
    }

    
    // Handle the different commands sent to this object by various action listeners.
    public void actionPerformed(ActionEvent e) {
        // Add a new sprite.
        if("add sprite".equals(e.getActionCommand())) {
            String selection = (String)spriteSelect.getSelectedItem();
            hideSpriteSelect();
            createSprite(selection);
        }

        // Execute an option command.
        else if("execute option".equals(e.getActionCommand())) {
            String selection = (String)optionsSelect.getSelectedItem();
            hideOptions();
            if("Export".equals(selection)) {
                export();
            }
            else if("Home".equals(selection)) {
                changeState.accept("Home");
            }
            else if("Set Title".equals(selection)) {
                showPopupPanel("Project Title", "setTitle", projectTitle);
            }
            else if("Save Project".equals(selection)) {
                saveProject();
            }
            else if("Resize Canvas".equals(selection)) {
                String currentCanvasSize = String.format("%s,%s", Integer.toString(getWidth()), Integer.toString(getHeight()));
                showPopupPanel("New Dimensions (width,height)", "setCanvasSize", currentCanvasSize);
            }
        }

        // Set the title.
        else if("setTitle".equals(e.getActionCommand())) {
            String newTitle = popupPanelTextField.getText();
            setProjectTitle(newTitle);
            hidePopupPanel();
        }

        // Creates a polygon with specified number of points in popup panel text field.
        else if("createPolygon".equals(e.getActionCommand())) {
            JSprite newSprite = new JPolygon(JPolygon.createDefaultPolygon(Integer.parseInt(popupPanelTextField.getText())));
            sprites.add(newSprite);
            setFocus(newSprite);
            hidePopupPanel();
        }

        // Creates an image sprite with the specified image path.
        else if("createImage".equals(e.getActionCommand())) {
            String imageName = popupPanelTextField.getText();
            JImage image = new JImage(JImage.imageFromPath("userImages\\" + imageName), imageName);
            sprites.add(image);
            setFocus(image);
            hidePopupPanel();
        }

        // Sets the canvas size.
        else if("setCanvasSize".equals(e.getActionCommand())) {
            String input = popupPanelTextField.getText();
            String[] bits = input.split(",");
            int width = Integer.parseInt(bits[0]);
            int height = Integer.parseInt(bits[1]);
            resizeCanvas(width, height);
            hidePopupPanel();
        }
    }

    // Creates sprites from String[] where each element represents a Sprite.
    private void importData(ArrayList<String> data) {
        String[] bits;

        // The first line contains the project meta data.
        bits = data.get(0).split(";");
        String projectTitle = bits[1].split("=")[1];
        String projectSizeBits = bits[2].split("=")[1];
        int projectWidth = Integer.parseInt(projectSizeBits.split(",")[0]);
        int projectHeight = Integer.parseInt(projectSizeBits.split(",")[1]);
        setProjectTitle(projectTitle);
        resizeCanvas(projectWidth, projectHeight);

        // The remaining lines contain sprite data.
        for(int i = 1; i < data.size(); i++) {
            bits = data.get(i).split(";");
            String type = bits[0].split("=")[1];
            switch(type) {
                case "rectangle": {
                    // Create JRectangle and set color
                    JRectangle jr = new JRectangle(JRectangle.rectangleFromString(bits));
                    jr.setRGBString(bits[5].split("=")[1]);
                    sprites.add(jr);
                    break;
                }
                case "oval": {
                    // Create JOval and set color
                    JOval jo = new JOval(JRectangle.rectangleFromString(bits));
                    jo.setRGBString(bits[5].split("=")[1]);
                    sprites.add(jo);
                    break;
                }
                case "polygon": {
                    // Create JPolygon and set color
                    JPolygon jp = new JPolygon(JPolygon.polygonFromString(bits));
                    jp.setRGBString(bits[3].split("=")[1]);
                    sprites.add(jp);
                    break;
                }
                case "image": {
                    // Extract attributes.
                    int x = Integer.parseInt(bits[1].split("=")[1]);
                    int y = Integer.parseInt(bits[2].split("=")[1]);
                    int width = Integer.parseInt(bits[3].split("=")[1]);
                    int height = Integer.parseInt(bits[4].split("=")[1]);
                    String imageName = bits[5].split("=")[1];

                    // Create JImage
                    JImage ji = new JImage(JImage.imageFromName(imageName), imageName, x, y, width, height);
                    sprites.add(ji);
                    break;
                }
            }
        }
    }

    // Exports the canvas to a Java Swing code file.
    private void export() {
        // Create parent directory for exported files.
        File exportDirectory = new File("export");
        if(!exportDirectory.exists()) {
            exportDirectory.mkdir();
        }

        // Initialize some variables and gather some sprite information
        // in preparation for implementing paintComponent.
        boolean containsPolygon = false;     // whether the exported data contains a polygon sprite.
        boolean containsImage = false;       // whether the exported data contains an image sprite.
        ArrayList<JImage> jImageList = new ArrayList<>();   // contains all the JImage sprites.
        String prevRGBString = null;
        int curImgIndex = 0;                // Tracks which image is currently being loaded.
        for(JSprite s : sprites) {
            if("polygon".equals(s.getType())) {
                containsPolygon = true;
            }
            else if("image".equals(s.getType())) {
                containsImage = true;
                jImageList.add((JImage)s);
            }
        }

        // Export image files.
        if(containsImage) {
            // Create images directory if does not exist.
            File exportImagesDirectory = new File("export\\images");
            if(!exportImagesDirectory.exists()) {
                exportImagesDirectory.mkdir();
            }

            // Export image files to images directory.
            for(JImage jimg : jImageList) {
                File destination = new File("export\\images\\" + jimg.getImageName());
                try {
                    ImageIO.write(jimg.getImage(), jimg.getImageFileExtension(), destination);
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }


        try(PrintWriter pw = new PrintWriter(new FileWriter(".\\export\\Program.java"))) {
            // Begin constructing java program.
            pw.println("import javax.swing.JFrame;");
            pw.println("import javax.swing.JPanel;");
            pw.println("import java.awt.Graphics;");
            pw.println("import java.awt.Color;");
            pw.println("import java.awt.Dimension;");
            if(containsImage) {
                // import packages needed to handle images.
                pw.println("import java.util.ArrayList;");
                pw.println("import javax.imageio.ImageIO;");
                pw.println("import java.io.File;");
                pw.println("import java.io.IOException;");
                pw.println("import java.awt.image.BufferedImage;");
            }
            pw.println("");

            pw.println("public class Program extends JFrame {");
            pw.println("\tpublic Program() {");
            pw.println("\t\tsetDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);");
            pw.printf("\t\tsetTitle(\"%s\");%n", projectTitle);
            pw.println("\t\tsetResizable(false);");
            pw.println("\t\tadd(new Canvas());");
            pw.println("\t\tpack();");

            pw.println("\t}");
            pw.println("");
            pw.println("\tpublic static void main(String[] args) {");
            pw.println("\t\tProgram program = new Program();");
            pw.println("\t\tprogram.setVisible(true);");
            pw.println("\t}");
            pw.println("}");
            pw.println("");

            pw.println("class Canvas extends JPanel {");
            if(containsImage) {
                // Create List to store images.
                pw.println("\tArrayList<BufferedImage> imgs;");
            }
            pw.println("");
            pw.println("\tpublic Canvas() {");
            pw.printf("\t\tsetPreferredSize(new Dimension(%d, %d));%n", getWidth(), getHeight());
            if(containsImage) {
                pw.println("");
                // Initialize List to store images.
                pw.println("\t\timgs = new ArrayList<>();");
                pw.println("\t\ttry {");
                
                // Load images into ArrayList.
                for(JImage jimg : jImageList) {
                    pw.printf("\t\t\timgs.add(ImageIO.read(new File(\"images\\\\%s\")));%n", jimg.getImageName());
                }

                pw.println("\t\t}");
                pw.println("\t\tcatch(IOException e) {");
                pw.println("\t\t\te.printStackTrace();");
                pw.println("\t\t}");
            }
            pw.println("\t}");
            pw.println("\tpublic void paintComponent(Graphics g) {");

            
            // Create xpoints/ypoints arrays for polygon implementation.
            if(containsPolygon) {
                pw.println("\t\tint[] xpoints, ypoints;");
            }

            // Now, convert each sprite into java swing code.
            for(int i = 0; i < sprites.size(); i++) {
                JSprite s = sprites.get(i);
                String RGBString = s.getRGBString();
                // Skip the color statement if no color change is needed.
                if(!RGBString.equals(prevRGBString)) {
                    pw.printf("\t\tg.setColor(new Color(%s));%n", s.getRGBString());
                }
                prevRGBString = RGBString;
                switch(s.getType()) {
                    case "rectangle": {
                        pw.printf("\t\tg.fillRect(%d, %d, %d, %d);%n", s.x, s.y, s.width, s.height);
                        break;
                    }
                    case "oval": {
                        pw.printf("\t\tg.fillOval(%d, %d, %d, %d);%n", s.x, s.y, s.width, s.height);
                        break;
                    }
                    case "polygon": {
                        // Construct statements as strings to initialize xpoints and ypoints.
                        Polygon polygon = ((JPolygon)s).getPolygon();
                        String xPointsString = "\t\txpoints = new int[]{";
                        String yPointsString = "\t\typoints = new int[]{";
                        for(int j = 0; j < polygon.npoints; j++) {
                            xPointsString += Integer.toString(polygon.xpoints[j]) + ", ";
                            yPointsString += Integer.toString(polygon.ypoints[j]) + ", ";
                        }
                        xPointsString = xPointsString.substring(0, xPointsString.length()-2) + "};";
                        yPointsString = yPointsString.substring(0, yPointsString.length()-2) + "};";

                        pw.println(xPointsString);
                        pw.println(yPointsString);
                        pw.printf("\t\tg.fillPolygon(xpoints, ypoints, %d);%n", polygon.npoints);
                        break;
                    }

                    case "image": {
                        pw.printf("\t\tg.drawImage(imgs.get(%d), %d, %d, null);%n", curImgIndex, s.x, s.y);
                        curImgIndex++;

                        break;
                    }
                }
            }
            pw.println("\t}");
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


    // Saves project to be edited in the future.
    private void saveProject() {
        ArrayList<String> projectData = new ArrayList<>();

        projectData.add(String.format("ProjectStart;title=%s;size=%d,%d", projectTitle, getWidth(), getHeight()));     
        for(JSprite s : sprites) {
            projectData.add(s.toString());
        }
        projectData.add("ProjectEnd");

        saveProjectCallback.accept(projectData);
    }


    // Resizes the canvas to width and height in text field.
    private void resizeCanvas(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        layoutComponents();
        framePack.accept();
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
            else if("image".equals(sprite.getType())) {
                g.drawImage(((JImage)sprite).getImage(), sprite.x, sprite.y, null);
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
            // Menus should disappear upon a click which is not on the menu.
            hideDetailsPanel();
            hideOptions();
            hideSpriteSelect();

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
                // Loop through backwards so newest sprites get click priority.
                for(int i = sprites.size()-1; i >= 0; i--) {
                    if(sprites.get(i).contains(p)) {
                        setFocus(sprites.get(i));
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
            for(int i = sprites.size()-1; i >= 0; i--) {
                if(sprites.get(i).contains(p)) {
                    setFocus(sprites.get(i));
                    showDetailsPanel(0, 0);
                    break;
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
                ProjectEditor.this.createSprite("rectangle");
                break;

            // 2 -> create new oval.
            case '2':
                ProjectEditor.this.createSprite("oval");
                break;

            // 3 -> create new polygon.
            case '3':
                ProjectEditor.this.createSprite("polygon");
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

            // s -> save.
            case 's':
                saveProject();
                break;
        }
    }

    // Unused key listener methods. TODO: Adapter.
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

            // Loop through attributes and create a different row depending on the attribute.
            for(int i = 0; i < s.getAttributes().size(); i++) {
                String attribute = s.getAttributes().get(i);

                // First, check if the attribute defines a point in a polygon.
                String[] bits = attribute.split(" ");
                if("point".equals(bits[0])) {
                    Polygon polygon = ((JPolygon)s).getPolygon();
                    int pointIndex = Integer.parseInt(bits[1]) - 1;
                    if("x".equals(bits[2])) {
                        attributeRows.add(new AttributeRow(attribute, String.format("Point %s X", bits[1]), 4, Integer.toString(polygon.xpoints[pointIndex]), "set " + attribute));
                    }
                    else if("y".equals(bits[2])) {
                        attributeRows.add(new AttributeRow(attribute, String.format("Point %s Y", bits[1]), 4, Integer.toString(polygon.ypoints[pointIndex]), "set " + attribute));
                    }

                    continue;
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
            String[] bits = e.getActionCommand().split(" ");
            if("set".equals(bits[0])) {
                switch(bits[1]) {
                    case "width":
                        focus.setSize(Integer.parseInt(searchRowByAttribute("width").textField.getText()), (int)focus.getHeight());
                        ProjectEditor.this.repaint();
                        break;
                    case "height":
                        focus.setSize((int)focus.getWidth(), Integer.parseInt(searchRowByAttribute("height").textField.getText()));
                        ProjectEditor.this.repaint();
                        break;
                    case "x":
                        focus.setLocation(Integer.parseInt(searchRowByAttribute("x").textField.getText()), (int)focus.getY());
                        ProjectEditor.this.repaint();
                        break;
                    case "y":
                        focus.setLocation((int)focus.getX(), Integer.parseInt(searchRowByAttribute("y").textField.getText()));
                        ProjectEditor.this.repaint();
                        break;
                    case "color":
                        String rgbString = searchRowByAttribute("color").textField.getText();
                        focus.setRGBString(rgbString);
                        ProjectEditor.this.repaint();
                        break;
                    case "point":
                        JPolygon polygon = (JPolygon)focus;
                        int pointIndex = Integer.parseInt(bits[2]) - 1;
                        if("x".equals(bits[3])) {
                            int newX = Integer.parseInt(searchRowByAttribute(String.format("%s %s %s", bits[1], bits[2], bits[3])).textField.getText());
                            polygon.movePoint(pointIndex, newX, polygon.getPolygon().ypoints[pointIndex]);
                            ProjectEditor.this.repaint();
                            break;
                        }
                        else if("y".equals(bits[3])) {
                            int newY = Integer.parseInt(searchRowByAttribute(String.format("%s %s %s", bits[1], bits[2], bits[3])).textField.getText());
                            polygon.movePoint(pointIndex, polygon.getPolygon().xpoints[pointIndex], newY);
                            ProjectEditor.this.repaint();
                            break;
                        }
                        break;
                }
            }

            // Delete focused sprite upon delete command.
            else if("delete".equals(bits[0])) {
                int i = sprites.indexOf(focus);
                if(i != -1) {
                    ProjectEditor.this.removeSprite(i);
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
            private JButton duplicateButton;

            public ButtonRow() {
                setBackground(Color.GRAY);

                deleteButton = new JButton("Delete");
                deleteButton.setActionCommand("delete");
                deleteButton.addActionListener(detailsPanel);
                
                add(deleteButton);
            }
        }
        
    }
}
