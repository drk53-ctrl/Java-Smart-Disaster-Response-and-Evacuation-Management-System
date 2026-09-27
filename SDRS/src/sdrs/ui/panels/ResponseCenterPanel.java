package sdrs.ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import sdrs.model.Citizen;
import sdrs.model.Disaster;
import sdrs.model.Evacuation;
import sdrs.model.Resource;
import sdrs.service.AppContext;
import sdrs.service.ResponseService;
import sdrs.ui.MainFrame;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.util.AppException;

public class ResponseCenterPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final ResponseService response = AppContext.get().response();

    private JComboBox<String> disasterCombo;
    private JTextArea summaryArea;
    private JTextArea chainArea;
    private DefaultTableModel affectedModel;
    private DefaultTableModel assignedModel;
    private JTable affectedTable;
    private JTable assignedTable;
    private final java.util.Map<String, String> comboToId = new java.util.HashMap<String, String>();

    public ResponseCenterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setBackground(Theme.BACKGROUND);
        top.add(buildSelector(), BorderLayout.NORTH);
        top.add(buildSummary(), BorderLayout.CENTER);

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT,
                buildAffectedCard(), buildAssignedCard());
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        split.setBorder(javax.swing.BorderFactory.createEmptyBorder());

        javax.swing.JScrollPane topScroll = new javax.swing.JScrollPane(top);
        topScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        topScroll.getVerticalScrollBar().setUnitIncrement(16);
        topScroll.setColumnHeaderView(null);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(Theme.BACKGROUND);
        center.add(buildChain(), BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);

        javax.swing.JSplitPane mainSplit = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,
                topScroll, center);
        mainSplit.setResizeWeight(0.45);
        mainSplit.setDividerLocation(0.45);
        mainSplit.setBorder(javax.swing.BorderFactory.createEmptyBorder());

        add(mainSplit, BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildSelector() {
        JPanel panel = UiUtil.toolbar();
        disasterCombo = new JComboBox<String>();
        JButton refreshCombo = new JButton("Reload List");
        refreshCombo.setFont(Theme.H3);
        refreshCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                loadDisasterCombo();
            }
        });
        panel.add(new JLabel("Active disaster:"));
        panel.add(disasterCombo);
        panel.add(refreshCombo);
        return panel;
    }

    private JPanel buildSummary() {
        summaryArea = new JTextArea(7, 40);
        summaryArea.setEditable(false);
        summaryArea.setFont(Theme.NORMAL);
        summaryArea.setBackground(Theme.CARD);
        summaryArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return UiUtil.card(new JScrollPane(summaryArea), "Disaster Summary and Triage");
    }

    private JPanel buildChain() {
        chainArea = new JTextArea(9, 40);
        chainArea.setEditable(false);
        chainArea.setFont(Theme.NORMAL);
        chainArea.setBackground(Theme.CHAIN_BG);
        chainArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(Theme.CHAIN_BG);
        inner.add(new JScrollPane(chainArea), BorderLayout.CENTER);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(Theme.BACKGROUND);
        holder.add(UiUtil.card(inner, "Coordination Chain"), BorderLayout.NORTH);
        holder.add(buildActions(), BorderLayout.SOUTH);
        return holder;
    }

    private JPanel buildActions() {
        JPanel actions = UiUtil.row();
        JButton assignTeam = Theme.primaryButton("Assign Team");
        JButton assignVehicle = Theme.primaryButton("Assign Vehicle");
        JButton allocateResource = Theme.warningButton("Allocate Resource");
        JButton createEvac = Theme.successButton("Create Evacuation");
        JButton autoAssign = Theme.successButton("Auto-Assign Shelters");
        JButton route = new JButton("Show Recommended Route");
        route.setFont(Theme.H3);
        JButton dispatch = new JButton("Dispatch Queue");
        dispatch.setFont(Theme.H3);
        JButton resolve = Theme.dangerButton("Mark Resolved");

        assignTeam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dispatchQueue("Team assignment requested for selected disaster.");
                mainFrame.showPanel("Emergency Teams");
            }
        });
        assignVehicle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dispatchQueue("Vehicle assignment requested for selected disaster.");
                mainFrame.showPanel("Vehicles");
            }
        });
        allocateResource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dispatchQueue("Resource allocation requested for selected disaster.");
                mainFrame.showPanel("Resources");
            }
        });
        createEvac.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dispatchQueue("Evacuation record requested for selected disaster.");
                mainFrame.showPanel("Evacuation");
            }
        });
        autoAssign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                runAutoAssign();
            }
        });
        route.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                showRoute();
            }
        });
        dispatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                processQueue();
            }
        });
        resolve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                resolveDisaster();
            }
        });

        actions.add(assignTeam);
        actions.add(assignVehicle);
        actions.add(allocateResource);
        actions.add(createEvac);
        actions.add(autoAssign);
        actions.add(route);
        actions.add(dispatch);
        actions.add(resolve);
        return actions;
    }

    private JPanel buildAffectedCard() {
        affectedModel = new DefaultTableModel(new Object[] {"Citizen", "Priority", "Score",
                "Evacuation Status"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable        table = UiUtil.makeTable(affectedModel);
        UiUtil.addToolTipRenderer(table);
        affectedTable = table;
        return UiUtil.card(UiUtil.tableScrollPane(table),
                "Affected Citizens (sorted by urgency)");
    }

    private JPanel buildAssignedCard() {
        assignedModel = new DefaultTableModel(new Object[] {"Type", "Name", "Detail", "Status"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable        table = UiUtil.makeTable(assignedModel);
        UiUtil.addToolTipRenderer(table);
        assignedTable = table;
        return UiUtil.card(UiUtil.tableScrollPane(table),
                "Assigned Teams / Vehicles / Allocations");
    }

    private Disaster selectedDisaster() {
        Object selected = disasterCombo.getSelectedItem();
        if (selected == null) {
            UiUtil.info(mainFrame, "No active disaster selected.");
            return null;
        }
        return disasterForLabel(selected.toString());
    }

    private Disaster disasterForLabel(String label) {
        String id = comboToId.get(label);
        if (id == null) {
            return null;
        }
        return (Disaster) AppContext.get().dataManager().disasterById(id);
    }

    private void dispatchQueue(String task) {
        AppContext.get().dataManager().enqueueTask(task);
        UiUtil.info(mainFrame, "Task queued. Use 'Dispatch Queue' to process tasks one by one (FIFO).");
    }

    private void processQueue() {
        StringBuilder sb = new StringBuilder();
        String task;
        int count = 0;
        while ((task = AppContext.get().dataManager().processNextTask()) != null && count < 20) {
            sb.append("Processed: ").append(task).append("\n");
            count++;
        }
        if (count == 0) {
            sb.append("Dispatch queue is empty.");
        }
        javax.swing.JTextArea area = new javax.swing.JTextArea(sb.toString());
        area.setEditable(false);
        javax.swing.JOptionPane.showMessageDialog(mainFrame, new javax.swing.JScrollPane(area),
                "Dispatch Queue (FIFO)", javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    private void runAutoAssign() {
        Disaster disaster = selectedDisaster();
        if (disaster == null) {
            return;
        }
        List<String> log = AppContext.get().evacuations().autoAssignAllPending();
        if (log.isEmpty()) {
            UiUtil.info(mainFrame, "No pending evacuations for assignment.");
            return;
        }
        javax.swing.JTextArea area = new javax.swing.JTextArea(String.join("\n", log));
        area.setEditable(false);
        javax.swing.JOptionPane.showMessageDialog(mainFrame, new javax.swing.JScrollPane(area),
                "Shelter Auto-Assignment (priority order)", javax.swing.JOptionPane.PLAIN_MESSAGE);
        refresh();
    }

    private void showRoute() {
        Disaster disaster = selectedDisaster();
        if (disaster == null) {
            return;
        }
        sdrs.algorithm.RouteResult route = response.recommendedRoute(disaster);
        if (route == null || !route.isReachable()) {
            UiUtil.error(mainFrame, "No reachable route from the emergency center to this disaster zone."
                    + " Map the disaster location and check for blocked roads.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Recommended route from ").append(AppContext.get().routes().locationName(
                response.emergencyCenterId())).append(" to ").append(disaster.getLocationName()).append(":\n\n");
        List<String> path = route.getPathIds();
        sb.append(AppContext.get().routes().locationName(path.get(0)));
        for (int i = 1; i < path.size(); i++) {
            sdrs.algorithm.RouteStep step = route.getSteps().get(i - 1);
            sb.append("\n   |  ").append(String.format("%.1f km", step.getDistanceKm()))
                    .append(", ").append(String.format("%.0f min", step.getTravelMinutes()))
                    .append("\n> ").append(AppContext.get().routes().locationName(path.get(i)));
        }
        sb.append("\n\nTOTAL: ").append(String.format("%.1f km", route.getTotalDistanceKm()))
                .append(", ").append(String.format("%.0f min", route.getTotalMinutes()));
        javax.swing.JTextArea area = new javax.swing.JTextArea(sb.toString());
        area.setEditable(false);
        javax.swing.JOptionPane.showMessageDialog(mainFrame, new javax.swing.JScrollPane(area),
                "Recommended Route (Dijkstra, fastest time)", javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    private void resolveDisaster() {
        Disaster disaster = selectedDisaster();
        if (disaster == null) {
            return;
        }
        try {
            AppContext.get().disasters().changeStatus(disaster, Disaster.STATUS_RESOLVED);
            UiUtil.success(mainFrame, "Disaster marked as resolved.");
            loadDisasterCombo();
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void loadDisasterCombo() {
        Object previous = disasterCombo.getSelectedItem();
        disasterCombo.removeAllItems();
        comboToId.clear();
        List<Disaster> active = response.triageQueue();
        for (int i = 0; i < active.size(); i++) {
            Disaster d = active.get(i);
            String label = d.getId() + " - " + d.getType() + " at " + d.getLocationName()
                    + " [score " + d.getPriorityScore() + "]";
            comboToId.put(label, d.getId());
            disasterCombo.addItem(label);
        }
        if (previous != null) {
            for (int i = 0; i < disasterCombo.getItemCount(); i++) {
                if (previous.equals(disasterCombo.getItemAt(i))) {
                    disasterCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public void refresh() {
        loadDisasterCombo();
        Disaster disaster = selectedDisasterSilently();
        if (disaster == null) {
            summaryArea.setText("No active disasters. Report a disaster to begin coordination.");
            chainArea.setText("");
            UiUtil.clearRows(affectedModel);
            UiUtil.clearRows(assignedModel);
            return;
        }
        summaryArea.setText(response.coordinationSummary(disaster));

        StringBuilder chain = new StringBuilder();
        chain.append("DISASTER ").append(disaster.getId()).append(" (").append(disaster.getSeverity())
                .append(")\n");
        List<Citizen> ranked = response.citizensByPriority(disaster);
        chain.append("   > Affected citizens: ").append(ranked.size())
                .append(" (top priority: ").append(ranked.isEmpty() ? "-" : ranked.get(0).getName())
                .append(")\n");
        chain.append("   > Triage rank: #").append(triageRank(disaster)).append(" globally\n");
        chain.append("   > Teams: ").append(response.teamsForDisaster(disaster).size())
                .append(" | Vehicles: ").append(response.vehiclesForDisaster(disaster).size()).append("\n");
        List<Resource> resources = response.recommendedResources(disaster);
        chain.append("   > Recommended resources: ")
                .append(resources.isEmpty() ? "-" : resources.get(0).getName() + " first").append("\n");
        List<Evacuation> evacuations = AppContext.get().evacuations().forDisaster(disaster.getId());
        int withShelter = 0;
        for (int i = 0; i < evacuations.size(); i++) {
            if (evacuations.get(i).getShelterId() != null) {
                withShelter++;
            }
        }
        chain.append("   > Evacuations: ").append(evacuations.size())
                .append(" records, ").append(withShelter).append(" with shelter assigned\n");
        chain.append("   > Shelters in use: ").append(response.sheltersForDisaster(disaster).size()).append("\n");
        sdrs.algorithm.RouteResult route = response.recommendedRoute(disaster);
        chain.append("   > Route from emergency center: ")
                .append(route == null ? "not mapped" : route.summary(true));
        chainArea.setText(chain.toString());

        UiUtil.clearRows(affectedModel);
        for (int i = 0; i < ranked.size(); i++) {
            Citizen c = ranked.get(i);
            UiUtil.addRow(affectedModel, new Object[] {
                    c.getName(), c.getEmergencyPriority(), String.valueOf(c.getPriorityScore()),
                    c.getEvacuationStatus()});
        }

        UiUtil.clearRows(assignedModel);
        List<sdrs.model.EmergencyTeam> teams = response.teamsForDisaster(disaster);
        for (int i = 0; i < teams.size(); i++) {
            UiUtil.addRow(assignedModel, new Object[] {"Team", teams.get(i).getName(),
                    teams.get(i).getType() + ", " + teams.get(i).getMemberCount() + " members", "On site"});
        }
        List<sdrs.model.Vehicle> vehicles = response.vehiclesForDisaster(disaster);
        for (int i = 0; i < vehicles.size(); i++) {
            UiUtil.addRow(assignedModel, new Object[] {"Vehicle", vehicles.get(i).getType(),
                    vehicles.get(i).getPlateNumber(), "Dispatched"});
        }
        List<sdrs.model.Allocation> allocations = AppContext.get().resources()
                .allocationsForDisaster(disaster.getId());
        for (int i = 0; i < allocations.size(); i++) {
            sdrs.model.Allocation a = allocations.get(i);
            UiUtil.addRow(assignedModel, new Object[] {"Resource", a.getResourceName(),
                    a.getQuantity() + " units", "Allocated"});
        }
        if (assignedModel.getRowCount() == 0) {
            UiUtil.addRow(assignedModel, new Object[] {"-", "Nothing assigned yet",
                    "Use the buttons below the chain", "-"});
        }
        UiUtil.packColumns(affectedTable);
        UiUtil.packColumns(assignedTable);
    }

    private int triageRank(Disaster disaster) {
        List<Disaster> ranked = response.triageQueue();
        for (int i = 0; i < ranked.size(); i++) {
            if (ranked.get(i).getId().equals(disaster.getId())) {
                return i + 1;
            }
        }
        return 0;
    }

    private Disaster selectedDisasterSilently() {
        Object selected = disasterCombo.getSelectedItem();
        if (selected == null) {
            return null;
        }
        return disasterForLabel(selected.toString());
    }
}
