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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import sdrs.model.Citizen;
import sdrs.model.Location;
import sdrs.service.AppContext;
import sdrs.service.CitizenService;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class CitizenDialog extends JDialog {

    private final Citizen editing;
    private final CitizenService service = AppContext.get().citizens();
    private boolean saved = false;

    private JTextField nameField;
    private JTextField ageField;
    private JTextField phoneField;
    private JTextField addressField;
    private JComboBox<String> locationCombo;
    private JTextField householdField;
    private JComboBox<String> priorityCombo;
    private JTextArea medicalArea;
    private JTextField contactNameField;
    private JTextField contactRelationField;
    private JTextField contactPhoneField;

    public CitizenDialog(JFrame owner, Citizen editing) {
        super(owner, true);
        this.editing = editing;
        setTitle(editing == null ? "Register Citizen" : "Edit Citizen " + editing.getId());
        setSize(600, Math.min(740, getToolkit().getScreenSize().height - 90));
        setMinimumSize(new java.awt.Dimension(560, 640));
        setResizable(true);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;

        nameField = new JTextField();
        ageField = new JTextField();
        phoneField = new JTextField();
        addressField = new JTextField();
        locationCombo = new JComboBox<String>();
        java.util.List<Location> locations = AppContext.get().routes().allLocations();
        for (int i = 0; i < locations.size(); i++) {
            locationCombo.addItem(locations.get(i).getId() + " - " + locations.get(i).getName());
        }
        householdField = new JTextField();
        priorityCombo = new JComboBox<String>(new String[] {
                Citizen.PRIORITY_LOW, Citizen.PRIORITY_MEDIUM,
                Citizen.PRIORITY_HIGH, Citizen.PRIORITY_CRITICAL});
        medicalArea = new JTextArea(2, 18);
        medicalArea.setLineWrap(true);
        contactNameField = new JTextField();
        contactRelationField = new JTextField();
        contactPhoneField = new JTextField();

        gc.gridy = 0;
        gc.gridy = row(form, gc, "Full Name *", nameField);
        gc.gridy = row(form, gc, "Age *", ageField);
        gc.gridy = row(form, gc, "Phone (10 digits) *", phoneField);
        gc.gridy = row(form, gc, "Address *", addressField);
        gc.gridy = row(form, gc, "Map Location", locationCombo);
        gc.gridy = row(form, gc, "Household Size *", householdField);
        gc.gridy = row(form, gc, "Emergency Priority *", priorityCombo);
        gc.gridy = row(form, gc, "Medical Requirement", new JScrollPane(medicalArea));
        gc.gridy = row(form, gc, "Emergency Contact Name", contactNameField);
        gc.gridy = row(form, gc, "Contact Relation", contactRelationField);
        gc.gridy = row(form, gc, "Contact Phone", contactPhoneField);

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.setBackground(Theme.CARD);
        JButton save = Theme.primaryButton(editing == null ? "Register Citizen" : "Save Changes");
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

        add(new JScrollPane(form), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setLocationRelativeTo(owner);
        if (editing != null) {
            loadValues();
        }
    }

    private int row(JPanel form, GridBagConstraints gc, String label, javax.swing.JComponent field) {
        form.add(UiUtil.formRow(label, field), gc);
        return gc.gridy + 1;
    }

    public boolean isSaved() {
        return saved;
    }

    private void loadValues() {
        nameField.setText(editing.getName());
        ageField.setText(String.valueOf(editing.getAge()));
        phoneField.setText(editing.getPhone());
        addressField.setText(editing.getAddress());
        selectLocation(editing.getLocationId());
        householdField.setText(String.valueOf(editing.getHouseholdSize()));
        priorityCombo.setSelectedItem(editing.getEmergencyPriority());
        medicalArea.setText(editing.getMedicalRequirement());
        if (editing.getEmergencyContact() != null) {
            contactNameField.setText(editing.getEmergencyContact().getName());
            contactRelationField.setText(editing.getEmergencyContact().getRelation());
            contactPhoneField.setText(editing.getEmergencyContact().getPhone());
        }
    }

    private void selectLocation(String locationId) {
        if (locationId == null) {
            return;
        }
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
                service.addCitizen(nameField.getText(), ageField.getText(), phoneField.getText(),
                        addressField.getText(), locationId, householdField.getText(),
                        UiUtil.comboValue(priorityCombo), medicalArea.getText(),
                        contactNameField.getText(), contactRelationField.getText(),
                        contactPhoneField.getText());
            } else {
                service.updateCitizen(editing, nameField.getText(), ageField.getText(), phoneField.getText(),
                        addressField.getText(), locationId, householdField.getText(),
                        UiUtil.comboValue(priorityCombo), medicalArea.getText(),
                        contactNameField.getText(), contactRelationField.getText(),
                        contactPhoneField.getText());
            }
            saved = true;
            dispose();
        } catch (AppException ex) {
            UiUtil.error(this, ex);
        }
    }
}
