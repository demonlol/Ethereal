package me.galazeek.ethereal.gui.comp;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Date;

public class DateSelectorV1 extends JTextField implements KeyListener {

    private static final int[] slashPoses = { 2, 5 };

    private String date;
    private boolean allowPastDates;

    public DateSelectorV1(boolean allowPastDates) {
        super();
        this.allowPastDates = allowPastDates;

        setText(" / /    ");
        setCaretPosition(0);

        addKeyListener(this);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(getText().equals(" / /    ")) {
                    setCaretPosition(0);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
            }
        });
    }

    public boolean isDateValid() {
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        e.consume();

        char keyChar = e.getKeyChar();
        int keyCode = e.getKeyCode();
        int caretPos = getCaretPosition();

        if(caretPos >= 10) return;
        if(keyCode == KeyEvent.VK_TAB) {

            return;
        }

        for (int i = 0; i < slashPoses.length; i++) {
            if(caretPos == slashPoses[i]) {
                setCaretPosition(i + 1);
            }
        }

        int pos1, pos2;
        if(caretPos == 0) {
            pos1 = 0;
            pos2 = caretPos + 1;
        } else {
            pos1 = caretPos - 1;
            pos2 = caretPos;
        }
        String typed = getText().substring(pos1, pos2);

        System.out.println(typed);

        if(typed.equals(" ")) {
            char[] chars = getText().toCharArray();
            chars[caretPos] = keyChar;
            setText(String.valueOf(chars));
            setCaretPosition(caretPos + 1);
        } else {
            if(Character.isDigit(typed.toCharArray()[0])) {
                setCaretPosition(caretPos + 1);
            }
            if(typed.equals("/")) {

            }
        }
    }



    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
