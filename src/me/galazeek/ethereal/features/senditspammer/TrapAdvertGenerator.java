package me.galazeek.ethereal.features.senditspammer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TrapAdvertGenerator {

    public static void main(String[] args) { new TrapAdvertGenerator(); }

    public TrapAdvertGenerator() {
        final int width = 1125, height = 2436;

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();

        g.drawString("Look! Text!", 150, 150);

        g.dispose();

        try {
            ImageIO.write(bi, "png", new File("C:\\Users\\tylch\\OneDrive\\Desktop\\trap.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
