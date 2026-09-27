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

import sdrs.model.Citizen;
import sdrs.model.Disaster;
import sdrs.service.AppContext;
import sdrs.service.EvacuationService;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class EvacuationDialog extends JDialog {

    private final EvacuationService service = AppContext.get().evacuations();
    private boolean saved = false;

    private JComboBox<String> citizenCombo;
    private JComboBox<String> disasterCombo;
    private JTextField peopleField;

    public EvacuationDialog(JFrame owner) {
        super(owner, true);
        setTitle("New Evacuation Record");
        setSize(560, Math.min(320, getToolkit().getScreenSize().height - 90));
        setMinimumSize(new java.awt.Dimension(520, 280));
        setResizable(true);
        setLayout(new BorderLayout());

        citizenCombo = new JComboBox<String>();
        java.util.List<Citizen> citizens = AppContext.get().citizens().allCitizens();
        for (int i = 0; i < citizens.size(); i++) {
            Citizen c = citizens.get(i);
            citizenCombo.addItem(c.getId() + " - " + c.getName() + " ["
                    + c.getEmergencyPriority() + "]");
        }
        disasterCombo = new JComboBox<String>();
        java.util.List<Disaster> disasters = AppContext.get().disasters().allDisasters();
        for (int i = 0; i < disasters.size(); i++) {
            Disaster d = disasters.get(i);
            if (d.isActiveLike()) {
                disasterCombo.addItem(d.getId() + " - " + d.getType() + " at " + d.getLocationName());
            }
        }
        peopleField = new JTextField();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(UiUtil.formRow("Citizen *", citizenCombo), gc);
        gc.gridy = 1;
        form.add(UiUtil.formRow("Disaster *", disasterCombo), gc);
        gc.gridy = 2;
        form.add(UiUtil.formRow("People To Evacuate *", peopleField), gc);

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.setBackground(Theme.CARD);
        JButton save = Theme.primaryButton("Create Evacuation");
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
    }

    public boolean isSaved() {
        return saved;
    }

    private String comboId(javax.swing.JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        if (selected == null) {
            return null;
        }
        String text = selected.toString();
        int dash = text.indexOf(" - ");
        return dash < 0 ? null : text.substring(0, dash);
    }

    private void save() {
        try {
            service.createEvacuation(comboId(citizenCombo), comboId(disasterCombo), peopleField.getText());
            saved = true;
            dispose();
        } catch (AppException ex) {
            UiUtil.error(this, ex);
        }
    }
}
