package swingpaint.helpers;

import swingpaint.sprites.JSprite;
import java.util.ArrayList;
import swingpaint.sprites.JOval;
import swingpaint.sprites.JPolygon;
import swingpaint.sprites.JImage;
import swingpaint.sprites.JRectangle;

// A Project contains the data of a canvas created by the user.
public class Project {
    private String title;       // title of project.
    private int width, height;  // dimensions of project.
    private ArrayList<JSprite> sprites;  // contains all sprites in project.


    // Constructor to initialize attributes.
    public Project() {
        sprites = new ArrayList<>();
    }


    // Constructor to clone.
    public Project(Project project) {
        sprites = new ArrayList<>();

        title = project.title;
        width = project.width;
        height = project.height;

        // Copy the sprites.
        for(JSprite sprite : project.sprites) {
            switch(sprite.getType()) {
                case "rectangle": {
                    sprites.add(new JRectangle((JRectangle)sprite));
                    break;
                }
                case "oval": {
                    sprites.add(new JOval((JOval)sprite));
                    break;
                }
                case "polygon": {
                    sprites.add(new JPolygon((JPolygon)sprite));
                    break;
                }
                case "image": {
                    sprites.add(new JImage((JImage)sprite));
                    break;
                }
            }
        }
    }


    // Getters and Setters for all attributes. ---------------

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ArrayList<JSprite> getSprites() {
        return sprites;
    }

    public void setSprites(ArrayList<JSprite> sprites) {
        this.sprites = sprites;
    }

    public void addSprite(JSprite sprite) {
        this.sprites.add(sprite);
    }
}
