package gui.util.dialogs.message;

import gui.util.enums.DialogFactoryOptions;
import gui.util.interfaces.IDialog;
import gui.util.interfaces.IFrame;

import javax.swing.*;

public class MessageDialogView implements IDialog {
    private IFrame mainFrame;
    private String message;
    private String title;
    private int messageType;

    public MessageDialogView(IFrame mainFrame, String message, String title, DialogFactoryOptions.dialogType messageType) {
        this.mainFrame = mainFrame;
        this.message = message;
        this.title = title != null ? title : "Message";

        switch (messageType) {
            case ERROR:
                this.messageType = JOptionPane.ERROR_MESSAGE;
                break;
            case WARNING:
                this.messageType = JOptionPane.WARNING_MESSAGE;
                break;
            case QUESTION:
                this.messageType = JOptionPane.QUESTION_MESSAGE;
                break;
            case INFORMATION:
                this.messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
            default:
                this.messageType = JOptionPane.PLAIN_MESSAGE;
                break;
        }
    }

    @Override
    public Object show() {
        JOptionPane.showMessageDialog(mainFrame.getFrame(), message, title, messageType);

        return null;
    }
}
