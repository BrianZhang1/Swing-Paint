package swingpaint.sprites;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Point;

public class JPolygon extends JSprite {
    private Polygon polygon;

    public JPolygon(Polygon polygon) {
        super(polygon.getBounds());
        type = "polygon";

        // Since polygons may not have four corners, dragPoints must adapt to n.
        dragPoints = new Rectangle[polygon.npoints];
        for(int i = 0; i < dragPoints.length; i++) {
            dragPoints[i] = new Rectangle(0, 0, CORNER_LENGTH, CORNER_LENGTH);
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

    // Moves the specified point to the specified location.
    public void movePoint(int index, int x, int y) {
        polygon.xpoints[index] = x;
        polygon.ypoints[index] = y;
        calculateBounds(polygon.xpoints, polygon.ypoints, polygon.npoints);
    }

    @Override
    // Override JSprite method.
    // Moves the drag points. Called upon changing focused sprite.
    public void moveDragPoints() {
        int l = CORNER_LENGTH;
        for(int i = 0; i < polygon.npoints; i++) {
            int px = polygon.xpoints[i];
            int py = polygon.ypoints[i];
            dragPoints[i].setLocation(px-l/2, py-l/2);
        }
    }

    // Override JSprite method.
    // Handles resizing of sprite by drag points.
    @Override
    public void handleDragPoint(int dragPointHeld, Point p) {
        movePoint(dragPointHeld, p.x, p.y);
    }

    /*
     * The source code for the contains() method java.awt.Polygon class
     * has an issue relating to bounds (https://bugs.openjdk.org/browse/JDK-4269933)
     * Copy and rewrite sections of the source code to adapt the contains() method
     * and the calculateBounds() method to accomodate the requirements of this program.
     */
    @Override
    public boolean contains(Point p) {
        return contains((double)p.x, (double)p.y);
    }
    @Override
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
        Rectangle bounds = new Rectangle(boundsMinX, boundsMinY,
                               boundsMaxX - boundsMinX,
                               boundsMaxY - boundsMinY);
        
        // appended changes to update bounds.
        x = bounds.x;
        y = bounds.y;
        width = bounds.width;
        height = bounds.height;
    }
}
