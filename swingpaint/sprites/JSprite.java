package swingpaint.sprites;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;


// Base class for all sprites.
public class JSprite extends Rectangle {
    protected ArrayList<String> attributes;
    protected Rectangle[] dragPoints;       // an array of rectangles; each represent one handle
                                            // point of the sprite. Used for resizing sprites with mouse.
    final protected int CORNER_LENGTH = 5;    // length of each corner rectangle.
    protected Color color;
    protected String type;


    // Construct from individual dimensions.
    public JSprite(int x, int y, int width, int height) {
        super(x, y, width, height);
        init();
    }


    // Construct from rectangle.
    public JSprite(Rectangle r) {
        super(r);
        init();
    }


    // Clone constructor.
    public JSprite(JSprite s) {
        super(s.x, s.y, s.width, s.height);

        // Clone attributes.
        attributes = new ArrayList<>();
        for(String attribute : s.attributes) {
            attributes.add(attribute);
        }

        // Clone dragPoints.
        dragPoints = new Rectangle[s.dragPoints.length];
        for(int i = 0; i < s.dragPoints.length; i++) {
            dragPoints[i] = new Rectangle(s.dragPoints[i]);
        }

        // Clone color.
        color = new Color(s.color.getRed(), s.color.getGreen(), s.color.getBlue());
        
        // Clone type.
        type = s.type;

    }


    // Initialize JSprite.
    private void init() {
        // Adding default attributes.
        attributes = new ArrayList<>();
        attributes.add("x");
        attributes.add("y");
        attributes.add("width");
        attributes.add("height");
        attributes.add("color");

        color = Color.BLACK;

        // Initializing drag points.
        dragPoints = new Rectangle[4];
        for(int i = 0; i < dragPoints.length; i++) {
            dragPoints[i] = new Rectangle(0, 0, CORNER_LENGTH, CORNER_LENGTH);
        }
    }


    // Moves the drag points. Called upon changing focused sprite.
    public void moveDragPoints() {
        int l = CORNER_LENGTH;
        dragPoints[0].setLocation(x-l/2, y-l/2); // top left
        dragPoints[1].setLocation(x+width-l/2, y-l/2); // top right
        dragPoints[2].setLocation(x-l/2, y+height-l/2); // bottom left
        dragPoints[3].setLocation(x+width-l/2, y+height-l/2); // bottom right
    }


    // Handles resizing of sprite by drag points.
    public void handleDragPoint(int dragPointHeld, Point p) {
        if(dragPointHeld == 0) {
            int diffX = x - p.x;
            width += diffX;
            x -= diffX;

            int diffY = y - p.y;
            height += diffY;
            y -= diffY;
        }
        else if(dragPointHeld == 1) {
            width = p.x - x;

            int diffY = y - p.y;
            height += diffY;
            y -= diffY;
        }
        else if(dragPointHeld == 2) {
            int diffX = x - p.x;
            width += diffX;
            x -= diffX;

            height = p.y - y;
        }
        else if(dragPointHeld == 3) {
            width = p.x - x;
            height = p.y - y;
        }
    }


    // Drag points should adjust to resizes.
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        moveDragPoints();
    }


    // Drag points should adjust to new location.
    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        moveDragPoints();
    }


    // Getters and setters.
    public String getRGBString() {
        return String.format("%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue());
    }

    public void setRGBString(String rgbString) throws IllegalArgumentException {
        String[] bits = rgbString.split(",");
        try {
            color = new Color(Integer.parseInt(bits[0]), Integer.parseInt(bits[1]), Integer.parseInt(bits[2]));
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("Improper color formatting. Color text must be formatted as r,g,b with no spaces.");
        }
        catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Color value out of bounds. Must be within the range 0-255.");
        }
    }

    public String getType() {
        return type;
    }

    public ArrayList<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<String> attributes) {
        this.attributes = attributes;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Rectangle[] getDragPoints() {
        return dragPoints;
    }

}
