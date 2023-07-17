package me.galazeek.ethereal.gui.builder;

import me.galazeek.ethereal.gui.comp.Menu;
import me.galazeek.ethereal.gui.comp.MenuItem;
import me.galazeek.ethereal.gui.comp.Popup;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class PopupBuilder {

    private List<JComponent> menuItems;

    public PopupBuilder() {
        this.menuItems = new ArrayList<>();
    }

    public PopupBuilder withItem(MenuItem menuItem) {
        menuItems.add(menuItem);
        return this;
    }

    public PopupBuilder withItem(Menu menuItem) {
        menuItems.add(menuItem);
        return this;
    }
    public PopupBuilder withSeparator() {
        menuItems.add(new JSeparator());
        return this;
    }
    public Popup build() {
        Popup popup = new Popup();
        menuItems.forEach(popup::add);
        return popup;
    }

}
