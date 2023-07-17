package me.galazeek.ethereal.gui.comp;

import javax.swing.*;

public class Spinner extends JSpinner {

    private SpinnerNumberModel model;
    private JSpinner.NumberEditor editor;

    public Spinner(double defaultVal, double min, double max, double stepSize) {
        super();
        setModel(new SpinnerNumberModel(defaultVal, min, max, stepSize));
        setEditor(new JSpinner.NumberEditor(this));
    }

}
