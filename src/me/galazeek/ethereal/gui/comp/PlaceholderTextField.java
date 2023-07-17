package me.galazeek.ethereal.gui.comp;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Objects;

public class PlaceholderTextField extends JTextField implements FocusListener {

    private String placeholder;
    private int limit;
    private boolean onlyDigits;

    public PlaceholderTextField(String ph) {
        super(ph);
        this.placeholder = ph;
        this.limit = Integer.MAX_VALUE;
        this.onlyDigits = false;
        addFocusListener(this);
    }
    public PlaceholderTextField(String ph, int l, boolean onlyDigits) {
        super(ph);
        this.placeholder = ph;
        this.limit = l;
        this.onlyDigits = onlyDigits;
        addFocusListener(this);
    }

    public boolean isEmpty() {
        return (getText().equals(placeholder)) || getText().isEmpty();
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(getText().equals(placeholder)) {
            setText("");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if(getText().length() == 0) {
            setText(placeholder);
        }
    }

    @Override
    protected Document createDefaultModel() {
        return new LimitDocument();
    }

    public String getPlaceholder() {
        return placeholder;
    }


    private class LimitDocument extends PlainDocument {

        @Override
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) return;

            if(onlyDigits) str = stripNonNumeric(str);

            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }

        private String stripNonNumeric(String s) {
            char[] chars = s.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char aChar : chars) {
                if(Character.isDigit(aChar)) sb.append(aChar);
            }
            return sb.toString();
        }

    }

}
