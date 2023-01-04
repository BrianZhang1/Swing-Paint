package swingpaint.sprites;

import java.awt.Rectangle;

public class JOval extends JRectangle {
    public JOval(int x, int y, int width, int height) {
        super(x, y, width, height);
        type = "oval";
    }

    public JOval(Rectangle r) {
        super(r);
        type = "oval";
    }
}
