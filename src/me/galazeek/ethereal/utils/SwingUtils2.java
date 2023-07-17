package me.galazeek.ethereal.utils;

import javax.swing.*;
import java.awt.*;

public class SwingUtils2 {

    public static Component CreateBox(int width, int height) {
        return Box.createRigidArea(new Dimension(width, height));
    }

    public static Component HorizontalBox(JComponent...components) {
        Box box = Box.createHorizontalBox();
        for (JComponent component : components) {
            box.add(component);
        }
        return box;
    }

    public static Component Separator(Container container) {
        return CreateBox(container.getWidth(), 0);
    }

    public static Component MaxSeparator() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return CreateBox(screenSize.width, 0);
    }

}
