package me.galazeek.ethereal.gui.comp;

import me.galazeek.ethereal.gui.listener.MenuItemClickListener;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Menu extends JMenu {

    private List<MenuItemClickListener> listeners;

    public Menu() {
        super();
    }

    public Menu(String s) {
        super(s);
    }

    private void initListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                listeners.forEach(MenuItemClickListener::click);
            }
        });
    }

    public void addClickListener(MenuItemClickListener listener) {
        if(listeners == null) listeners = new ArrayList<>();
        listeners.add(listener);
    }

}
