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
import sdrs.model.Resource;
import sdrs.service.AppContext;
import sdrs.service.ResourceService;
import sdrs.ui.MainFrame;
import sdrs.ui.Refreshable;
import sdrs.ui.Theme;
import sdrs.ui.UiUtil;
import sdrs.ui.dialogs.ResourceDialog;
import sdrs.util.AppException;

public class ResourcesPanel extends JPanel implements Refreshable {

    private final MainFrame mainFrame;
    private final ResourceService service = AppContext.get().resources();

    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JComboBox<String> statusFilter;
    private DefaultTableModel model;
    private JTable table;

    public ResourcesPanel(MainFrame mainFrame) {
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
        typeFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, Resource.TYPE_FOOD,
                Resource.TYPE_WATER, Resource.TYPE_MEDICAL_KIT, Resource.TYPE_BLANKET,
                Resource.TYPE_RESCUE_EQUIPMENT, Resource.TYPE_FUEL, Resource.TYPE_OTHER});
        statusFilter = new JComboBox<String>(new String[] {UiUtil.FILTER_ALL, "Available",
                "Low Stock", "Out of Stock"});
        JButton search = Theme.primaryButton("Search");
        JButton reset = new JButton("Reset");
        reset.setFont(Theme.H3);
        JButton add = Theme.successButton("+ Add Resource");
        JButton stock = Theme.warningButton("Update Stock");
        JButton allocate = Theme.primaryButton("Allocate to Disaster");
        JButton sortQty = new JButton("Sort by Quantity");
        sortQty.setFont(Theme.H3);
        JButton delete = Theme.dangerButton("Delete");

        java.awt.event.ActionListener reload = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                refresh();
            }
        };
        search.addActionListener(reload);
        typeFilter.addActionListener(reload);
        statusFilter.addActionListener(reload);
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                searchField.setText("");
                typeFilter.setSelectedIndex(0);
                statusFilter.setSelectedIndex(0);
                sortByQuantity = false;
                refresh();
            }
        });
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                ResourceDialog dialog = new ResourceDialog(mainFrame, null);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    UiUtil.success(mainFrame, "Resource added successfully.");
                    refresh();
                }
            }
        });
        stock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                updateStock();
            }
        });
        allocate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                allocateToDisaster();
            }
        });
        sortQty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent event) {
                sortByQuantity = true;
                refresh();
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
                new JLabel("Status:"), statusFilter,
                search, reset, add, stock, allocate, sortQty, delete);
    }

    private boolean sortByQuantity = false;

    private JPanel buildTable() {
        model = new DefaultTableModel(new Object[] {"ID", "Name", "Type", "Quantity", "Unit",
                "Stored At", "Status"}, 0) {
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

    private Resource selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.info(mainFrame, "Select a resource in the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = String.valueOf(model.getValueAt(modelRow, 0));
        return (Resource) AppContext.get().dataManager().resourceById(id);
    }

    private void updateStock() {
        Resource resource = selected();
        if (resource == null) {
            return;
        }
        String input = javax.swing.JOptionPane.showInputDialog(mainFrame,
                "Change stock of " + resource.getName() + " (current " + resource.getQuantity()
                + " " + resource.getUnit() + ").\nEnter a positive or negative number:",
                "Update Stock", javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (input == null) {
            return;
        }
        try {
            service.updateQuantity(resource.getId(), input);
            UiUtil.success(mainFrame, "Stock updated.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    private void allocateToDisaster() {
        Resource resource = selected();
        if (resource == null) {
            return;
        }
        Disaster disaster = chooseDisaster();
        if (disaster == null) {
            return;
        }
        String input = javax.swing.JOptionPane.showInputDialog(mainFrame,
                "Allocate how many " + resource.getUnit() + " of " + resource.getName() + " (in stock: "
                + resource.getQuantity() + ") to " + disaster.getId() + "?",
                "Allocate Resource", javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (input == null) {
            return;
        }
        try {
            String userName = "Resource Manager";
            service.allocate(resource.getId(), disaster.getId(), input, userName);
            UiUtil.success(mainFrame, "Allocation recorded and stock reduced.");
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
            UiUtil.info(mainFrame, "No active disasters to allocate to.");
            return null;
        }
        String[] options = new String[active.size()];
        for (int i = 0; i < active.size(); i++) {
            Disaster d = active.get(i);
            options[i] = d.getId() + "  " + d.getType() + " at " + d.getLocationName();
        }
        String chosen = (String) javax.swing.JOptionPane.showInputDialog(mainFrame,
                "Allocate to which active disaster?", "Allocate Resource",
                javax.swing.JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (chosen == null) {
            return null;
        }
        String id = chosen.substring(0, chosen.indexOf(' '));
        return (Disaster) AppContext.get().dataManager().disasterById(id);
    }

    private void deleteSelected() {
        Resource resource = selected();
        if (resource == null) {
            return;
        }
        if (!UiUtil.confirm(mainFrame, "Delete resource " + resource.getName() + "?")) {
            return;
        }
        try {
            service.deleteResource(resource.getId());
            UiUtil.success(mainFrame, "Resource deleted.");
            refresh();
        } catch (AppException ex) {
            UiUtil.error(mainFrame, ex);
        }
    }

    public void refresh() {
        java.util.List<Resource> resources = service.search(searchField == null ? "" : searchField.getText(),
                UiUtil.comboValue(typeFilter), UiUtil.comboValue(statusFilter));
        if (sortByQuantity) {
            java.util.List<Object> boxed = new java.util.ArrayList<Object>(resources);
            java.util.List<Object> sorted = sdrs.algorithm.SortSearch.mergeSort(boxed,
                    new sdrs.algorithm.SortSearch.Key() {
                        public double keyOf(Object item) {
                            return ((Resource) item).getQuantity();
                        }
                    }, true);
            resources = new java.util.ArrayList<Resource>();
            for (int i = 0; i < sorted.size(); i++) {
                resources.add((Resource) sorted.get(i));
            }
        }
        UiUtil.clearRows(model);
        for (int i = 0; i < resources.size(); i++) {
            Resource r = resources.get(i);
            String storedAt = AppContext.get().routes().locationName(r.getLocationId());
            UiUtil.addRow(model, new Object[] {
                    r.getId(), r.getName(), r.getType(), String.valueOf(r.getQuantity()), r.getUnit(),
                    storedAt, r.statusLabel()});
        }
        UiUtil.packColumns(table);
    }
}
