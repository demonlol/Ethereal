package me.galazeek.ethereal.gui.builder;

import me.galazeek.ethereal.gui.comp.Menu;
import me.galazeek.ethereal.gui.comp.MenuItem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MenuBuilder {

    private List<JComponent> children;
    private String txt;

    public MenuBuilder() {
        this.children = new ArrayList<>();
    }

    public MenuBuilder withMenuItem(MenuItem mi) {
        children.add(mi);
        return this;
    }
    public MenuBuilder withMenu(Menu mi) {
        children.add(mi);
        return this;
    }
    public MenuBuilder withSeparator() {
        children.add(new JSeparator());
        return this;
    }

    public MenuBuilder withText(String txt) {
        this.txt = txt;
        return this;
    }

    public Menu build() {
        Menu mb = new Menu();
        if(txt != null) mb.setText(txt);
        children.forEach(mb::add);
        return mb;
    }


}
