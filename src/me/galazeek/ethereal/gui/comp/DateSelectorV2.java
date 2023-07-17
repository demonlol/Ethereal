package me.galazeek.ethereal.gui.comp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateSelectorV2 extends JPanel implements KeyListener, FocusListener {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    private JLabel label;
    private PlaceholderTextField[] fields;

    private int currentPos = 0;
    private boolean allowPastDates;

    private Map<Integer, String> previousText;

    public DateSelectorV2(String labelText, boolean allowPastDates) {
        super();
        this.previousText = new HashMap<>();

        if(labelText != null && !labelText.isEmpty()) this.label = new JLabel(labelText);

        this.allowPastDates = allowPastDates;
        this.fields = new PlaceholderTextField[3];

        this.fields[0] = new PlaceholderTextField("MM", 2, true);
        this.fields[1] = new PlaceholderTextField("DD", 2, true);
        this.fields[2] = new PlaceholderTextField("YYYY", 4, true);

        //Apply text field changes
        int mm = 40, dd = 38, yyyy = 48;
        for (int i = 0; i < fields.length; i++) {
            PlaceholderTextField field = fields[i];

            field.setFocusTraversalKeysEnabled(false); //Enable tab detection
            field.addKeyListener(this);
            field.addFocusListener(this);

            previousText.put(i, "");

            switch(i) {
                case 0:
                    field.setPreferredSize(new Dimension(mm, 25));
                    break;
                case 1:
                    field.setPreferredSize(new Dimension(dd, 25));
                    break;
                case 2:
                    field.setPreferredSize(new Dimension(yyyy, 25));
                    break;
            }
            fields[i] = field;
        }

        Box box = Box.createHorizontalBox();
        if(label != null) {
            box.add(label);
            box.add(Box.createRigidArea(new Dimension(6, 0)));
        }

        for (int i = 0; i < fields.length; i++) {
            box.add(fields[i]);
            if(i <= 1) box.add(new JLabel("/"));
        }

        add(box);
    }

    public boolean isFilledOutCorrectly() {
        if(isDataEmpty()) return false;
        if(!allowPastDates && isPastDate()) return false;

        int[] dateArray = getDateArray();

        boolean months = dateArray[0] >= 1 && dateArray[0] <= 12;
        boolean days = dateArray[1] >= 1 && dateArray[1] <= 31;
        boolean years = dateArray[2] >= 2000 && dateArray[2] <= 2099;

        return months && days && years;
    }

    public boolean isDataEmpty() {
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].getText().isEmpty()) return true;
        }
        return false;
    }

    public boolean isPastDate() {
        Date date = Date.from(Instant.now());
        return date.after(getDate());
    }

    public Date getDate() {
        Date date = null;
        try {
            date = sdf.parse(getDateString());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    public long getDateMs() {
        Date date = getDate();
        if(date == null) return 0;
        return date.getTime();
    }

    private String getDateString() {
        StringBuilder sb = new StringBuilder();
        int[] dateArray = getDateArray();
        for (int i = 0; i < dateArray.length; i++) {
            sb.append(dateArray[i]);
            if(i <= 1) sb.append("/");
        }
        return sb.toString();
    }

    private int[] getDateArray() {
        int[] darr = new int[3];
        for (int i = 0; i < fields.length; i++) {
            darr[i] = Integer.parseInt(fields[i].getText());
        }
        return darr;
    }

    private int getFocusedFieldIndex() {
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].hasFocus()) return i;
        }
        return -1;
    }

    private boolean anyFieldHaveFocus() {
        for (PlaceholderTextField field : fields) {
            if(field.hasFocus()) return true;
        }
        return false;
    }

    private void removeAllFieldFocuses() {
        for (int i = 0; i < fields.length; i++) {
            fields[i].transferFocus();
        }
    }

    private void setTabDetection(boolean b) {
        for (int i = 0; i < fields.length; i++) {
            fields[i].setFocusTraversalKeysEnabled(b);
        }
    }

    private void setFieldsFocusable(boolean b) {
        for (int i = 0; i < fields.length; i++) {
            fields[i].setFocusable(b);
        }
    }

    private boolean isStringDigital(String s) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(!Character.isDigit(chars[i])) return false;
        }
        return true;
    }

    private String stripNonNumeric(String s) {
        char[] chars = s.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char aChar : chars) {
            if(Character.isDigit(aChar)) sb.append(aChar);
        }
        return sb.toString();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override public void keyPressed(KeyEvent e) {
        char keyChar = e.getKeyChar();
        int keyCode = e.getKeyCode();
        PlaceholderTextField comp = (PlaceholderTextField) e.getComponent();

        if(keyCode == KeyEvent.VK_BACK_SPACE) {
            previousText.put(currentPos, comp.getText());
            return;
        }

        //Next
        if(keyCode == KeyEvent.VK_TAB || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE) {
            System.out.println("Old Position: " + currentPos);
            if((keyCode == KeyEvent.VK_ENTER && (currentPos == (fields.length - 1))) || keyCode == KeyEvent.VK_ESCAPE) {
                setFieldsFocusable(false);
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                setFieldsFocusable(true);
                return;
            }
            e.consume();

            if(currentPos == 2) currentPos = 0;
            else currentPos += 1;
            System.out.println("New Position: " + currentPos);

            fields[currentPos].grabFocus();
            return;
        }

    }
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void focusGained(FocusEvent e) {
        this.currentPos = getFocusedFieldIndex();
        System.out.println("[" + currentPos + "]");
    }

    @Override
    public void focusLost(FocusEvent e) {}

}
