package me.galazeek.ethereal.gui.prompts;

import me.galazeek.ethereal.classes.ClassesManager;
import me.galazeek.ethereal.exceptions.CourseNameExistsException;
import me.galazeek.ethereal.gui.comp.DateSelectorV2;
import me.galazeek.ethereal.gui.comp.PlaceholderTextField;
import me.galazeek.ethereal.gui.comp.Spinner;
import me.galazeek.ethereal.gui.prompts.obj.ClassData;
import me.galazeek.ethereal.utils.SwingUtils2;

import javax.swing.*;
import java.awt.*;

public class CreateClassPrompt {

    private Frame container;

    private JDialog dialog;

    private PlaceholderTextField className, teacherName;
    private JSpinner credits;
    private DateSelectorV2 dateStart, dateEnd;

    private JButton createBtn;

    private ClassData classData;

    public CreateClassPrompt(Frame container) {
        this.container = container;
        this.classData = new ClassData();

        initFrame();
        initComponents();
    }

    public ClassData get() {
        dialog.setVisible(true);
        return classData;
    }
    public void close() {
        dialog.dispose();
        container.toFront();
    }

    private void initFrame() {
        dialog = new JDialog(container);
        dialog.setModal(true);
        dialog.setSize(300, 275);
        dialog.setLayout(new FlowLayout());
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    private void initComponents() {
        className = new PlaceholderTextField("Course's name");
        teacherName = new PlaceholderTextField("Teacher's name");
        credits = new Spinner(3, 0, 20, 0.5);
        dateStart = new DateSelectorV2("Start (m/d/y):", false);
        dateEnd = new DateSelectorV2("End (m/d/y):", false);

        dialog.add(SwingUtils2.HorizontalBox(new JLabel("Class: "), className));
        dialog.add(SwingUtils2.MaxSeparator());
        dialog.add(SwingUtils2.HorizontalBox(new JLabel("Teacher: "), teacherName));
        dialog.add(SwingUtils2.MaxSeparator());
        dialog.add(credits);
        dialog.add(SwingUtils2.MaxSeparator());
        dialog.add(dateStart);
        dialog.add(dateEnd);

        createBtn = new JButton("Create");
        createBtn.addActionListener(a -> {
            //Check for invalid input
            if(className.isEmpty() || teacherName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fulfill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if(((Double) credits.getValue()) < .5) {
                JOptionPane.showMessageDialog(dialog, "A class cannot be <.5 credits", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if(!dateStart.isFilledOutCorrectly() || !dateEnd.isFilledOutCorrectly()) {
                JOptionPane.showMessageDialog(dialog, "Dates are incorrect", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            classData.setName(className.getText());
            classData.setTeacher(teacherName.getText());
            classData.setCredits((Double) credits.getValue());
            classData.setStart(dateStart.getDate());
            classData.setEnd(dateEnd.getDate());

            try {
                ClassesManager.CreateCourse(classData);
                close();
            } catch (CourseNameExistsException e) {
                JOptionPane.showConfirmDialog(dialog, "Course name is already\nin use", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(createBtn);
    }

}
