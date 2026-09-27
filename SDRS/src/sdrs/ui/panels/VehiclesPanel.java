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
import sdrs.model.Vehicle;
import sdrs.service.AppContext;
import sdrs.service.VehicleService;
import sdrs.ui.MainFrame;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.ui.dialogs.VehicleDialog;
import sdrs.util.AppException;

public class VehiclesPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final VehicleService service = AppContext.get().vehicles();

    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JComboBox<String> availabilityFilter;
    private DefaultTableModel model;
    private JTable table;

    public VehiclesPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildToolbar() {
        searchField = new JTextField(14);
        typeFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, Vehicle.TYPE_AMBULANCE,
                Vehicle.TYPE_FIRE_TRUCK, Vehicle.TYPE_RESCUE, Vehicle.TYPE_SUPPLY});
        availabilityFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, "Available", "Assigned"});
        JButton search = Theme.primaryButton("Search");
        JButton reset = new JButton("Reset");
        reset.setFont(Theme.H3);
        JButton add = Theme.successButton("+ Add Vehicle");
        JButton edit = Theme.primaryButton("Edit");
        JButton assign = Theme.warningButton("Assign to Disaster");
        JButton release = new JButton("Release");
        release.setFont(Theme.H3);
        JButton delete = Theme.dangerButton("Delete");

        java.awt.event.ActionListener reload = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                refresh();
            }
        };
        search.addActionListener(reload);
        typeFilter.addActionListener(reload);
        availabilityFilter.addActionListener(reload);
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                searchField.setText("");
                typeFilter.setSelectedIndex(0);
                availabilityFilter.setSelectedIndex(0);
                refresh();
            }
        });
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                VehicleDialog dialog = new VehicleDialog(mainFrame, null);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    UiUtil.success(mainFrame, "Vehicle added successfully.");
                    refresh();
                }
            }
        });
        edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                Vehicle vehicle = selected();
                if (vehicle == null) {
                    return;
                }
                VehicleDialog dialog = new VehicleDialog(mainFrame, vehicle);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    refresh();
                }
            }
        });
        assign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                assignSelected();
            }
        });
        release.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                releaseSelected();
            }
        });
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                deleteSelected();
            }
        });

        return UiUtil.toolbar(
                new JLabel("Search:"), searchField,
                new JLabel("Type:"), typeFilter,
                new JLabel("Availability:"), availabilityFilter,
                search, reset, add, edit, assign, release, delete);
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(new Object[] {"ID", "Type", "Plate", "Driver", "Base Location",
                "Availability", "Assigned Disaster"}, 0) {
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

    private Vehicle selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.info(mainFrame, "Select a vehicle in the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = String.valueOf(model.getValueAt(modelRow, 0));
        return (Vehicle) AppContext.get().dataManager().vehicleById(id);
    }

    private void assignSelected() {
        Vehicle vehicle = selected();
        if (vehicle == null) {
            return;
        }
        Disaster disaster = chooseDisaster();
        if (disaster == null) {
            return;
        }
        try {
            service.assignToDisaster(vehicle.getId(), disaster.getId());
            UiUtil.success(mainFrame, vehicle.getType() + " assigned to " + disaster.getId() + ".");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void releaseSelected() {
        Vehicle vehicle = selected();
        if (vehicle == null) {
            return;
        }
        try {
            service.releaseFromDisaster(vehicle.getId());
            UiUtil.success(mainFrame, vehicle.getType() + " released.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void deleteSelected() {
        Vehicle vehicle = selected();
        if (vehicle == null) {
            return;
        }
        if (!UiUtil.confirm(mainFrame, "Delete vehicle " + vehicle.getPlateNumber() + "?")) {
            return;
        }
        try {
            service.deleteVehicle(vehicle.getId());
            UiUtil.success(mainFrame, "Vehicle deleted.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private Disaster chooseDisaster() {
        java.util.List<Disaster> active = new java.util.ArrayList<Disaster>();
        java.util.List<Disaster> all = AppContext.get().disasters().allDisasters();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).isActiveLike()) {
                active.add(all.get(i));
            }
        }
        if (active.isEmpty()) {
            UiUtil.info(mainFrame, "No active disasters to assign to.");
            return null;
        }
        String[] options = new String[active.size()];
        for (int i = 0; i < active.size(); i++) {
            Disaster d = active.get(i);
            options[i] = d.getId() + "  " + d.getType() + " at " + d.getLocationName()
                    + " [" + d.getSeverity() + ", score " + d.getPriorityScore() + "]";
        }
        String chosen = (String) javax.swing.JOptionPane.showInputDialog(mainFrame,
                "Assign vehicle to which active disaster?", "Assign Vehicle",
                javax.swing.JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (chosen == null) {
            return null;
        }
        String id = chosen.substring(0, chosen.indexOf(' '));
        return (Disaster) AppContext.get().dataManager().disasterById(id);
    }

    public void refresh() {
        java.util.List<Vehicle> vehicles = service.search(searchField == null ? "" : searchField.getText(),
                UiUtil.comboValue(typeFilter), UiUtil.comboValue(availabilityFilter));
        UiUtil.clearRows(model);
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle v = vehicles.get(i);
            String base = AppContext.get().routes().locationName(v.getLocationId());
            UiUtil.addRow(model, new Object[] {
                    v.getId(), v.getType(), v.getPlateNumber(), v.getDriverName(), base,
                    v.isAvailable() ? "Available" : "Assigned",
                    v.getAssignedDisasterId() == null ? "-" : v.getAssignedDisasterId()});
        }
        UiUtil.packColumns(table);
    }
}
