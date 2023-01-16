package swingpaint.sprites;

import java.awt.Rectangle;


// Oval sprite.
public class JOval extends JRectangle {

    // Construct from individual dimensions.
    public JOval(int x, int y, int width, int height) {
        super(x, y, width, height);
        type = "oval";
    }


    // Construct from rectangle.
    public JOval(Rectangle r) {
        super(r);
        type = "oval";
    }


    // Clone constructor.
    public JOval(JOval joval) {
        super(joval);
    }
}
