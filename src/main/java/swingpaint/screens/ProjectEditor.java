package swingpaint.screens;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import swingpaint.helpers.Project;
import swingpaint.helpers.Screen;
import swingpaint.screens.Settings.Setting;
import swingpaint.sprites.JImage;
import swingpaint.sprites.JOval;
import swingpaint.sprites.JPolygon;
import swingpaint.sprites.JRectangle;
import swingpaint.sprites.JSprite;


// This screen handles all project editing. The main screen of the program.
public class ProjectEditor extends JPanel implements ActionListener {
    // General variables.
    private ArrayList<JSprite> sprites;     // contains all the sprites on the canvas.
    private JSprite focus;                  // the sprite that is currently focused.
    private boolean spriteHeld;             // whether a sprite is held (clicked and held).
    private int dx, dy;                     // x & y displacement of cursor from sprite, used to maintain
                                            // relative cursor position when moving sprites (click and drag).
    private int dragPointHeld;              // the index of the drag point held. -1 if none are held.
    private LocalDateTime dateCreated;      // creation date of this project
    private List<String> existingProjectNames;  // to ensure no duplicate names
    private boolean projectModified;        // whether project has been modified.
    private boolean userImagesAvailable;    // whether the user has loaded any images.

    // Callback variables.
    Consumer<Screen> changeScreen;           // Callback function to change screen.
    Consumer<String> setTitle;              // Callback function to set title of frame.
    Runnable framePack;                     // Callback function to pack frame. Used for resizing.
    Consumer<Project> saveProjectCallback;  // Callback function to save project.

    // UI Elements.
    private BufferedImage addIcon;          // Icon to add new sprites.
    private Rectangle addIconRect;          // A rectangle that represents the position and size of the icon.
    private BufferedImage optionsIcon;      // Icon to open options.
    private Rectangle optionsIconRect;      // A rectangle that represents the position and size of the icon.
    private DetailsPanel detailsPanel;      // contains information on focused sprites.
    private JComboBox<String> spriteSelect;
    private JComboBox<String> optionsSelect;
    private String projectTitle;
    private JTextField projectTitleTextField;
    private JPanel popupPanel;
    private JLabel popupPanelLabel;
    private JTextField popupPanelTextField;
    private JPanel confirmSavePanel;
    private JLabel confirmSaveLabel;
    private JButton confirmSaveButton1;
    private JButton confirmSaveButton2;
    private JPanel imageSelectPanel;
    private JLabel imageSelectLabel;
    private ArrayList<JButton> imageSelectButtons;

    public ProjectEditor(
            Consumer<Screen> changeScreen,
            Consumer<String> setTitle,
            Runnable framePack,
            Consumer<Project> saveProjectCallback,
            ArrayList<String> userImages,
            Project project,
            List<String> existingProjectNames,
            LinkedHashMap<Setting, Integer> settings
        ) {

        // Configuring JPanel
        setPreferredSize(new Dimension(settings.get(Setting.DEFAULT_PROJECT_WIDTH), 
            settings.get(Setting.DEFAULT_PROJECT_LENGTH)));
        setFocusable(true);
        setLayout(null);

        // Adding Listeners
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addKeyListener(keyAdapter);

        // Initializing variables
        this.changeScreen = changeScreen;
        this.setTitle = setTitle;
        this.framePack = framePack;
        this.saveProjectCallback = saveProjectCallback;
        this.existingProjectNames = existingProjectNames;
        userImagesAvailable = userImages.size() > 0;
        
        sprites = new ArrayList<>();
        spriteHeld = false;

        // Creating UI
        detailsPanel = new DetailsPanel();

        spriteSelect = new JComboBox<>(new String[]{"Select", "rectangle", "oval", "polygon", "image"});
        spriteSelect.setActionCommand("add sprite");
        spriteSelect.addActionListener(this);

        optionsSelect = new JComboBox<>(new String[]{"Select", "Set Title", "Resize Canvas", "Save Project", "Export", "Return Home"});
        optionsSelect.setActionCommand("execute option");
        optionsSelect.addActionListener(this);

        popupPanel = new JPanel();
        popupPanel.setBackground(Color.WHITE);
        popupPanelLabel = new JLabel();
        popupPanelTextField = new JTextField();
        popupPanelTextField.addActionListener(this);
        popupPanelTextField.addKeyListener(keyAdapter);
        popupPanel.add(popupPanelLabel);
        popupPanel.add(popupPanelTextField);

        confirmSavePanel = new JPanel();
        confirmSavePanel.setLayout(new BoxLayout(confirmSavePanel, BoxLayout.X_AXIS));
        confirmSaveLabel = new JLabel("Save project?");
        confirmSaveButton1 = new JButton("Save");
        confirmSaveButton1.setActionCommand("confirmSaveYes");
        confirmSaveButton1.addActionListener(this);
        confirmSaveButton2 = new JButton("Don't Save");
        confirmSaveButton2.setActionCommand("confirmSaveNo");
        confirmSaveButton2.addActionListener(this);
        confirmSavePanel.add(confirmSaveLabel);
        JPanel spacer = new JPanel();
        spacer.setSize(10, 10);
        confirmSavePanel.add(spacer);
        confirmSavePanel.add(confirmSaveButton1);
        spacer = new JPanel();
        spacer.setSize(10, 10);
        confirmSavePanel.add(spacer);
        confirmSavePanel.add(confirmSaveButton2);
        confirmSavePanel.setSize(confirmSavePanel.getPreferredSize().width+10, confirmSavePanel.getPreferredSize().height);

        imageSelectPanel = new JPanel();
        imageSelectPanel.setLayout(new BoxLayout(imageSelectPanel, BoxLayout.X_AXIS));
        imageSelectLabel = new JLabel("Select an image:");
        imageSelectPanel.add(imageSelectLabel);
        imageSelectButtons = new ArrayList<>();
        // Add a button for each user image.
        for(String imgName : userImages) {
            JButton button = new JButton(imgName);
            button.setActionCommand("createImage " + imgName);
            button.addActionListener(this);
            imageSelectButtons.add(button);

            spacer = new JPanel();
            spacer.setSize(10, 10);
            imageSelectPanel.add(spacer);
            imageSelectPanel.add(button);
        }
        
        // Initalizing Images
        try {
            addIcon = ImageIO.read(getClass().getResource("/addIcon.png"));
            optionsIcon = ImageIO.read(getClass().getResource("/optionsIcon.png"));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        // Initalize rects for buttons.
        addIconRect = new Rectangle(0, 0, addIcon.getWidth(), addIcon.getHeight());
        optionsIconRect = new Rectangle(0, 0, optionsIcon.getWidth(), optionsIcon.getHeight());
        layoutIcons();

        // Initialize Project Title Text Field which sets the title of the working project.
        projectTitleTextField = new JTextField(projectTitle, 10);
        projectTitleTextField.addActionListener(this);
        projectTitleTextField.setActionCommand("setTitle");


        // Load project, if applicable.
        // a non-null project argument signifies an existing project is to be loaded.
        if(project != null) {
            // Load project.
            setProjectTitle(project.getTitle());
            resizeCanvas(project.getWidth(), project.getHeight());
            sprites = project.getSprites();
            dateCreated = project.getDateCreated();
        }
        else {
            // increment number until unique title is found
            int i = 1;
            while(!setProjectTitle("Untitled " + i)) ++i;
            dateCreated = LocalDateTime.now();
        }

        projectModified = false;
    }


    // Lays out components. Used mainly after resizing of project.
    private void layoutIcons() {
        addIconRect.setLocation(getPreferredSize().width-addIcon.getWidth()-5, 5);
        optionsIconRect.setLocation(addIconRect.x-optionsIcon.getWidth()-5, addIconRect.y);
    }


    // Resizes the canvas to width and height in text field.
    private void resizeCanvas(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        layoutIcons();
        framePack.run();
    }


    //============================================
    //      SHOWING/HIDING PANELS
    //============================================
    // Shows the details panel.
    private void showDetailsPanel(int x, int y) {
        // Update, resize, and relocate.
        detailsPanel.update(focus);
        detailsPanel.scrollPane.setLocation(x, y);

        // Set maximum height for details panel so it doesn't go off the window.
        int height = detailsPanel.scrollPane.getPreferredSize().height;
        if(height > getSize().height) {
            height = getSize().height;
        }
        detailsPanel.scrollPane.setSize(detailsPanel.scrollPane.getPreferredSize().width, height);

        // Add to panel.
        add(detailsPanel.scrollPane);
        detailsPanel.revalidate();
        repaint();
    }


    // Hides the details panel.
    private void hideDetailsPanel() {
        remove(detailsPanel.scrollPane);
        repaint();
    }


    // Shows the sprite selection combo box.
    private void showSpriteSelect() {
        // Configure.
        spriteSelect.setSize(spriteSelect.getPreferredSize());
        spriteSelect.setLocation(getWidth()-spriteSelect.getSize().width-5, addIconRect.y+addIconRect.height+5);

        // Add to panel.
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
        // Configure.
        optionsSelect.setSize(optionsSelect.getPreferredSize());
        optionsSelect.setLocation(getWidth()-optionsSelect.getSize().width-5, addIconRect.y+addIconRect.height+5);
        
        // Add to panel.
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


    // Changes the title of the project. Returns true upon successful change.
    private boolean setProjectTitle(String newTitle) {
        // verify unique title
        if(existingProjectNames.stream().anyMatch(newTitle::equals)) return false;

        projectTitle = newTitle;

        // Change the title of the frame to match new title.
        setTitle.accept("Swing Paint - " + newTitle);

        return true;
    }


    // Shows the popup panel.
    private void showPopupPanel(String labelText, String textFieldCommand, String textFieldValue) {
        // Configure popup panel.
        popupPanelLabel.setText(labelText);
        popupPanelTextField.setColumns(10);
        popupPanelTextField.setActionCommand(textFieldCommand);
        popupPanelTextField.setText(textFieldValue);
        popupPanel.setSize(popupPanel.getPreferredSize());
        // Center the popup panel in frame.
        popupPanel.setLocation(this.getWidth()/2-popupPanel.getWidth()/2, this.getHeight()/2-popupPanel.getHeight()/2);

        // Add to panel.
        add(popupPanel);
        popupPanel.revalidate();
        repaint();

        // Autofocus the text field.
        popupPanelTextField.requestFocusInWindow();
    }


    // Hides the popup panel.
    private void hidePopupPanel() {
        remove(popupPanel);
        repaint();
    }

    
    // Shows the image select panel.
    private void showImageSelectPanel() {
        // Configure.
        imageSelectPanel.setSize(imageSelectPanel.getPreferredSize().width + 10,
            imageSelectPanel.getPreferredSize().height);
        imageSelectPanel.setLocation(getWidth()/2-imageSelectPanel.getWidth()/2,
            getHeight()/2-imageSelectPanel.getHeight()/2);

        // Add to panel.
        add(imageSelectPanel);
        imageSelectPanel.revalidate();
        repaint();
    }


    // Hides the image select panel.
    private void hideImageSelectPanel() {
        remove(imageSelectPanel);
        repaint();
    }


    // Asks user whether they would like to save and calls callback upon answer.
    private void showConfirmSave() {
        confirmSavePanel.setLocation(getWidth()/2-confirmSavePanel.getWidth()/2,
            getHeight()/2-confirmSavePanel.getHeight()/2);
        add(confirmSavePanel);
        confirmSavePanel.revalidate();
    }

    private void hideConfirmSave() {
        remove(confirmSavePanel);
        repaint();
    }
    
    
    // Hides all menus.
    private void hideAllMenus() {
        hideDetailsPanel();
        hideOptions();
        hideSpriteSelect();
        hideImageSelectPanel();
        hidePopupPanel();
        hideConfirmSave();
    }


    
    //============================================
    //      PROJECT HANDLING
    //      Saving/export project.
    //============================================

    // Saves project to be edited in the future.
    private void saveProject() {
        // Create project object with all this projects data and call callback.
        Project project = new Project();
        project.setTitle(projectTitle);
        project.setWidth(getWidth());
        project.setHeight(getHeight());
        project.setSprites(sprites);
        project.setDateCreated(dateCreated);
        project.setDateModified(LocalDateTime.now());

        saveProjectCallback.accept(project);
        projectModified = false;
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
        String sep = System.getProperty("file.separator");
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
            File exportImagesDirectory = new File("export/images");
            if(!exportImagesDirectory.exists()) {
                exportImagesDirectory.mkdir();
            }

            // Export image files to images directory.
            for(JImage jimg : jImageList) {
                File destination = new File("export/images/" + jimg.getImageName());
                try {
                    ImageIO.write(jimg.getImage(), jimg.getImageFileExtension(), destination);
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }


        // Write to export file. This file will have all the java code.
        try(PrintWriter pw = new PrintWriter(new FileWriter("export/Program.java"))) {
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
                    // Check if system uses backslash as separator. If so, must double it.
                    if("\\".equals(System.getProperty("file.separator"))) {
                        pw.printf("\t\t\timgs.add(ImageIO.read(new File(\"images%s%s%s\")));%n", sep, sep, jimg.getImageName());
                    }
                    else {
                        pw.printf("\t\t\timgs.add(ImageIO.read(new File(\"images%s%s\")));%n", sep, jimg.getImageName());
                    }
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



    //============================================
    //      HANDLING SPRITES
    //      Creating/removing/duplicating sprites, manipulating focus, etc.
    //============================================
    // Creates a sprite on the canvas.
    private void createSprite(String type) {
        JSprite newSprite;

        // Create a different sprite depending on the type.
        switch(type) {
            case "rectangle": {
                int width = 20;
                int height = 20;
                int x = getWidth()/2 - width/2;
                int y = getHeight()/2 - height/2;
                newSprite = new JRectangle(x, y, width, height);
                sprites.add(newSprite);
                setFocus(newSprite);
                projectModified = true;
                break;
            }
            case "oval": {
                int width = 20;
                int height = 20;
                int x = getWidth()/2 - width/2;
                int y = getHeight()/2 - height/2;
                newSprite = new JOval(x, y, width, height);
                sprites.add(newSprite);
                setFocus(newSprite);
                projectModified = true;
                break;
            }
            case "polygon": {
                // Show the popup panel and ask user for number of points on polygon.
                showPopupPanel("Number of Points", "createPolygon", "3");
                break;
            }
            case "image": {
                if(userImagesAvailable) {
                    // Show the popup panel and ask user for the path to the image file.
                    showImageSelectPanel();
                }
                else {
                    JOptionPane.showMessageDialog(this,
                        "No images available. Add images to userImages folder.",
                        "No Images", JOptionPane.ERROR_MESSAGE);
                }
                break;
            }
        }

        repaint();
    }



    // Changes the focused sprite.
    private void setFocus(JSprite s) {
        focus = s;
        s.moveDragPoints();
    }


    // Unfocuses sprite.
    private void removeFocus() {
        focus = null;
        repaint();
    }


    // Removes a sprite given its index in the sprites array.
    private void removeSprite(int index) {
        sprites.remove(index);
        hideDetailsPanel();
        removeFocus();
        repaint();
    }


    // Duplicates a sprite given its index in the sprites array.
    private void duplicateSprite(int index) {
        JSprite target = sprites.get(index);
        JSprite s = null;
        if(target instanceof JRectangle) {
            s = new JRectangle((JRectangle)target);
        }
        else if(target instanceof JOval) {
            s = new JOval((JOval)target);
        }
        else if(target instanceof JPolygon) {
            s = new JPolygon((JPolygon)target);
        }
        else if(target instanceof JImage) {
            s = new JImage((JImage)target);
        }
        if(s != null) {
            s.setLocation(s.x+10, s.y+10);
            sprites.add(s);
            setFocus(s);
            hideDetailsPanel();
            repaint();
        }
        else {
            throw new IllegalArgumentException("Invalid sprite index.");
        }
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



    //============================================
    //      HANDLING ACTIONS/INPUTS
    //============================================
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
            else if("Return Home".equals(selection)) {
                if(projectModified) {
                    showConfirmSave();
                }
                else {
                    changeScreen.accept(Screen.HOME);
                }
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
            if(!setProjectTitle(newTitle)) {
                JOptionPane.showMessageDialog(this,
                    "Title of project cannot be identical to an existing project.",
                    "Duplicate Title", JOptionPane.ERROR_MESSAGE);
                return;
            }
            hidePopupPanel();
            projectModified = true;
        }

        // Creates a polygon with specified number of points in popup panel text field.
        else if("createPolygon".equals(e.getActionCommand())) {
            JSprite newSprite = new JPolygon(JPolygon.createDefaultPolygon(Integer.parseInt(popupPanelTextField.getText())));
            newSprite.setLocation(getWidth()/2-newSprite.width/2, getHeight()/2-newSprite.height/2);
            sprites.add(newSprite);
            setFocus(newSprite);
            hidePopupPanel();
            projectModified = true;
        }

        // Creates an image sprite with the specified image path.
        else if("createImage".equals(e.getActionCommand().split(" ")[0])) {
            String imageName = e.getActionCommand().split(" ")[1];
            JImage image = new JImage(JImage.imageFromName(imageName), imageName);
            image.setLocation(getWidth()/2-image.width/2, getHeight()/2-image.height/2);
            sprites.add(image);
            setFocus(image);
            hideImageSelectPanel();
            projectModified = true;
        }

        // Sets the canvas size.
        else if("setCanvasSize".equals(e.getActionCommand())) {
            String input = popupPanelTextField.getText();
            String[] bits = input.split(",");
            try {
                int width = Integer.parseInt(bits[0]);
                int height = Integer.parseInt(bits[1]);
                resizeCanvas(width, height);
            }
            catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(ProjectEditor.this,
                    "Value must be formatted as 'width,height', with no space. Both values must be integers.",
                    "Value Error", JOptionPane.ERROR_MESSAGE);
            }
            hidePopupPanel();
            projectModified = true;
        }

        // Reacts to confirm save response.
        else if("confirmSaveYes".equals(e.getActionCommand())) {
            saveProject();
            changeScreen.accept(Screen.HOME);
        }
        else if("confirmSaveNo".equals(e.getActionCommand())) {
            changeScreen.accept(Screen.HOME);
        }
    }



    // Creating Mouse Adapter to listen for mouse events
    MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1) {
                // Menus should disappear upon a click which is not on the menu.
                hideAllMenus();

                // Initialize variables for later.
                dragPointHeld = -1;
                Point p = e.getPoint();

                // Check if the click was on a button.
                if(addIconRect.contains(p)) {
                    showSpriteSelect();
                    return;
                }
                if(optionsIconRect.contains(p)) {
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
                    removeFocus();
                }

                // Check if the click was on a sprite.
                // Loop through backwards so newest sprites get click priority.
                for(int i = sprites.size()-1; i >= 0; i--) {
                    if(sprites.get(i).contains(p)) {
                        setFocus(sprites.get(i));
                        spriteHeld = true;
                        dx = (int)p.getX() - (int)focus.getX();
                        dy = (int)p.getY() - (int)focus.getY();
                        return;
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
                projectModified = true;
                focus.handleDragPoint(dragPointHeld, p);
                focus.moveDragPoints();
                repaint();
            }

            // Handle sprite click-and-drag movement.
            else if(spriteHeld) {
                projectModified = true;
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
                dragPointHeld = -1;
            }
        }
    };
    

    // Creating Key Adapter to listen for mouse events
    KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE: {
                    System.out.println("fasld");
                    hideAllMenus();
                    removeFocus();
                    break;
                }
                
                case KeyEvent.VK_BACK_SPACE: {
                    if(focus != null) {
                        int spriteIndex = sprites.indexOf(focus);
                        if(spriteIndex != -1) {
                            removeSprite(spriteIndex);
                        }
                    }
                    break;
                }
            }
        }
    };



    //============================================
    //      DETAILS PANEL
    //============================================
    // The details panel allows the user to view and edit attributes of a focused sprite.
    private class DetailsPanel extends JPanel implements ActionListener {
        private ArrayList<AttributeRow> attributeRows;    // Each row is assigned an attribute.
        private JScrollPane scrollPane;

        public DetailsPanel() {
            attributeRows = new ArrayList<>();
            scrollPane = new JScrollPane(this);

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
            projectModified = true;
            // Split event.
            String[] bits = e.getActionCommand().split(" ");

            // If first word is "set", check the next word and set the specified attribute.
            if("set".equals(bits[0])) {
                switch(bits[1]) {
                    case "width":
                        try {
                            focus.setSize(Integer.parseInt(searchRowByAttribute("width").textField.getText()), (int)focus.getHeight());
                            ProjectEditor.this.repaint();
                        }
                        catch(NumberFormatException ex) {
                            JOptionPane.showMessageDialog(ProjectEditor.this, "Value must be an integer.", "Value Error", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "height":
                        try {
                            focus.setSize((int)focus.getWidth(), Integer.parseInt(searchRowByAttribute("height").textField.getText()));
                            ProjectEditor.this.repaint();
                        }
                        catch(NumberFormatException ex) {
                            JOptionPane.showMessageDialog(ProjectEditor.this, "Value must be an integer.", "Value Error", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "x":
                        try {
                            focus.setLocation(Integer.parseInt(searchRowByAttribute("x").textField.getText()), (int)focus.getY());
                            ProjectEditor.this.repaint();
                        }
                        catch(NumberFormatException ex) {
                            JOptionPane.showMessageDialog(ProjectEditor.this, "Value must be an integer.", "Value Error", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "y":
                        try {
                            focus.setLocation((int)focus.getX(), Integer.parseInt(searchRowByAttribute("y").textField.getText()));
                            ProjectEditor.this.repaint();
                        }
                        catch(NumberFormatException ex) {
                            JOptionPane.showMessageDialog(ProjectEditor.this, "Value must be an integer.", "Value Error", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "color":
                        String rgbString = searchRowByAttribute("color").textField.getText();
                        try {
                            focus.setRGBString(rgbString);
                        }
                        catch(IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(ProjectEditor.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        ProjectEditor.this.repaint();
                        break;
                    case "point":
                        JPolygon polygon = (JPolygon)focus;
                        int pointIndex = Integer.parseInt(bits[2]) - 1;
                        if("x".equals(bits[3])) {
                            try {
                                int newX = Integer.parseInt(searchRowByAttribute(String.format("%s %s %s", bits[1], bits[2], bits[3])).textField.getText());
                                polygon.movePoint(pointIndex, newX, polygon.getPolygon().ypoints[pointIndex]);
                                ProjectEditor.this.repaint();
                            }
                            catch(NumberFormatException ex) {
                                JOptionPane.showMessageDialog(ProjectEditor.this, "Value must be an integer.", "Value Error", JOptionPane.ERROR_MESSAGE);
                            }
                            break;
                        }
                        else if("y".equals(bits[3])) {
                            try {
                                int newY = Integer.parseInt(searchRowByAttribute(String.format("%s %s %s", bits[1], bits[2], bits[3])).textField.getText());
                                polygon.movePoint(pointIndex, polygon.getPolygon().xpoints[pointIndex], newY);
                                ProjectEditor.this.repaint();
                            }
                            catch(NumberFormatException ex) {
                                JOptionPane.showMessageDialog(ProjectEditor.this, "Value must be an integer.", "Value Error", JOptionPane.ERROR_MESSAGE);
                            }
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

            // Duplicate focused sprite.
            else if("duplicate".equals(bits[0])) {
                int i = sprites.indexOf(focus);
                if(i != -1) {
                    try {
                        ProjectEditor.this.duplicateSprite(i);
                    }
                    catch(IllegalArgumentException ex) {
                        ex.printStackTrace();
                    }
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
            private JButton duplicateButton;
            private JButton deleteButton;

            public ButtonRow() {
                // Configure.
                setBackground(Color.GRAY);

                // Create UI.
                duplicateButton = new JButton("Duplicate");
                duplicateButton.setActionCommand("duplicate");
                duplicateButton.addActionListener(detailsPanel);

                deleteButton = new JButton("Delete");
                deleteButton.setActionCommand("delete");
                deleteButton.addActionListener(detailsPanel);
                
                add(duplicateButton);
                add(deleteButton);
            }
        }
        
    }
}
