package swingpaint.sprites;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.File;
import java.awt.Point;

public class JImage extends JSprite {
    private BufferedImage originalImage;    // Store the original image for resizing purposes.
    private BufferedImage image;            // The current image being drawn.


    public JImage(BufferedImage image) {
        super(0, 0, image.getWidth(), image.getHeight());
        
        type = "image";
        this.originalImage = image;
        this.image = originalImage;
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


    @Override
    public void handleDragPoint(int dragPointHeld, Point p) {
        super.handleDragPoint(dragPointHeld, p);
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


    // Returns this sprite's image.
    public BufferedImage getImage() {
        return image;
    }
}
