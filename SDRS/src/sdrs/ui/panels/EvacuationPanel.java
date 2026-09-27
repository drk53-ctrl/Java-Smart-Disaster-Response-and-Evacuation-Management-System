package sdrs.ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import sdrs.model.Citizen;
import sdrs.model.Disaster;
import sdrs.model.Evacuation;
import sdrs.model.Shelter;
import sdrs.service.AppContext;
import sdrs.service.DataManager;
import sdrs.service.EvacuationService;
import sdrs.ui.MainFrame;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.ui.dialogs.EvacuationDialog;
import sdrs.util.AppException;

public class EvacuationPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final EvacuationService service = AppContext.get().evacuations();
    private final DataManager dm = AppContext.get().dataManager();

    private JTabbedPane tabs;
    private DefaultTableModel pendingModel;
    private DefaultTableModel assignedModel;
    private DefaultTableModel evacuatingModel;
    private DefaultTableModel completedModel;
    private JTable pendingTable;
    private JTable assignedTable;
    private JTable evacuatingTable;
    private JTable completedTable;

    public EvacuationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        tabs = new JTabbedPane();
        tabs.setFont(Theme.H3);
        pendingModel = emptyModel();
        assignedModel = emptyModel();
        evacuatingModel = emptyModel();
        completedModel = emptyModel();
        tabs.addTab("Pending", wrapTable(pendingModel));
        tabs.addTab("Assigned", wrapTable(assignedModel));
        tabs.addTab("Evacuating", wrapTable(evacuatingModel));
        tabs.addTab("Completed", wrapTable(completedModel));
        tabs.setFont(Theme.H3);
        tabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent event) {
            }
        });

        add(buildToolbar(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        refresh();
    }

    private String tabStatus(int index) {
        if (index == 1) {
            return Evacuation.STATUS_ASSIGNED;
        }
        if (index == 2) {
            return Evacuation.STATUS_EVACUATING;
        }
        if (index == 3) {
            return Evacuation.STATUS_EVACUATED;
        }
        return Evacuation.STATUS_PENDING;
    }
    private DefaultTableModel emptyModel() {
        return new DefaultTableModel(new Object[] {"Evac ID", "Citizen", "Priority", "Disaster",
                "People", "Shelter", "Route", "Status"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel wrapTable(DefaultTableModel tableModel) {
        JTable table = UiUtil.makeTable(tableModel);
        if (tableModel == pendingModel) {
            pendingTable = table;
        } else if (tableModel == assignedModel) {
            assignedTable = table;
        } else if (tableModel == evacuatingModel) {
            evacuatingTable = table;
        } else {
            completedTable = table;
        }
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Theme.CARD);
        body.add(UiUtil.tableScrollPane(table), BorderLayout.CENTER);
        return body;
    }

    private JPanel buildToolbar() {
        JButton add = Theme.successButton("+ New Evacuation");
        JButton shelter = Theme.primaryButton("Assign Shelter");
        JButton auto = Theme.warningButton("Auto-Assign All (by priority)");
        JButton advance = Theme.successButton("Advance Status");
        JButton remove = Theme.dangerButton("Delete Record");

        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (AppContext.get().citizens().allCitizens().isEmpty()) {
                    UiUtil.info(mainFrame, "Register citizens first.");
                    return;
                }
                if (activeDisasters().isEmpty()) {
                    UiUtil.info(mainFrame, "No active disasters. Report a disaster first.");
                    return;
                }
                EvacuationDialog dialog = new EvacuationDialog(mainFrame);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    UiUtil.success(mainFrame, "Evacuation record created.");
                    refresh();
                }
            }
        });
        shelter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                assignShelter(false);
            }
        });
        auto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                List<String> log = service.autoAssignAllPending();
                if (log.isEmpty()) {
                    UiUtil.info(mainFrame, "No pending evacuations to assign.");
                    return;
                }
                showLog("Auto-Assignment Result (priority order)", log);
                refresh();
            }
        });
        advance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                advanceSelected();
            }
        });
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                deleteSelected();
            }
        });

        return UiUtil.toolbar(add, shelter, auto, advance, remove);
    }

    private List<Disaster> activeDisasters() {
        List<Disaster> active = new ArrayList<Disaster>();
        List<Disaster> all = AppContext.get().disasters().allDisasters();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).isActiveLike()) {
                active.add(all.get(i));
            }
        }
        return active;
    }

    private Evacuation selectedEvacuation() {
        int index = tabs.getSelectedIndex();
        JTable table = tableFor(index);
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.info(mainFrame, "Select an evacuation record in the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = String.valueOf(table.getModel().getValueAt(modelRow, 0));
        return service.byId(id);
    }

    private JTable tableFor(int index) {
        if (index == 1) {
            return assignedTable;
        }
        if (index == 2) {
            return evacuatingTable;
        }
        if (index == 3) {
            return completedTable;
        }
        return pendingTable;
    }

    private void assignShelter(boolean auto) {
        Evacuation ev = selectedEvacuation();
        if (ev == null) {
            return;
        }
        if (Evacuation.STATUS_EVACUATED.equals(ev.getStatus())) {
            UiUtil.info(mainFrame, "This evacuation is already completed.");
            return;
        }
        try {
            if (auto) {
                String name = service.autoAssignShelter(ev.getId());
                UiUtil.success(mainFrame, "Assigned to " + name);
            } else {
                Shelter shelter = chooseShelter(ev);
                if (shelter == null) {
                    return;
                }
                service.assignShelter(ev.getId(), shelter.getId());
                UiUtil.success(mainFrame, "Assigned to " + shelter.getName() + ".");
            }
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private Shelter chooseShelter(Evacuation ev) {
        List<Shelter> options = new ArrayList<Shelter>();
        List<Shelter> all = AppContext.get().shelters().allShelters();
        for (int i = 0; i < all.size(); i++) {
            Shelter s = all.get(i);
            if (!Shelter.STATUS_CLOSED.equals(s.getStatus()) && s.getAvailableCapacity() >= ev.getPeopleCount()) {
                options.add(s);
            }
        }
        if (options.isEmpty()) {
            UiUtil.info(mainFrame, "No shelter with enough free capacity for " + ev.getPeopleCount() + " people.");
            return null;
        }
        String[] names = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            names[i] = options.get(i).getId() + " - " + options.get(i).getName()
                    + " (free: " + options.get(i).getAvailableCapacity() + ")";
        }
        String chosen = (String) javax.swing.JOptionPane.showInputDialog(mainFrame,
                "Assign " + ev.getPeopleCount() + " people to which shelter?", "Assign Shelter",
                javax.swing.JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        if (chosen == null) {
            return null;
        }
        String id = chosen.substring(0, chosen.indexOf(" - "));
        return (Shelter) dm.shelterById(id);
    }

    private void advanceSelected() {
        Evacuation ev = selectedEvacuation();
        if (ev == null) {
            return;
        }
        String next = null;
        if (Evacuation.STATUS_PENDING.equals(ev.getStatus())) {
            next = Evacuation.STATUS_ASSIGNED;
        } else if (Evacuation.STATUS_ASSIGNED.equals(ev.getStatus())) {
            next = Evacuation.STATUS_EVACUATING;
        } else if (Evacuation.STATUS_EVACUATING.equals(ev.getStatus())) {
            next = Evacuation.STATUS_EVACUATED;
        } else {
            UiUtil.info(mainFrame, "This evacuation is already completed.");
            return;
        }
        try {
            if (Evacuation.STATUS_ASSIGNED.equals(next) && ev.getShelterId() == null) {
                UiUtil.info(mainFrame, "Assign a shelter first (Pending -> Assigned needs a shelter).");
                return;
            }
            service.setStatus(ev.getId(), next);
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void deleteSelected() {
        Evacuation ev = selectedEvacuation();
        if (ev == null) {
            return;
        }
        if (!UiUtil.confirm(mainFrame, "Delete evacuation record " + ev.getId()
                + "? Any shelter places held will be released.")) {
            return;
        }
        try {
            service.deleteEvacuation(ev.getId());
            UiUtil.success(mainFrame, "Record deleted.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void showLog(String title, List<String> log) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < log.size(); i++) {
            sb.append(log.get(i)).append("\n");
        }
        javax.swing.JTextArea area = new javax.swing.JTextArea(sb.toString());
        area.setEditable(false);
        javax.swing.JOptionPane.showMessageDialog(mainFrame, new javax.swing.JScrollPane(area),
                title, javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    public void refresh() {
        UiUtil.clearRows(pendingModel);
        UiUtil.clearRows(assignedModel);
        UiUtil.clearRows(evacuatingModel);
        UiUtil.clearRows(completedModel);
        List<Evacuation> all = service.allEvacuations();
        for (int i = 0; i < all.size(); i++) {
            Evacuation ev = all.get(i);
            Object[] row = rowFor(ev);
            if (Evacuation.STATUS_PENDING.equals(ev.getStatus())) {
                UiUtil.addRow(pendingModel, row);
            } else if (Evacuation.STATUS_ASSIGNED.equals(ev.getStatus())) {
                UiUtil.addRow(assignedModel, row);
            } else if (Evacuation.STATUS_EVACUATING.equals(ev.getStatus())) {
                UiUtil.addRow(evacuatingModel, row);
            } else {
                UiUtil.addRow(completedModel, row);
            }
        }
        UiUtil.packColumns(pendingTable);
        UiUtil.packColumns(assignedTable);
        UiUtil.packColumns(evacuatingTable);
        UiUtil.packColumns(completedTable);
    }

    private Object[] rowFor(Evacuation ev) {
        Citizen citizen = (Citizen) dm.citizenById(ev.getCitizenId());
        String citizenName = citizen == null ? ev.getCitizenId() : citizen.getName();
        String priority = citizen == null ? "-" : citizen.getEmergencyPriority()
                + " (" + citizen.getPriorityScore() + ")";
        Shelter shelter = ev.getShelterId() == null ? null : (Shelter) dm.shelterById(ev.getShelterId());
        String shelterName = shelter == null ? "Not assigned" : shelter.getName();
        String route = shelter == null ? "-" : service.routeDescription(ev, shelter);
        return new Object[] {ev.getId(), citizenName, priority,
                ev.getDisasterId(), String.valueOf(ev.getPeopleCount()),
                shelterName, route, ev.getStatus()};
    }

}
