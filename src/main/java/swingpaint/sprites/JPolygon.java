package swingpaint.sprites;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

public class JPolygon extends JSprite {
    private Polygon polygon;


    // Construct from polygon.
    public JPolygon(Polygon polygon) {
        super(polygon.getBounds());
        type = "polygon";

        // Since polygons may not have four corners, dragPoints must adapt to n.
        corners = new Point[polygon.npoints];
        for(int i = 0; i < corners.length; i++) {
            corners[i] = new Point();
        }

        // Different attribute list is needed for polygons
        attributes.clear();
        attributes.add("color");
        for(int i = 0; i < polygon.npoints; i++) {
            attributes.add(String.format("point %d x", i+1));
            attributes.add(String.format("point %d y", i+1));
        }

        this.polygon = polygon;
    }


    // Clone constructor.
    public JPolygon(JPolygon jpolygon) {
        super(jpolygon);
        polygon = new Polygon();
        polygon.xpoints = new int[jpolygon.polygon.npoints];
        polygon.ypoints = new int[jpolygon.polygon.npoints];
        for(int i = 0; i < jpolygon.polygon.npoints; i++) {
            polygon.xpoints[i] = jpolygon.polygon.xpoints[i];
            polygon.ypoints[i] = jpolygon.polygon.ypoints[i];
        }
        polygon.npoints = jpolygon.polygon.npoints;
    }


    // Moves the specified point to the specified location.
    public void movePoint(int index, int x, int y) {
        polygon.xpoints[index] = x;
        polygon.ypoints[index] = y;
        calculateBounds(polygon.xpoints, polygon.ypoints, polygon.npoints);
    }


    // Provides string representation of this sprite.
    @Override
    public String toString() {
        String xpointsString = "[";
        for(int x : polygon.xpoints) {
            xpointsString += Integer.toString(x) + ",";
        }
        xpointsString = xpointsString.substring(0, xpointsString.length()-1);
        xpointsString += "]";

        String ypointsString = "[";
        for(int y : polygon.ypoints) {
            ypointsString += Integer.toString(y) + ",";
        }
        ypointsString = ypointsString.substring(0, ypointsString.length()-1);
        ypointsString += "]";

        return String.format("type=%s;xpoints=%s;ypoints=%s;color=%s",
            type, xpointsString, ypointsString, getRGBString());
    }


    // Creates a Polygon from the given String.
    // Also see the toString() method.
    public static Polygon polygonFromString(String[] data) {
        String xpointsString, ypointsString;
        int[] xpoints, ypoints;
        String[] bits;

        xpointsString = data[1];
        ypointsString = data[2];

        // Value is on the right of the equals sign. Isolate value.
        xpointsString = xpointsString.split("=")[1];
        ypointsString = ypointsString.split("=")[1];
        // Strip the first and last character (square brackets).
        xpointsString = xpointsString.substring(1, xpointsString.length()-1);
        ypointsString = ypointsString.substring(1, ypointsString.length()-1);

        // Load values of xpointsString into xpoints.
        bits = xpointsString.split(",");
        xpoints = new int[bits.length];
        for(int i = 0; i < xpoints.length; i++) {
            xpoints[i] = Integer.parseInt(bits[i]);
        }

        // Load values of ypointsString into ypoints.
        bits = ypointsString.split(",");
        ypoints = new int[bits.length];
        for(int i = 0; i < ypoints.length; i++) {
            ypoints[i] = Integer.parseInt(bits[i]);
        }

        return new Polygon(xpoints, ypoints, xpoints.length);
    }


    // Creates a polygon with n points.
    public static Polygon createDefaultPolygon(int n) {
        int[] xpoints = new int[n];
        int[] ypoints = new int[n];
        for(int i = 0; i < (n+1)/2; i++) {
            xpoints[i] = i*20;
            ypoints[i] = (i%2)*5;
        }
        for(int i = 0; i < n/2; i++) {
            xpoints[i+(n+1)/2] = xpoints[(n+1)/2 - i - 1];
            ypoints[i+(n+1)/2] = (i%2)*5 + 20;
        }

        return new Polygon(xpoints, ypoints, n);
    }


    @Override
    public void updateCorners() {
        // empty, since JPolygons maintain their corners in the polygon field.
    }


    /*
     * The source code for the contains() method java.awt.Polygon class
     * has an issue relating to bounds (https://bugs.openjdk.org/browse/JDK-4269933)
     * Copy and rewrite sections of the source code to adapt the contains() method
     * and the calculateBounds() method to accomodate the requirements of this program.
     */
    public boolean contains(Point p) {
        return contains((double)p.x, (double)p.y);
    }
    
    public boolean contains(double x, double y) {
        // Change in the line below (!polygon.getBoundingBox() -> !this.getBounds())
        if (polygon.npoints <= 2 || !getBounds().contains(x, y)) {
            return false;
        }
        int hits = 0;

        int lastx = polygon.xpoints[polygon.npoints - 1];
        int lasty = polygon.ypoints[polygon.npoints - 1];
        int curx, cury;

        // Walk the edges of the polygon
        for (int i = 0; i < polygon.npoints; lastx = curx, lasty = cury, i++) {
            curx = polygon.xpoints[i];
            cury = polygon.ypoints[i];

            if (cury == lasty) {
                continue;
            }

            int leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }

            double test1, test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }

            if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }

        return ((hits & 1) != 0);
    }


    // Calculates bounds of polygon as a Rectangle.
    void calculateBounds(int[] xpoints, int[] ypoints, int npoints) {
        int boundsMinX = Integer.MAX_VALUE;
        int boundsMinY = Integer.MAX_VALUE;
        int boundsMaxX = Integer.MIN_VALUE;
        int boundsMaxY = Integer.MIN_VALUE;

        for (int i = 0; i < npoints; i++) {
            int x = xpoints[i];
            boundsMinX = Math.min(boundsMinX, x);
            boundsMaxX = Math.max(boundsMaxX, x);
            int y = ypoints[i];
            boundsMinY = Math.min(boundsMinY, y);
            boundsMaxY = Math.max(boundsMaxY, y);
        }
        bounds = new Rectangle(boundsMinX, boundsMinY,
                               boundsMaxX - boundsMinX,
                               boundsMaxY - boundsMinY);
    }


    // Getters and setters.
    public Polygon getPolygon() {
        return polygon;
    }


    // Changes the x and y coordinates of each point of the polygon to x and y respectively.
    public void setXY(int x, int y) {
        polygon.translate(x - bounds.x, y - bounds.y);
    }


    // Sets location of Rectangle and Polygon in one method.
    @Override
    public void setLocation(int x, int y) {
        setXY(x, y);
        super.setLocation(x, y);
    }


    @Override
    public Point[] getCorners() {
        Point[] corners = new Point[polygon.npoints];
        for(int i = 0; i < polygon.npoints; ++i) {
            corners[i] = new Point(polygon.xpoints[i], polygon.ypoints[i]);
        }
        return corners;
    }
}
