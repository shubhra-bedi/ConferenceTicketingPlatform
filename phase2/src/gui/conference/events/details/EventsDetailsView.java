package gui.conference.events.details;

import gui.util.interfaces.IFrame;
import gui.util.interfaces.IPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Map;
import java.util.UUID;

public class EventsDetailsView implements IEventsDetailsView, IPanel {
    private JPanel eventsGeneralPanel;
    private JButton registerButton;
    private JButton deleteEventsButton;
    private JButton editEventsButton;
    private JTable generalEventsTable;

    private EventsDetailsPresenter eventsGeneralPresenter;

    public EventsDetailsView(IFrame mainFrame, UUID eventUUID, UUID conferenceUUID, Map<String, Object> initializationArguments){
        eventsGeneralPresenter = new EventsDetailsPresenter(mainFrame, this, eventUUID, conferenceUUID, initializationArguments);

        registerButton.addActionListener((e)-> eventsGeneralPresenter.toggleRegistration());
        deleteEventsButton.addActionListener((e)-> eventsGeneralPresenter.deleteEvent());
        editEventsButton.addActionListener((e)-> eventsGeneralPresenter.editEvent());
    }

    @Override
    public void setRegisterButtonText(String text) {
        registerButton.setText(text);
    }

    @Override
    public void setTableData(String[][] tableData, String[] columnNames) {
        TableModel tableModel = new DefaultTableModel(tableData, columnNames);
        generalEventsTable.setModel(tableModel);
    }

    @Override
    public JPanel getPanel() {
        return eventsGeneralPanel;
    }
}
