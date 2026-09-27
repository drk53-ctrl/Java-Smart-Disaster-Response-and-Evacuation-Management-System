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
import sdrs.model.EmergencyTeam;
import sdrs.service.AppContext;
import sdrs.service.TeamService;
import sdrs.ui.MainFrame;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.ui.dialogs.TeamDialog;
import sdrs.util.AppException;

public class TeamsPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final TeamService service = AppContext.get().teams();

    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JComboBox<String> availabilityFilter;
    private DefaultTableModel model;
    private JTable table;

    public TeamsPanel(MainFrame mainFrame) {
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
        typeFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, EmergencyTeam.TYPE_MEDICAL,
                EmergencyTeam.TYPE_FIRE_RESCUE, EmergencyTeam.TYPE_POLICE,
                EmergencyTeam.TYPE_SEARCH_RESCUE, EmergencyTeam.TYPE_DISASTER_RESPONSE});
        availabilityFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, "Available", "Assigned"});
        JButton search = Theme.primaryButton("Search");
        JButton reset = new JButton("Reset");
        reset.setFont(Theme.H3);
        JButton add = Theme.successButton("+ Add Team");
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
                TeamDialog dialog = new TeamDialog(mainFrame, null);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    UiUtil.success(mainFrame, "Team added successfully.");
                    refresh();
                }
            }
        });
        edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                EmergencyTeam team = selected();
                if (team == null) {
                    return;
                }
                TeamDialog dialog = new TeamDialog(mainFrame, team);
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
        model = new DefaultTableModel(new Object[] {"ID", "Team Name", "Type", "Members", "Leader",
                "Base Location", "Availability", "Assigned Disaster"}, 0) {
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

    private EmergencyTeam selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.info(mainFrame, "Select a team in the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = String.valueOf(model.getValueAt(modelRow, 0));
        return (EmergencyTeam) AppContext.get().dataManager().teamById(id);
    }

    private void assignSelected() {
        EmergencyTeam team = selected();
        if (team == null) {
            return;
        }
        Disaster disaster = chooseDisaster();
        if (disaster == null) {
            return;
        }
        try {
            service.assignToDisaster(team.getId(), disaster.getId());
            UiUtil.success(mainFrame, team.getName() + " assigned to " + disaster.getId() + ".");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void releaseSelected() {
        EmergencyTeam releaseTarget = selected();
        if (releaseTarget == null) {
            return;
        }
        try {
            service.releaseFromDisaster(releaseTarget.getId());
            UiUtil.success(mainFrame, releaseTarget.getName() + " released and available again.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void deleteSelected() {
        EmergencyTeam team = selected();
        if (team == null) {
            return;
        }
        if (!UiUtil.confirm(mainFrame, "Delete team " + team.getName() + "?")) {
            return;
        }
        try {
            service.deleteTeam(team.getId());
            UiUtil.success(mainFrame, "Team deleted.");
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
                "Assign " + "team to which active disaster?", "Assign Team",
                javax.swing.JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (chosen == null) {
            return null;
        }
        String id = chosen.substring(0, chosen.indexOf(' '));
        return (Disaster) AppContext.get().dataManager().disasterById(id);
    }

    public void refresh() {
        java.util.List<EmergencyTeam> teams = service.search(searchField == null ? "" : searchField.getText(),
                UiUtil.comboValue(typeFilter), UiUtil.comboValue(availabilityFilter));
        UiUtil.clearRows(model);
        for (int i = 0; i < teams.size(); i++) {
            EmergencyTeam t = teams.get(i);
            String base = AppContext.get().routes().locationName(t.getLocationId());
            UiUtil.addRow(model, new Object[] {
                    t.getId(), t.getName(), t.getType(), String.valueOf(t.getMemberCount()),
                    t.getLeaderName(), base, t.isAvailable() ? "Available" : "Assigned",
                    t.getAssignedDisasterId() == null ? "-" : t.getAssignedDisasterId()});
        }
        UiUtil.packColumns(table);
    }
}
