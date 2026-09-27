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
import sdrs.model.Resource;
import sdrs.service.AppContext;
import sdrs.service.ResourceService;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class ResourceDialog extends JDialog {

    private final Resource editing;
    private final ResourceService service = AppContext.get().resources();
    private boolean saved = false;

    private JTextField nameField;
    private JComboBox<String> typeCombo;
    private JTextField quantityField;
    private JComboBox<String> locationCombo;
    private JTextField unitField;

    public ResourceDialog(JFrame owner, Resource editing) {
        super(owner, true);
        this.editing = editing;
        setTitle(editing == null ? "Add Resource" : "Edit Resource " + editing.getId());
        setSize(560, Math.min(420, getToolkit().getScreenSize().height - 90));
        setMinimumSize(new java.awt.Dimension(520, 380));
        setResizable(true);
        setLayout(new BorderLayout());

        nameField = new JTextField();
        typeCombo = new JComboBox<String>(new String[] {Resource.TYPE_FOOD, Resource.TYPE_WATER,
                Resource.TYPE_MEDICAL_KIT, Resource.TYPE_BLANKET, Resource.TYPE_RESCUE_EQUIPMENT,
                Resource.TYPE_FUEL, Resource.TYPE_OTHER});
        quantityField = new JTextField();
        locationCombo = new JComboBox<String>();
        java.util.List<Location> locations = AppContext.get().routes().allLocations();
        for (int i = 0; i < locations.size(); i++) {
            locationCombo.addItem(locations.get(i).getId() + " - " + locations.get(i).getName());
        }
        unitField = new JTextField();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(UiUtil.formRow("Resource Name *", nameField), gc);
        gc.gridy = 1;
        form.add(UiUtil.formRow("Resource Type *", typeCombo), gc);
        gc.gridy = 2;
        form.add(UiUtil.formRow("Quantity *", quantityField), gc);
        gc.gridy = 3;
        form.add(UiUtil.formRow("Stored At *", locationCombo), gc);
        gc.gridy = 4;
        form.add(UiUtil.formRow("Unit *", unitField), gc);

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.setBackground(Theme.CARD);
        JButton save = Theme.primaryButton(editing == null ? "Add Resource" : "Save Changes");
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
            nameField.setText(editing.getName());
            typeCombo.setSelectedItem(editing.getType());
            quantityField.setText(String.valueOf(editing.getQuantity()));
            selectLocation(editing.getLocationId());
            unitField.setText(editing.getUnit());
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
                service.addResource(nameField.getText(), UiUtil.comboValue(typeCombo), quantityField.getText(),
                        selectedLocationId(), unitField.getText());
            } else {
                int target;
                try {
                    target = Integer.parseInt(quantityField.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new AppException("Quantity must be a whole number.");
                }
                if (target < 0) {
                    throw new AppException("Quantity cannot be negative.");
                }
                service.updateQuantity(editing.getId(), String.valueOf(target - editing.getQuantity()));
            }
            saved = true;
            dispose();
        } catch (AppException ex) {
            UiUtil.error(this, ex);
        }
    }
}
