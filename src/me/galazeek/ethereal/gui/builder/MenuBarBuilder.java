package me.galazeek.ethereal.gui.builder;

import me.galazeek.ethereal.gui.comp.Menu;
import me.galazeek.ethereal.gui.comp.MenuBar;
import me.galazeek.ethereal.gui.comp.MenuItem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MenuBarBuilder {

    private List<JComponent> children;

    public MenuBarBuilder() {
        this.children = new ArrayList<>();
    }

    public MenuBarBuilder withMenuItem(MenuItem mi) {
        children.add(mi);
        return this;
    }
    public MenuBarBuilder withMenu(Menu mi) {
        children.add(mi);
        return this;
    }
    public MenuBarBuilder withSeparator() {
        children.add(new JSeparator());
        return this;
    }
    public MenuBar build() {
        MenuBar mb = new MenuBar();
        children.forEach(mb::add);
        children.forEach(m -> System.out.println(m.getUIClassID()));
        return mb;
    }

}
