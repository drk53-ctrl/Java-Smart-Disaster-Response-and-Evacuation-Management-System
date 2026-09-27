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

import sdrs.model.Shelter;
import sdrs.service.AppContext;
import sdrs.service.ShelterService;
import sdrs.ui.MainFrame;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.ui.dialogs.ShelterDialog;
import sdrs.util.AppException;

public class SheltersPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final ShelterService service = AppContext.get().shelters();

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private DefaultTableModel model;
    private JTable table;

    public SheltersPanel(MainFrame mainFrame) {
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
        statusFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, Shelter.STATUS_OPEN,
                Shelter.STATUS_FULL, Shelter.STATUS_CLOSED});
        JButton search = Theme.primaryButton("Search");
        JButton reset = new JButton("Reset");
        reset.setFont(Theme.H3);
        JButton add = Theme.successButton("+ Add Shelter");
        JButton edit = Theme.primaryButton("Edit");
        JButton occupancy = Theme.warningButton("Adjust Occupancy");
        JButton evacuees = new JButton("View Evacuees");
        evacuees.setFont(Theme.H3);
        JButton delete = Theme.dangerButton("Delete");

        java.awt.event.ActionListener reload = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                refresh();
            }
        };
        search.addActionListener(reload);
        statusFilter.addActionListener(reload);
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                searchField.setText("");
                statusFilter.setSelectedIndex(0);
                refresh();
            }
        });
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                ShelterDialog dialog = new ShelterDialog(mainFrame, null);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    UiUtil.success(mainFrame, "Shelter added successfully.");
                    refresh();
                }
            }
        });
        edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                Shelter shelter = selected();
                if (shelter == null) {
                    return;
                }
                ShelterDialog dialog = new ShelterDialog(mainFrame, shelter);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    refresh();
                }
            }
        });
        occupancy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                adjustOccupancy();
            }
        });
        evacuees.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                showEvacuees();
            }
        });
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                deleteSelected();
            }
        });

        return UiUtil.toolbar(
                new JLabel("Search ID/Name:"), searchField,
                new JLabel("Status:"), statusFilter,
                search, reset, add, edit, occupancy, evacuees, delete);
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(new Object[] {"ID", "Name", "Map Location", "Manager", "Phone",
                "Capacity", "Occupancy", "Available", "Utilization", "Status"}, 0) {
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

    private Shelter selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.info(mainFrame, "Select a shelter in the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = String.valueOf(model.getValueAt(modelRow, 0));
        return (Shelter) AppContext.get().dataManager().shelterById(id);
    }

    private void adjustOccupancy() {
        Shelter shelter = selected();
        if (shelter == null) {
            return;
        }
        String input = javax.swing.JOptionPane.showInputDialog(mainFrame,
                "Change occupancy of " + shelter.getName() + " (current " + shelter.getCurrentOccupancy()
                + "/" + shelter.getCapacity() + ").\nEnter a positive or negative number:",
                "Adjust Occupancy", javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (input == null) {
            return;
        }
        try {
            service.adjustOccupancy(shelter.getId(), input);
            UiUtil.success(mainFrame, "Occupancy updated.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void showEvacuees() {
        Shelter shelter = selected();
        if (shelter == null) {
            return;
        }
        java.util.List<sdrs.model.Citizen> evacuees = service.citizensAtShelter(shelter.getId());
        StringBuilder sb = new StringBuilder();
        if (evacuees.isEmpty()) {
            sb.append("No evacuees assigned to this shelter yet.");
        } else {
            for (int i = 0; i < evacuees.size(); i++) {
                sdrs.model.Citizen c = evacuees.get(i);
                sb.append(c.getId()).append("  ").append(c.getName())
                        .append("  (priority ").append(c.getEmergencyPriority()).append(")\n");
            }
        }
        javax.swing.JTextArea area = new javax.swing.JTextArea(sb.toString());
        area.setEditable(false);
        javax.swing.JOptionPane.showMessageDialog(mainFrame, new javax.swing.JScrollPane(area),
                "Evacuees at " + shelter.getName(), javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    private void deleteSelected() {
        Shelter shelter = selected();
        if (shelter == null) {
            return;
        }
        if (!UiUtil.confirm(mainFrame, "Delete shelter " + shelter.getName() + "?")) {
            return;
        }
        try {
            service.deleteShelter(shelter.getId());
            UiUtil.success(mainFrame, "Shelter deleted.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    public void refresh() {
        java.util.List<Shelter> shelters = service.search(searchField == null ? "" : searchField.getText(),
                UiUtil.comboValue(statusFilter), false);
        UiUtil.clearRows(model);
        for (int i = 0; i < shelters.size(); i++) {
            Shelter s = shelters.get(i);
            String mapLocation = AppContext.get().routes().locationName(s.getLocationId());
            String bar = progressBar(s.occupancyPercent());
            UiUtil.addRow(model, new Object[] {
                    s.getId(), s.getName(), mapLocation, s.getManagerName(), s.getPhone(),
                    String.valueOf(s.getCapacity()), String.valueOf(s.getCurrentOccupancy()),
                    String.valueOf(s.getAvailableCapacity()), bar, s.getStatus()});
        }
        UiUtil.packColumns(table);
    }

    private String progressBar(double percent) {
        int filled = (int) Math.round(percent / 10.0);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 10; i++) {
            sb.append(i < filled ? "#" : "-");
        }
        sb.append("] ").append((int) percent).append("%");
        return sb.toString();
    }
}
