package swingpaint.sprites;

public class JRectangle extends JSprite {
    public JRectangle(int x, int y, int width, int height) {
        super(x, y, width, height);
        type = "rectangle";
    }

    // Provides string representation of this sprite.
    @Override
    public String toString() {
        return String.format("type=%s;x=%d;y=%d;width=%d;height=%d", type, x, y, width, height);
    }
}
