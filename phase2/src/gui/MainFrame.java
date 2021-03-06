package gui;

import gui.util.enums.PanelFactoryOptions;
import gui.util.factories.DialogFactory;
import gui.util.factories.PanelFactory;
import gui.util.interfaces.IDialogFactory;
import gui.util.interfaces.IFrame;
import gui.util.interfaces.IPanel;
import gui.util.interfaces.IPanelFactory;
import util.ControllerBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Stores the primary JFrame and the necessary tools to navigate between pages
 */
public class MainFrame implements IFrame {
    private ControllerBundle controllerBundle;

    private Runnable shutdown;
    private JFrame frame;

    private IPanelFactory panelFactory;
    private IDialogFactory dialogFactory;

    private final int initialWidth = 1100;
    private final int initialHeight = 700;

    /**
     * Constructs the main UI system.
     *
     * @param controllerBundle contains all of the controllers that the UI needs to interact with
     * @param shutdown         runnable that is executed when JFrame is shut down
     */
    public MainFrame(ControllerBundle controllerBundle, Runnable shutdown) {
        this.controllerBundle = controllerBundle;
        this.shutdown = shutdown;

        panelFactory = new PanelFactory(this);
        dialogFactory = new DialogFactory(this);
    }

    /**
     * Gets the controller bundle
     *
     * @return the controller bundle
     */
    @Override
    public ControllerBundle getControllerBundle() {
        return controllerBundle;
    }

    /**
     * Sets the current panel
     *
     * @param newPanel the new panel to load
     */
    @Override
    public void setPanel(IPanel newPanel) {
        frame.setContentPane(newPanel.getPanel());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Get the panel factory initialized for this IFrame
     *
     * @return panel factory initialized for this IFrame
     */
    @Override
    public IPanelFactory getPanelFactory() {
        return panelFactory;
    }

    @Override
    public IDialogFactory getDialogFactory() {
        return dialogFactory;
    }

    @Override
    public JFrame getFrame() {
        return frame;
    }

    private void setLookAndFeel() {
        String osName = System.getProperty("os.name").toLowerCase();

        // We don't want to apply a custom theme if we're not on macOS since the default mac look and feel is already hot
        if (!osName.contains("darwin") && !osName.contains("mac")) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                System.out.println("Unable to load custom look and feel");
            }
        }
    }

    /**
     * Runs the main UI loop
     * <p>
     * If the user is not logged in, present loginUI/register prompts. Otherwise, send them to the main menu.
     */
    public void run() {

        setLookAndFeel();

        frame = new JFrame("Conference Management System");

        // Adds listener to run shutdown sequence
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown.run();
            }
        });

        // Restrict size
        Dimension initialDimension = new Dimension(initialWidth, initialHeight);

        frame.setMinimumSize(initialDimension);

        // Open panel depending on login state
        if (controllerBundle.getUserController().getCurrentUser() != null) {
            setPanel(panelFactory.createPanel(PanelFactoryOptions.panelNames.MAIN_MENU));
        } else {
            setPanel(panelFactory.createPanel(PanelFactoryOptions.panelNames.LOGIN));
        }
    }
}
