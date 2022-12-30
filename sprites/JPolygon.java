package sprites;

import java.awt.Polygon;
import java.awt.Point;

public class JPolygon extends JSprite {
    private Polygon polygon;

    public JPolygon(Polygon polygon) {
        super(polygon.getBounds());
        type = "polygon";

        this.polygon = polygon;
    }

    @Override
    public boolean contains(Point p) {
        return polygon.contains(p);
    }

    public Polygon getPolygon() {
        return polygon;
    }

    // Changes the x coordinate of each point of the polygon by int dx.
    public void moveX(int dx) {
        for(int i = 0; i < polygon.xpoints.length; i++) {
            polygon.xpoints[i] += dx;
        }
    }

    // Changes the x coordinate of each point of the polygon by int dx.
    public void moveY(int dy) {
        for(int i = 0; i < polygon.ypoints.length; i++) {
            polygon.ypoints[i] += dy;
        }
    }

    // Changes the x and y coordinates of each point of the polygon by dx and dy respectively.
    public void moveXY(int dx, int dy) {
        moveX(dx);
        moveY(dy);
    }
}
