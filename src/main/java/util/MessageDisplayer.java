package util;

import javax.swing.*;

public class MessageDisplayer {

    public void display(String message) {
        JOptionPane.showConfirmDialog(null, message);
    }

    public void displayError(String errorMessage, String message) {
        JOptionPane.showConfirmDialog(null, errorMessage, message, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
    }
}
