package sdrs.ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import sdrs.model.User;
import sdrs.service.AppContext;
import sdrs.service.DataManager;
import sdrs.ui.MainFrame;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class UserManagementPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final DataManager dataManager = AppContext.get().dataManager();

    private DefaultTableModel model;
    private JTable table;

    public UserManagementPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel toolbar;
        JButton add = Theme.successButton("+ Add User");
        JButton reset = Theme.warningButton("Reset Password");
        JButton delete = Theme.dangerButton("Delete User");
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                new UserDialog(mainFrame).setVisible(true);
                refresh();
            }
        });
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                resetPassword();
            }
        });
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                deleteSelected();
            }
        });
        toolbar = UiUtil.toolbar(add, reset, delete);

        model = new DefaultTableModel(new Object[] {"ID", "Username", "Full Name", "Phone", "Role"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = UiUtil.makeTable(model);
        UiUtil.addToolTipRenderer(table);
        add(toolbar, BorderLayout.NORTH);
        add(UiUtil.tableScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    private User selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.info(mainFrame, "Select a user in the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String username = String.valueOf(model.getValueAt(modelRow, 1));
        return (User) dataManager.userByUsername(username);
    }

    private void resetPassword() {
        User user = selected();
        if (user == null) {
            return;
        }
        JPasswordField field = new JPasswordField();
        JPasswordField confirm = new JPasswordField();
        JPanel panel = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("New password for " + user.getUsername() + ":"));
        panel.add(field);
        panel.add(new JLabel("Confirm:"));
        panel.add(confirm);
        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Reset Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String password = new String(field.getPassword());
        String confirmed = new String(confirm.getPassword());
        if (password.length() < 5) {
            UiUtil.error(mainFrame, "Password must be at least 5 characters.");
            return;
        }
        if (!password.equals(confirmed)) {
            UiUtil.error(mainFrame, "Passwords do not match.");
            return;
        }
        user.setPassword(password);
        dataManager.notifyChanged();
        UiUtil.success(mainFrame, "Password updated.");
    }

    private void deleteSelected() {
        User user = selected();
        if (user == null) {
            return;
        }
        if (User.ROLE_ADMIN.equals(user.getRole())) {
            int admins = 0;
            java.util.List<User> users = dataManager.data().getUsers();
            for (int i = 0; i < users.size(); i++) {
                if (User.ROLE_ADMIN.equals(users.get(i).getRole())) {
                    admins++;
                }
            }
            if (admins <= 1) {
                UiUtil.error(mainFrame, "At least one admin account must remain.");
                return;
            }
        }
        if (!UiUtil.confirm(mainFrame, "Delete user " + user.getUsername() + "?")) {
            return;
        }
        dataManager.removeUser(user.getUsername());
        dataManager.notifyChanged();
        UiUtil.success(mainFrame, "User deleted.");
        refresh();
    }

    public void refresh() {
        java.util.List<User> users = dataManager.data().getUsers();
        UiUtil.clearRows(model);
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            UiUtil.addRow(model, new Object[] {
                    u.getId(), u.getUsername(), u.getName(), u.getPhone(), u.getRole()});
        }
        UiUtil.packColumns(table);
    }

    private class UserDialog extends JDialog {

        private JTextField usernameField;
        private JPasswordField passwordField;
        private JPasswordField confirmField;
        private JTextField nameField;
        private JTextField phoneField;
        private JComboBox<String> roleCombo;

        UserDialog(JFrame owner) {
            super(owner, true);
            setTitle("Add System User");
            setSize(560, Math.min(430, getToolkit().getScreenSize().height - 90));
            setMinimumSize(new java.awt.Dimension(520, 400));
            setResizable(true);
            setLayout(new BorderLayout());
            usernameField = new JTextField();
            passwordField = new JPasswordField();
            confirmField = new JPasswordField();
            nameField = new JTextField();
            phoneField = new JTextField();
            roleCombo = new JComboBox<String>(new String[] {User.ROLE_ADMIN, User.ROLE_DMO, User.ROLE_RO});

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Theme.CARD);
            form.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(5, 4, 5, 4);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.gridx = 0;
            gc.gridy = 0;
            form.add(UiUtil.formRow("Username *", usernameField), gc);
            gc.gridy = 1;
            form.add(UiUtil.formRow("Password *", passwordField), gc);
            gc.gridy = 2;
            form.add(UiUtil.formRow("Confirm Password *", confirmField), gc);
            gc.gridy = 3;
            form.add(UiUtil.formRow("Full Name *", nameField), gc);
            gc.gridy = 4;
            form.add(UiUtil.formRow("Phone *", phoneField), gc);
            gc.gridy = 5;
            form.add(UiUtil.formRow("Role *", roleCombo), gc);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.setBackground(Theme.CARD);
            JButton save = Theme.primaryButton("Create User");
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

        private void save() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            try {
                if (username.length() < 3 || username.length() > 20
                        || !username.matches("[a-zA-Z0-9_]+")) {
                    throw new AppException("Username must be 3-20 letters, digits or underscore.");
                }
                if (dataManager.userByUsername(username) != null) {
                    throw new AppException("Username already exists.");
                }
                if (password.length() < 5) {
                    throw new AppException("Password must be at least 5 characters.");
                }
                if (!password.equals(confirm)) {
                    throw new AppException("Passwords do not match.");
                }
                if (name.isEmpty()) {
                    throw new AppException("Full name is required.");
                }
                if (!phone.matches("\\d{10}")) {
                    throw new AppException("Phone must be exactly 10 digits.");
                }
                User user = new User(dataManager.nextId("USR"), username,
                        sdrs.util.PasswordUtil.hash(password), name, phone,
                        UiUtil.comboValue(roleCombo));
                dataManager.addUser(user);
                dataManager.notifyChanged();
                UiUtil.success(mainFrame, "User created.");
                dispose();
            } catch (AppException ex) {
                UiUtil.error(this, ex);
            }
        }
    }
}
