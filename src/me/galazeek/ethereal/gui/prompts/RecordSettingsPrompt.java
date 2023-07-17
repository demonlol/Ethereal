package me.galazeek.ethereal.gui.prompts;

import javax.swing.*;
import java.awt.*;

@Deprecated
public class RecordSettingsPrompt extends JDialog {

    public static void showPrompt(Window parent, String title) {
        final RecordSettingsPrompt prompt = new RecordSettingsPrompt(parent);
        prompt.setTitle(title);
        // set other components text
        prompt.setVisible(true);

    }

    private JTextField textField;


    private RecordSettingsPrompt(Window parent) {
        super(parent, Dialog.DEFAULT_MODALITY_TYPE);
        initComponents();
    }

    private void initComponents() {

    }

}
