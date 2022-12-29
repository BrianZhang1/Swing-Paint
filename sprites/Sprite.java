package sprites;
// Base class for all sprites.

import java.awt.Rectangle;
import java.awt.Color;
import java.util.ArrayList;

public class Sprite extends Rectangle {
    protected ArrayList<String> attributes;
    protected Color color;
    protected String type;

    public Sprite(int x, int y, int width, int height) {
        super(x, y, width, height);
        attributes = new ArrayList<>();
        attributes.add("x");
        attributes.add("y");
        attributes.add("width");
        attributes.add("height");
        attributes.add("color");

        color = Color.BLACK;
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

}
