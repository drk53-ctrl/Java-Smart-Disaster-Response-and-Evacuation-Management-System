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

import sdrs.model.Disaster;
import sdrs.model.Location;
import sdrs.service.AppContext;
import sdrs.service.DisasterService;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class DisasterDialog extends JDialog {

    private final Disaster editing;
    private final DisasterService service = AppContext.get().disasters();
    private boolean saved = false;

    private JComboBox<String> typeCombo;
    private JTextField locationNameField;
    private JComboBox<String> locationCombo;
    private JComboBox<String> severityCombo;
    private JTextField affectedField;
    private JTextArea descriptionArea;

    public DisasterDialog(JFrame owner, Disaster editing) {
        super(owner, true);
        this.editing = editing;
        setTitle(editing == null ? "Report Disaster" : "Edit Disaster " + editing.getId());
        setSize(580, Math.min(560, getToolkit().getScreenSize().height - 90));
        setMinimumSize(new java.awt.Dimension(540, 500));
        setResizable(true);
        setLayout(new BorderLayout());

        typeCombo = new JComboBox<String>(new String[] {
                Disaster.TYPE_FLOOD, Disaster.TYPE_EARTHQUAKE, Disaster.TYPE_CYCLONE,
                Disaster.TYPE_FIRE, Disaster.TYPE_LANDSLIDE, Disaster.TYPE_TSUNAMI, Disaster.TYPE_OTHER});
        severityCombo = new JComboBox<String>(new String[] {
                Disaster.SEVERITY_LOW, Disaster.SEVERITY_MEDIUM,
                Disaster.SEVERITY_HIGH, Disaster.SEVERITY_CRITICAL});
        locationNameField = new JTextField();
        locationCombo = new JComboBox<String>();
        locationCombo.addItem("Not mapped to road network");
        java.util.List<Location> locations = AppContext.get().routes().allLocations();
        for (int i = 0; i < locations.size(); i++) {
            locationCombo.addItem(locations.get(i).getId() + " - " + locations.get(i).getName());
        }
        affectedField = new JTextField();
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(UiUtil.formRow("Disaster Type *", typeCombo), gc);
        gc.gridy = 1;
        form.add(UiUtil.formRow("Location Name *", locationNameField), gc);
        gc.gridy = 2;
        form.add(UiUtil.formRow("Map Location", locationCombo), gc);
        gc.gridy = 3;
        form.add(UiUtil.formRow("Severity *", severityCombo), gc);
        gc.gridy = 4;
        form.add(UiUtil.formRow("Affected People *", affectedField), gc);
        gc.gridy = 5;
        form.add(UiUtil.formRow("Description *", new JScrollPane(descriptionArea)), gc);

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.setBackground(Theme.CARD);
        JButton save = Theme.primaryButton(editing == null ? "Report Disaster" : "Save Changes");
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
            loadValues();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private void loadValues() {
        typeCombo.setSelectedItem(editing.getType());
        locationNameField.setText(editing.getLocationName());
        for (int i = 0; i < locationCombo.getItemCount(); i++) {
            if (locationCombo.getItemAt(i).startsWith(editing.getLocationId() + " - ")) {
                locationCombo.setSelectedIndex(i);
                break;
            }
        }
        severityCombo.setSelectedItem(editing.getSeverity());
        affectedField.setText(String.valueOf(editing.getAffectedPeople()));
        descriptionArea.setText(editing.getDescription());
    }

    private String selectedLocationId() {
        Object selected = locationCombo.getSelectedItem();
        if (selected == null || selected.toString().startsWith("Not mapped")) {
            return null;
        }
        String text = selected.toString();
        int dash = text.indexOf(" - ");
        return dash < 0 ? null : text.substring(0, dash);
    }

    private void save() {
        try {
            if (editing == null) {
                service.reportDisaster(UiUtil.comboValue(typeCombo), locationNameField.getText(),
                        selectedLocationId(), UiUtil.comboValue(severityCombo), affectedField.getText(),
                        descriptionArea.getText());
            } else {
                service.updateDisaster(editing, UiUtil.comboValue(typeCombo), locationNameField.getText(),
                        selectedLocationId(), UiUtil.comboValue(severityCombo), affectedField.getText(),
                        descriptionArea.getText());
            }
            saved = true;
            dispose();
        } catch (AppException ex) {
            UiUtil.error(this, ex);
        }
    }
}
