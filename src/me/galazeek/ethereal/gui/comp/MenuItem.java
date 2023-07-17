package me.galazeek.ethereal.gui.comp;

import me.galazeek.ethereal.gui.listener.MenuItemClickListener;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MenuItem extends JMenuItem {

    private List<MenuItemClickListener> listeners;

    public MenuItem() {
        initListener();
    }
    public MenuItem(Icon icon) {
        super(icon);
        initListener();
    }
    public MenuItem(String text) {
        super(text);
        initListener();
    }
    public MenuItem(Action a) {
        super(a);
        initListener();
    }
    public MenuItem(String text, Icon icon) {
        super(text, icon);
        initListener();
    }
    public MenuItem(String text, int mnemonic) {
        super(text, mnemonic);
        initListener();
    }

    private void initListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(listeners == null) return;
                listeners.forEach(MenuItemClickListener::click);
            }
        });
    }

    public void addClickListener(MenuItemClickListener listener) {
        if(listeners == null) listeners = new ArrayList<>();
        listeners.add(listener);
    }

}
