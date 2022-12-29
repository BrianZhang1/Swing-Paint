package sprites;

import java.awt.Polygon;
import java.awt.Point;

public class JPolygon extends JSprite {
    private Polygon polygon;

    public JPolygon(int x, int y, int width, int height) {
        super(x, y, width, height);
        type = "polygon";

        polygon = new Polygon(new int[]{0, 10, 5}, new int[]{0, 0, 10}, 3);
    }

    @Override
    public boolean contains(Point p) {
        return polygon.contains(p);
    }

    public Polygon getPolygon() {
        return polygon;
    }
}
