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

import sdrs.model.EmergencyTeam;
import sdrs.model.Location;
import sdrs.service.AppContext;
import sdrs.service.TeamService;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class TeamDialog extends JDialog {

    private final EmergencyTeam editing;
    private final TeamService service = AppContext.get().teams();
    private boolean saved = false;

    private JTextField nameField;
    private JComboBox<String> typeCombo;
    private JTextField memberField;
    private JTextField leaderField;
    private JComboBox<String> locationCombo;

    public TeamDialog(JFrame owner, EmergencyTeam editing) {
        super(owner, true);
        this.editing = editing;
        setTitle(editing == null ? "Add Emergency Team" : "Edit Team " + editing.getId());
        setSize(560, Math.min(420, getToolkit().getScreenSize().height - 90));
        setMinimumSize(new java.awt.Dimension(520, 380));
        setResizable(true);
        setLayout(new BorderLayout());

        nameField = new JTextField();
        typeCombo = new JComboBox<String>(new String[] {EmergencyTeam.TYPE_MEDICAL,
                EmergencyTeam.TYPE_FIRE_RESCUE, EmergencyTeam.TYPE_POLICE,
                EmergencyTeam.TYPE_SEARCH_RESCUE, EmergencyTeam.TYPE_DISASTER_RESPONSE});
        memberField = new JTextField();
        leaderField = new JTextField();
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
        form.add(UiUtil.formRow("Team Name *", nameField), gc);
        gc.gridy = 1;
        form.add(UiUtil.formRow("Team Type *", typeCombo), gc);
        gc.gridy = 2;
        form.add(UiUtil.formRow("Member Count *", memberField), gc);
        gc.gridy = 3;
        form.add(UiUtil.formRow("Leader Name *", leaderField), gc);
        gc.gridy = 4;
        form.add(UiUtil.formRow("Base Location *", locationCombo), gc);

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.setBackground(Theme.CARD);
        JButton save = Theme.primaryButton(isEdit() ? "Save Changes" : "Add Team");
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
        if (isEdit()) {
            nameField.setText(editing.getName());
            typeCombo.setSelectedItem(editing.getType());
            memberField.setText(String.valueOf(editing.getMemberCount()));
            leaderField.setText(editing.getLeaderName());
            selectLocation(editing.getLocationId());
        }
    }

    private boolean isEdit() {
        return editing != null;
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
            if (isEdit()) {
                service.updateTeam(editing, nameField.getText(), UiUtil.comboValue(typeCombo),
                        memberField.getText(), leaderField.getText(), selectedLocationId());
            } else {
                service.addTeam(nameField.getText(), UiUtil.comboValue(typeCombo), memberField.getText(),
                        leaderField.getText(), selectedLocationId());
            }
            saved = true;
            dispose();
        } catch (AppException ex) {
            UiUtil.error(this, ex);
        }
    }
}
