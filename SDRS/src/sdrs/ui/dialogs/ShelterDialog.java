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
import sdrs.model.Shelter;
import sdrs.service.AppContext;
import sdrs.service.ShelterService;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class ShelterDialog extends JDialog {

    private final Shelter editing;
    private final ShelterService service = AppContext.get().shelters();
    private boolean saved = false;

    private JTextField nameField;
    private JComboBox<String> locationCombo;
    private JTextField addressField;
    private JTextField managerField;
    private JTextField phoneField;
    private JTextField capacityField;
    private JComboBox<String> statusCombo;

    public ShelterDialog(JFrame owner, Shelter editing) {
        super(owner, true);
        this.editing = editing;
        setTitle(editing == null ? "Add Shelter" : "Edit Shelter " + editing.getId());
        setSize(580, Math.min(500, getToolkit().getScreenSize().height - 90));
        setMinimumSize(new java.awt.Dimension(540, 460));
        setResizable(true);
        setLayout(new BorderLayout());

        nameField = new JTextField();
        locationCombo = new JComboBox<String>();
        java.util.List<Location> locations = AppContext.get().routes().allLocations();
        for (int i = 0; i < locations.size(); i++) {
            locationCombo.addItem(locations.get(i).getId() + " - " + locations.get(i).getName());
        }
        addressField = new JTextField();
        managerField = new JTextField();
        phoneField = new JTextField();
        capacityField = new JTextField();
        statusCombo = new JComboBox<String>(new String[] {Shelter.STATUS_OPEN, Shelter.STATUS_CLOSED});
        boolean isEdit = editing != null;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(UiUtil.formRow("Shelter Name *", nameField), gc);
        gc.gridy = 1;
        form.add(UiUtil.formRow("Map Location *", locationCombo), gc);
        gc.gridy = 2;
        form.add(UiUtil.formRow("Address *", addressField), gc);
        gc.gridy = 3;
        form.add(UiUtil.formRow("Manager Name *", managerField), gc);
        gc.gridy = 4;
        form.add(UiUtil.formRow("Phone *", phoneField), gc);
        gc.gridy = 5;
        form.add(UiUtil.formRow("Capacity *", capacityField), gc);
        if (isEdit) {
            gc.gridy = 6;
            form.add(UiUtil.formRow("Status", statusCombo), gc);
        }

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.setBackground(Theme.CARD);
        JButton save = Theme.primaryButton(isEdit ? "Save Changes" : "Add Shelter");
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
        if (isEdit) {
            nameField.setText(editing.getName());
            selectLocation(editing.getLocationId());
            addressField.setText(editing.getAddress());
            managerField.setText(editing.getManagerName());
            phoneField.setText(editing.getPhone());
            capacityField.setText(String.valueOf(editing.getCapacity()));
            statusCombo.setSelectedItem(editing.getStatus());
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
            String locationId = selectedLocationId();
            if (editing == null) {
                service.addShelter(nameField.getText(), locationId, addressField.getText(),
                        managerField.getText(), phoneField.getText(), capacityField.getText());
            } else {
                service.updateShelter(editing, nameField.getText(), locationId, addressField.getText(),
                        managerField.getText(), phoneField.getText(), capacityField.getText(),
                        UiUtil.comboValue(statusCombo));
            }
            saved = true;
            dispose();
        } catch (AppException ex) {
            UiUtil.error(this, ex);
        }
    }
}
