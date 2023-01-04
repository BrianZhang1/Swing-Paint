package swingpaint.sprites;

import java.awt.Rectangle;

public class JOval extends JSprite {
    public JOval(int x, int y, int width, int height) {
        super(x, y, width, height);
        type = "oval";
    }

    public JOval(Rectangle r) {
        super(r);
        type = "oval";
    }

    // Provides string representation of this sprite.
    @Override
    public String toString() {
        return String.format("type=%s;x=%d;y=%d;width=%d;height=%d;color=%s",
            type, x, y, width, height, getRGBString());
    }
}
