package com.ortiz.util;

import javax.swing.*;

public class DialogManagerUtil {

    public static int showOptionDialog(Object[] options, String message, String title, int optionType, int messageType, int defaultOptionIndex) {
        return JOptionPane.showOptionDialog(null, message, title, optionType, messageType, null, options, options[defaultOptionIndex]);
    }

    public static void showInfoDialog(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, null, JOptionPane.ERROR_MESSAGE);
    }
}