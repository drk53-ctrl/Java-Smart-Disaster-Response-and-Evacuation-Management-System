package sdrs.ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import sdrs.model.Disaster;
import sdrs.service.AppContext;
import sdrs.service.DisasterService;
import sdrs.ui.MainFrame;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.ui.dialogs.DisasterDialog;
import sdrs.util.AppException;

public class DisastersPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final DisasterService service = AppContext.get().disasters();

    private JTextField searchField;
    private JComboBox<String> severityFilter;
    private JComboBox<String> statusFilter;
    private DefaultTableModel model;
    private JTable table;

    public DisastersPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildToolbar() {
        searchField = new JTextField(16);
        severityFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, Disaster.SEVERITY_LOW,
                Disaster.SEVERITY_MEDIUM, Disaster.SEVERITY_HIGH, Disaster.SEVERITY_CRITICAL});
        statusFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, Disaster.STATUS_REPORTED,
                Disaster.STATUS_ACTIVE, Disaster.STATUS_RESPONSE, Disaster.STATUS_RESOLVED});
        JButton search = Theme.primaryButton("Search");
        JButton reset = new JButton("Reset");
        reset.setFont(Theme.H3);
        JButton add = Theme.dangerButton("+ Report Disaster");
        JButton edit = Theme.primaryButton("Edit");
        JButton status = Theme.warningButton("Change Status");
        JButton prioritize = Theme.successButton("Sort by Priority");
        JButton details = new JButton("View Details");
        details.setFont(Theme.H3);
        JButton delete = Theme.dangerButton("Delete");

        java.awt.event.ActionListener reload = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                refresh();
            }
        };
        search.addActionListener(reload);
        severityFilter.addActionListener(reload);
        statusFilter.addActionListener(reload);
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                searchField.setText("");
                severityFilter.setSelectedIndex(0);
                statusFilter.setSelectedIndex(0);
                priorityMode = false;
                refresh();
            }
        });
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                DisasterDialog dialog = new DisasterDialog(mainFrame, null);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    UiUtil.success(mainFrame, "Disaster reported successfully.");
                    refresh();
                }
            }
        });
        edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                Disaster disaster = selected();
                if (disaster == null) {
                    return;
                }
                DisasterDialog dialog = new DisasterDialog(mainFrame, disaster);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    refresh();
                }
            }
        });
        status.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                changeStatus();
            }
        });
        prioritize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                priorityMode = true;
                refresh();
                UiUtil.info(mainFrame, "Table now shows active disasters ranked by triage priority (max-heap order).");
            }
        });
        details.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                showDetails();
            }
        });
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                deleteSelected();
            }
        });

        return UiUtil.toolbar(
                new JLabel("Search ID/Location/Type:"), searchField,
                new JLabel("Severity:"), severityFilter,
                new JLabel("Status:"), statusFilter,
                search, reset, add, edit, status, prioritize, details, delete);
    }

    private boolean priorityMode = false;

    private JPanel buildTable() {
        model = new DefaultTableModel(new Object[] {"ID", "Type", "Location", "Severity", "Status",
                "Affected", "Priority", "Reported"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = UiUtil.makeTable(model);
        UiUtil.addToolTipRenderer(table);
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Theme.BACKGROUND);
        body.add(UiUtil.tableScrollPane(table), BorderLayout.CENTER);
        return body;
    }

    private Disaster selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.info(mainFrame, "Select a disaster in the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = String.valueOf(model.getValueAt(modelRow, 0));
        return (Disaster) AppContext.get().dataManager().disasterById(id);
    }

    private void changeStatus() {
        Disaster disaster = selected();
        if (disaster == null) {
            return;
        }
        String[] options = new String[] {Disaster.STATUS_REPORTED, Disaster.STATUS_ACTIVE,
                Disaster.STATUS_RESPONSE, Disaster.STATUS_RESOLVED};
        String choice = (String) javax.swing.JOptionPane.showInputDialog(mainFrame,
                "New status for " + disaster.getId() + " (current: " + disaster.getStatus() + "):",
                "Change Disaster Status", javax.swing.JOptionPane.PLAIN_MESSAGE, null, options,
                disaster.getStatus());
        if (choice == null) {
            return;
        }
        try {
            service.changeStatus(disaster, choice);
            UiUtil.success(mainFrame, "Status updated to " + choice + ".");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void showDetails() {
        Disaster disaster = selected();
        if (disaster == null) {
            return;
        }
        sdrs.service.ResponseService response = AppContext.get().response();
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(disaster.getId()).append("\n");
        sb.append("Type: ").append(disaster.getType()).append("\n");
        sb.append("Location: ").append(disaster.getLocationName());
        if (disaster.getLocationId() != null) {
            sb.append(" (").append(AppContext.get().routes().locationName(disaster.getLocationId())).append(")");
        }
        sb.append("\n");
        sb.append("Severity: ").append(disaster.getSeverity()).append("\n");
        sb.append("Status: ").append(disaster.getStatus()).append("\n");
        sb.append("Affected People: ").append(disaster.getAffectedPeople()).append("\n");
        sb.append("Priority Score: ").append(disaster.getPriorityScore()).append("\n");
        sb.append("Reported: ").append(DisasterService.formatTime(disaster.getReportedAt())).append("\n");
        sb.append("Description: ").append(disaster.getDescription()).append("\n");
        sb.append("Teams Assigned: ").append(response.teamsForDisaster(disaster).size()).append("\n");
        sb.append("Vehicles Assigned: ").append(response.vehiclesForDisaster(disaster).size()).append("\n");
        sb.append("Evacuation Records: ").append(AppContext.get().evacuations().forDisaster(disaster.getId()).size());
        javax.swing.JTextArea area = new javax.swing.JTextArea(sb.toString());
        area.setEditable(false);
        javax.swing.JOptionPane.showMessageDialog(mainFrame, new javax.swing.JScrollPane(area),
                "Disaster Details", javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    private void deleteSelected() {
        Disaster disaster = selected();
        if (disaster == null) {
            return;
        }
        if (!UiUtil.confirm(mainFrame, "Delete disaster " + disaster.getId()
                + "? Its evacuation records will also be removed; teams and vehicles will be released.")) {
            return;
        }
        try {
            service.deleteDisaster(disaster.getId());
            UiUtil.success(mainFrame, "Disaster deleted. Use Undo Delete in Citizens (or re-report) if needed.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    public void refresh() {
        java.util.List<Disaster> list;
        if (priorityMode) {
            list = service.disastersByPriority();
        } else {
            list = service.search(searchField == null ? "" : searchField.getText(),
                    UiUtil.comboValue(severityFilter), UiUtil.comboValue(statusFilter));
        }
        UiUtil.clearRows(model);
        for (int i = 0; i < list.size(); i++) {
            Disaster d = list.get(i);
            UiUtil.addRow(model, new Object[] {
                    d.getId(), d.getType(), d.getLocationName(), d.getSeverity(), d.getStatus(),
                    String.valueOf(d.getAffectedPeople()), String.valueOf(d.getPriorityScore()),
                    DisasterService.formatTime(d.getReportedAt())});
        }
        UiUtil.packColumns(table);
    }
}
