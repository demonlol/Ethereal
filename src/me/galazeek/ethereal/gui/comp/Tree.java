package me.galazeek.ethereal.gui.comp;

import me.galazeek.ethereal.classes.ClassesManager;
import me.galazeek.ethereal.classes.course.Course;
import me.galazeek.ethereal.gui.action.MultiNodeRunnable;

import javax.swing.*;
import javax.swing.tree.TreeNode;

public class Tree extends JTree {

    private MultiNodeRunnable updateUiRunnable;

    public Tree() {
    }

    public Tree(Object[] value) {
        super(value);
    }

    public Tree(TreeNode root) {
        super(root);
    }

    public void setUIUpdateRunnable(MultiNodeRunnable updateUiRunnable) {
        this.updateUiRunnable = updateUiRunnable;
    }

    public void updateRecordings(Node node, String courseName) {
        Course course = ClassesManager.GetCourse(courseName);
        if(course == null) {
            System.out.println("[Tree] - Failed to update courses");
            return;
        }
        updateUiRunnable.run(node, courseName);

        updateUI();
        repaint();
    }
}
