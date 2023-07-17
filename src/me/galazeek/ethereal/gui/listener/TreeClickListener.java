package me.galazeek.ethereal.gui.listener;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public interface TreeClickListener {

    void onClick(Point point, DefaultMutableTreeNode node, String name, boolean doubleClick, boolean leftClick, boolean isFolder);

}
