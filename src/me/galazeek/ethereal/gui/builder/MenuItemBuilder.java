package me.galazeek.ethereal.gui.builder;

import me.galazeek.ethereal.gui.action.MenuItemRunnable;
import me.galazeek.ethereal.gui.action.MenuRunnable;
import me.galazeek.ethereal.gui.comp.Menu;
import me.galazeek.ethereal.gui.comp.MenuItem;
import me.galazeek.ethereal.gui.listener.MenuItemClickListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MenuItemBuilder {

    private String txt;
    private ImageIcon icon;
    private int mnemonic = -1;
    private List<JComponent> children;
    private MenuItemRunnable miRunnable;
    private MenuRunnable mRunnable;
    private boolean disabled;

    private List<MenuItemClickListener> listeners;

    public MenuItemBuilder() {}

    public MenuItemBuilder(String txt) {
        this.txt = txt;
    }
    public MenuItemBuilder(String txt, ImageIcon icon) {
        this.txt = txt;
        this.icon = icon;
    }
    public MenuItemBuilder(String txt, ImageIcon icon, int mnemonic) {
        this.txt = txt;
        this.icon = icon;
        this.mnemonic = mnemonic;
    }

    public MenuItemBuilder withMenuItem(MenuItem menuItem) {
        if(children == null) children = new ArrayList<>();
        children.add(menuItem);
        return this;
    }
    public MenuItemBuilder withSeparator() {
        children.add(new JSeparator());
        return this;
    }
    public MenuItemBuilder withMenuItemRunnable(MenuItemRunnable runnable) {
        this.miRunnable = runnable;
        return this;
    }
    public MenuItemBuilder withMenuRunnable(MenuRunnable runnable) {
        this.mRunnable = runnable;
        return this;
    }
    public MenuItemBuilder isDisabled(boolean b) {
        this.disabled = b;
        return this;
    }

    public MenuItemBuilder withText(String txt) {
        this.txt = txt;
        return this;
    }
    public MenuItemBuilder withIcon(ImageIcon icon) {
        this.icon = icon;
        return this;
    }
    public MenuItemBuilder withMnemonic(int m) {
        this.mnemonic = m;
        return this;
    }

    public MenuItemBuilder withClickListener(MenuItemClickListener listener) {
        if(listeners == null) listeners = new ArrayList<>();
        listeners.add(listener);
        return this;
    }

    public MenuItem build() {
        MenuItem mi = null;
        if(txt != null) {
            mi = new MenuItem(txt);
            if(icon != null) mi.setIcon(icon);
            if(mnemonic != -1) mi.setMnemonic(mnemonic);
            if(listeners != null) listeners.forEach(mi::addClickListener);
            if(children != null) children.forEach(mi::add);
            if(miRunnable != null) miRunnable.run(mi);
            if(disabled) mi.setEnabled(false);
            return mi;
        }
        return null;
    }

    public Menu buildMenu() {
        Menu mi = null;
        if(txt != null) {
            mi = new Menu(txt);
            if(icon != null) mi.setIcon(icon);
            if(mnemonic != -1) mi.setMnemonic(mnemonic);
            if(listeners != null) listeners.forEach(mi::addClickListener);
            if(children != null) children.forEach(mi::add);
            if(mRunnable != null) mRunnable.run(mi);
            if(disabled) mi.setEnabled(false);
            return mi;
        }
        return null;
    }

}
