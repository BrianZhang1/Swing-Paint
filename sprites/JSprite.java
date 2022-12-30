package sprites;
// Base class for all sprites.

import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Color;
import java.util.ArrayList;

public class JSprite extends Rectangle {
    protected ArrayList<String> attributes;
    protected Rectangle[] dragPoints;       // an array of rectangles; each represent one handle
                                            // point of the sprite. Used for resizing sprites with mouse.
    final private int CORNER_LENGTH = 5;    // length of each corner rectangle.
    protected Color color;
    protected String type;

    public JSprite(int x, int y, int width, int height) {
        super(x, y, width, height);
        init();
    }

    public JSprite(Rectangle r) {
        super(r);
        init();
    }

    private void init() {
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

    public String getRGBString() {
        return String.format("%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue());
    }

    public void setRGBString(int r, int g, int b) {
        color = new Color(r, g, b);
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
