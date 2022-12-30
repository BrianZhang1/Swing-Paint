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


    // Changes the x and y coordinates of each point of the polygon to x and y respectively.
    public void setXY(int x, int y) {
        polygon.translate(x - this.x, y - this.y);
    }

    // Sets location of Rectangle and Polygon in one method.
    @Override
    public void setLocation(int x, int y) {
        setXY(x, y);
        super.setLocation(x, y);
    }
}
