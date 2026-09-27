package sdrs.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sdrs.model.Location;
import sdrs.model.Vehicle;
import sdrs.service.AppContext;
import sdrs.service.VehicleService;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class VehicleDialog extends JDialog {

    private final Vehicle editing;
    private final VehicleService service = AppContext.get().vehicles();
    private boolean saved = false;

    private JComboBox<String> typeCombo;
    private JTextField plateField;
    private JTextField driverField;
    private JComboBox<String> locationCombo;

    public VehicleDialog(JFrame owner, Vehicle editing) {
        super(owner, true);
        this.editing = editing;
        setTitle(editing == null ? "Add Vehicle" : "Edit Vehicle " + editing.getId());
        setSize(560, Math.min(380, getToolkit().getScreenSize().height - 90));
        setMinimumSize(new java.awt.Dimension(520, 340));
        setResizable(true);
        setLayout(new BorderLayout());

        typeCombo = new JComboBox<String>(new String[] {Vehicle.TYPE_AMBULANCE, Vehicle.TYPE_FIRE_TRUCK,
                Vehicle.TYPE_RESCUE, Vehicle.TYPE_SUPPLY});
        plateField = new JTextField();
        driverField = new JTextField();
        locationCombo = new JComboBox<String>();
        java.util.List<Location> locations = AppContext.get().routes().allLocations();
        for (int i = 0; i < locations.size(); i++) {
            locationCombo.addItem(locations.get(i).getId() + " - " + locations.get(i).getName());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(UiUtil.formRow("Vehicle Type *", typeCombo), gc);
        gc.gridy = 1;
        form.add(UiUtil.formRow("Plate Number *", plateField), gc);
        gc.gridy = 2;
        form.add(UiUtil.formRow("Driver Name *", driverField), gc);
        gc.gridy = 3;
        form.add(UiUtil.formRow("Base Location *", locationCombo), gc);

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.setBackground(Theme.CARD);
        JButton save = Theme.primaryButton(editing == null ? "Add Vehicle" : "Save Changes");
        JButton cancel = new JButton("Cancel");
        cancel.setFont(Theme.H3);
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                save();
            }
        });
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dispose();
            }
        });
        buttons.add(cancel);
        buttons.add(save);

        add(new javax.swing.JScrollPane(form), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setLocationRelativeTo(owner);
        if (editing != null) {
            typeCombo.setSelectedItem(editing.getType());
            plateField.setText(editing.getPlateNumber());
            driverField.setText(editing.getDriverName());
            selectLocation(editing.getLocationId());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private void selectLocation(String locationId) {
        for (int i = 0; i < locationCombo.getItemCount(); i++) {
            if (locationCombo.getItemAt(i).startsWith(locationId + " - ")) {
                locationCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String selectedLocationId() {
        Object selected = locationCombo.getSelectedItem();
        if (selected == null) {
            return null;
        }
        String text = selected.toString();
        int dash = text.indexOf(" - ");
        return dash < 0 ? null : text.substring(0, dash);
    }

    private void save() {
        try {
            if (editing == null) {
                service.addVehicle(UiUtil.comboValue(typeCombo), plateField.getText(), driverField.getText(),
                        selectedLocationId());
            } else {
                service.updateVehicle(editing, UiUtil.comboValue(typeCombo), plateField.getText(),
                        driverField.getText(), selectedLocationId());
            }
            saved = true;
            dispose();
        } catch (AppException ex) {
            UiUtil.error(this, ex);
        }
    }
}
