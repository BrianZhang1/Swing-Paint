package swingpaint.sprites;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


// Image sprite.
public class JImage extends JSprite {
    private BufferedImage originalImage;    // Store the original image for resizing purposes.
    private BufferedImage image;            // The current image being drawn.
    private String imageName;               // Stored for saving purposes.


    // Construct from image.
    public JImage(BufferedImage image, String imageName) {
        super(0, 0, image.getWidth(), image.getHeight());
        
        type = "image";
        this.originalImage = image;
        this.image = originalImage;
        this.imageName = imageName;
    }


    // Construct from image and dimensions.
    public JImage(BufferedImage image, String imageName, int x, int y, int width, int height) {
        super(x, y, width, height);
        
        type = "image";
        this.originalImage = image;
        this.image = originalImage;
        this.imageName = imageName;

        fitImage();
    }


    // Clone constructor.
    public JImage(JImage jimg) {
        super(jimg);
        originalImage = JImage.copyImage(jimg.originalImage);
        image = JImage.copyImage(jimg.image);
        imageName = jimg.imageName;
    }


    // Returns a BufferedImage given an image path.
    public static BufferedImage imageFromPath(String imagePath) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(imagePath));
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return image;
    }


    // Returns a BufferedImage given an image name, assuming it is in the appropriate directory.
    public static BufferedImage imageFromName(String imageName) {
        return imageFromPath("userImages/" + imageName);
    }


    // Returns a copy of given BufferedImage.
    public static BufferedImage copyImage(BufferedImage img){
        BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g = newImg.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return newImg;
    }


    // Images must resize when drag points are moved.
    @Override
    public void handleDragPoint(int dragPointHeld, Point p) {
        super.handleDragPoint(dragPointHeld, p);
        fitImage();
    }
    
    
    // setSize should refit image.
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        fitImage();
    }


    // Fits the image to the bounds of this sprite's rectangle.
    private void fitImage() {
        image = resize(originalImage, width, height);
    }


    // Returns a resized BufferedImage.
    private BufferedImage resize(BufferedImage image, int width, int height) {
        Image temp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(width, height, image.getType());

        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();

        return newImage;
    }


    // Returns this sprite's image's file extension (e.g., jpg, png, etc.).
    public String getImageFileExtension() {
        return imageName.substring(imageName.length()-3, imageName.length());
    }


    // Provides string representation of this sprite.
    @Override
    public String toString() {
        return String.format("type=%s;x=%d;y=%d;width=%d;height=%d;imageName=%s",
            type, x, y, width, height, imageName);
    }


    // Getters and setters.
    public BufferedImage getImage() {
        return image;
    }

    public String getImageName() {
        return imageName;
    }
}
