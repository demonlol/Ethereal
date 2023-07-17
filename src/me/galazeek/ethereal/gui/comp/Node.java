package me.galazeek.ethereal.gui.comp;

import me.galazeek.ethereal.gui.listener.TreeClickListener;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Node extends DefaultMutableTreeNode {

    private List<TreeClickListener> listeners;

    public Node(Object userObject) {
        super(userObject);
    }

    public boolean hasListeners() {
        return listeners != null && listeners.size() > 0;
    }

    public void addClickListener(TreeClickListener listener) {
        if(listeners == null) listeners = new ArrayList<>();
        this.listeners.add(listener);
    }

    public void setText(String s) {
        this.setUserObject(s);
    }

    public void removeClickListener(TreeClickListener listener) {
        if(listeners == null) listeners = new ArrayList<>();
        this.listeners.remove(listener);
    }
    public void click(Point point, DefaultMutableTreeNode node, String name, boolean doubleClick, boolean leftClick, boolean isLeaf) {
        if(listeners == null) return;
        for (TreeClickListener listener : listeners) {
            listener.onClick(point, node, name, doubleClick, leftClick, isLeaf);
        }
    }
}
