package swingpaint.sprites;

public class JOval extends JSprite {
    public JOval(int x, int y, int width, int height) {
        super(x, y, width, height);
        type = "oval";
    }

    // Provides string representation of this sprite.
    @Override
    public String toString() {
        return String.format("type=%s;x=%d;y=%d;width=%d;height=%d", type, x, y, width, height);
    }
}
