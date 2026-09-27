package sdrs.ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import sdrs.model.Citizen;
import sdrs.service.AppContext;
import sdrs.service.CitizenService;
import sdrs.ui.MainFrame;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.ui.dialogs.CitizenDialog;
import sdrs.util.AppException;

public class CitizensPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final CitizenService service = AppContext.get().citizens();

    private JTextField searchField;
    private JComboBox<String> priorityFilter;
    private JComboBox<String> statusFilter;
    private DefaultTableModel model;
    private JTable table;

    public CitizensPanel(MainFrame mainFrame) {
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
        priorityFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, Citizen.PRIORITY_LOW,
                Citizen.PRIORITY_MEDIUM, Citizen.PRIORITY_HIGH, Citizen.PRIORITY_CRITICAL});
        statusFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, Citizen.EV_PENDING,
                Citizen.EV_ASSIGNED, Citizen.EV_EVACUATING, Citizen.EV_EVACUATED});
        JButton search = Theme.primaryButton("Search");
        JButton reset = new JButton("Reset");
        reset.setFont(Theme.H3);
        JButton add = Theme.successButton("+ Add Citizen");
        JButton edit = Theme.primaryButton("Edit");
        JButton details = new JButton("View Details");
        details.setFont(Theme.H3);
        JButton delete = Theme.dangerButton("Delete");
        JButton undo = new JButton("Undo Delete");
        undo.setFont(Theme.H3);

        java.awt.event.ActionListener reload = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                refresh();
            }
        };
        search.addActionListener(reload);
        priorityFilter.addActionListener(reload);
        statusFilter.addActionListener(reload);
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                searchField.setText("");
                priorityFilter.setSelectedIndex(0);
                statusFilter.setSelectedIndex(0);
                refresh();
            }
        });
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                CitizenDialog dialog = new CitizenDialog(mainFrame, null);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    UiUtil.success(mainFrame, "Citizen registered successfully.");
                    refresh();
                }
            }
        });
        edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                editSelected();
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
        undo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                undoDelete();
            }
        });

        return UiUtil.toolbar(
                new JLabel("Search ID/Name:"), searchField,
                new JLabel("Priority:"), priorityFilter,
                new JLabel("Evacuation:"), statusFilter,
                search, reset, add, edit, details, delete, undo);
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(new Object[] {"ID", "Name", "Age", "Phone", "Location",
                "Household", "Priority", "Evacuation Status"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = UiUtil.makeTable(model);
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Theme.BACKGROUND);
        body.add(UiUtil.tableScrollPane(table), BorderLayout.CENTER);
        JLabel hint = new JLabel("Select a row and use the buttons above. Double-click a row for details.");
        hint.setFont(Theme.SMALL);
        hint.setForeground(Theme.TEXT_LIGHT);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 2));
        body.add(hint, BorderLayout.SOUTH);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    showDetails();
                }
            }
        });
        UiUtil.addToolTipRenderer(table);
        return body;
    }

    private Citizen selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.info(mainFrame, "Select a citizen in the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = String.valueOf(model.getValueAt(modelRow, 0));
        return (Citizen) AppContext.get().dataManager().citizenById(id);
    }

    private void editSelected() {
        Citizen citizen = selected();
        if (citizen == null) {
            return;
        }
        CitizenDialog dialog = new CitizenDialog(mainFrame, citizen);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            UiUtil.success(mainFrame, "Citizen updated successfully.");
            refresh();
        }
    }

    private void showDetails() {
        Citizen citizen = selected();
        if (citizen == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(citizen.getId()).append("\n");
        sb.append("Name: ").append(citizen.getName()).append("\n");
        sb.append("Age: ").append(citizen.getAge()).append("\n");
        sb.append("Phone: ").append(citizen.getPhone()).append("\n");
        sb.append("Address: ").append(citizen.getAddress()).append("\n");
        String locationName = citizen.getLocationId() == null ? "Not mapped"
                : AppContext.get().routes().locationName(citizen.getLocationId());
        sb.append("Map Location: ").append(locationName).append("\n");
        sb.append("Household Size: ").append(citizen.getHouseholdSize()).append("\n");
        sb.append("Emergency Priority: ").append(citizen.getEmergencyPriority())
                .append(" (score ").append(citizen.getPriorityScore()).append(")\n");
        sb.append("Medical Requirement: ")
                .append(citizen.getMedicalRequirement().isEmpty() ? "None" : citizen.getMedicalRequirement())
                .append("\n");
        sb.append("Evacuation Status: ").append(citizen.getEvacuationStatus()).append("\n");
        if (citizen.getEmergencyContact() != null) {
            sb.append("Emergency Contact: ").append(citizen.getEmergencyContact().getName())
                    .append(" (").append(citizen.getEmergencyContact().getRelation()).append("), ")
                    .append(citizen.getEmergencyContact().getPhone()).append("\n");
        }
        java.util.List<sdrs.model.Disaster> disasters = AppContext.get().disasters().allDisasters();
        java.util.List<String> linkedNames = new java.util.ArrayList<String>();
        for (int i = 0; i < disasters.size(); i++) {
            if (disasters.get(i).getCitizenIds().contains(citizen.getId())) {
                linkedNames.add(disasters.get(i).getId() + " " + disasters.get(i).getType());
            }
        }
        sb.append("Linked Disasters: ")
                .append(linkedNames.isEmpty() ? "None" : String.join(", ", linkedNames)).append("\n");
        javax.swing.JTextArea area = new javax.swing.JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        javax.swing.JOptionPane.showMessageDialog(mainFrame, new javax.swing.JScrollPane(area),
                "Citizen Details", javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    private void deleteSelected() {
        Citizen citizen = selected();
        if (citizen == null) {
            return;
        }
        if (!UiUtil.confirm(mainFrame, "Delete citizen " + citizen.getName() + " (" + citizen.getId()
                + ")? Active evacuation records will be removed.")) {
            return;
        }
        try {
            service.deleteCitizen(citizen.getId());
            UiUtil.success(mainFrame, "Citizen deleted. Use Undo Delete to restore.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void undoDelete() {
        try {
            String message = AppContext.get().dataManager().undoLastDelete();
            UiUtil.success(mainFrame, message);
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    public void refresh() {
        java.util.List<Citizen> citizens = service.search(searchField == null ? "" : searchField.getText(),
                UiUtil.comboValue(priorityFilter), UiUtil.comboValue(statusFilter));
        UiUtil.clearRows(model);
        for (int i = 0; i < citizens.size(); i++) {
            Citizen c = citizens.get(i);
            String locationName = c.getLocationId() == null ? "Not mapped"
                    : AppContext.get().routes().locationName(c.getLocationId());
            UiUtil.addRow(model, new Object[] {
                    c.getId(), c.getName(), String.valueOf(c.getAge()), c.getPhone(),
                    locationName, String.valueOf(c.getHouseholdSize()),
                    c.getEmergencyPriority(), c.getEvacuationStatus()});
        }
        UiUtil.packColumns(table);
    }
}
