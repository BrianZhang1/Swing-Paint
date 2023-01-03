package swingpaint.sprites;

import java.awt.Rectangle;

public class JRectangle extends JSprite {
    public JRectangle(int x, int y, int width, int height) {
        super(x, y, width, height);
        type = "rectangle";
    }

    public JRectangle(Rectangle r) {
        super(r);
        type = "rectangle";
    }

    // Provides string representation of this sprite.
    @Override
    public String toString() {
        return String.format("type=%s;x=%d;y=%d;width=%d;height=%d", type, x, y, width, height);
    }

    // Creates a Rectangle from the given String.
    // Also see the toString() method.
    public static Rectangle rectangleFromString(String[] data) {
        int x, y, width, height;
        x = Integer.parseInt(data[1].split("=")[1]);
        y = Integer.parseInt(data[2].split("=")[1]);
        width = Integer.parseInt(data[3].split("=")[1]);
        height = Integer.parseInt(data[4].split("=")[1]);

        return new Rectangle(x, y, width, height);
    }
}
