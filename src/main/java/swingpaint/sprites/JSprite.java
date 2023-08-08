package swingpaint.sprites;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;


// Base class for all sprites.
public class JSprite {
    protected Rectangle bounds;
    protected ArrayList<String> attributes;
    protected Point[] corners;              // each point represents a corner of this sprite
                                            // point of the sprite. Used for resizing sprites with mouse.
    protected Color color;
    protected String type;


    // Construct from individual dimensions.
    public JSprite(int x, int y, int width, int height) {
        bounds = new Rectangle(x, y, width, height);
        init();
    }


    // Construct from rectangle.
    public JSprite(Rectangle r) {
        bounds = new Rectangle(r);
        init();
    }


    // Clone constructor.
    public JSprite(JSprite s) {
        bounds = new Rectangle(s.bounds);

        // Clone attributes.
        attributes = new ArrayList<>();
        for(String attribute : s.attributes) {
            attributes.add(attribute);
        }

        // Clone dragPoints.
        corners = new Point[s.corners.length];
        for(int i = 0; i < s.corners.length; i++) {
            corners[i] = new Point(s.corners[i]);
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
        corners = new Point[4];
        for(int i = 0; i < corners.length; i++) {
            corners[i] = new Point();
        }
        updateCorners();
    }

    // Moves the corners to the corners of this sprite.
    public void updateCorners() {
        corners[0].setLocation(bounds.x, bounds.y); // top left
        corners[1].setLocation(bounds.x+bounds.width, bounds.y); // top right
        corners[2].setLocation(bounds.x, bounds.y+bounds.height); // bottom left
        corners[3].setLocation(bounds.x+bounds.width, bounds.y+bounds.height); // bottom right
    }


    public void setSize(int width, int height) {
        bounds.setSize(width, height);
        updateCorners();
    }


    public void setLocation(int x, int y) {
        bounds.setLocation(x, y);
        updateCorners();
    }


    // Getters and setters.
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

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

    public Point[] getCorners() {
        return corners;
    }

}
