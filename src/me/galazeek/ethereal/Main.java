package me.galazeek.ethereal;

import com.formdev.flatlaf.FlatDarculaLaf;
import me.galazeek.ethereal.classes.ClassesManager;
import me.galazeek.ethereal.gui.PrototypeMenu;

import javax.swing.*;

public class Main {
    /*
    todo ChatGPT implementation
     */
    public static void main(String[] args) {
        //Better look and feel
        FlatDarculaLaf.setup();

        //Load classes
        ClassesManager.getInstance();

        SwingUtilities.invokeLater(() -> {
            //GUI
            PrototypeMenu menu = new PrototypeMenu();

            menu.load();
            menu.display();
        });
    }
}