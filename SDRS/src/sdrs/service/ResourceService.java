package sdrs.service;

import java.util.ArrayList;
import java.util.List;

import sdrs.model.Allocation;
import sdrs.model.Disaster;
import sdrs.model.Resource;
import sdrs.util.AppException;
import sdrs.util.Validators;

public class ResourceService {

    private final DataManager dataManager;

    public ResourceService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Resource addResource(String name, String type, String quantityText, String locationId,
                                String unit) throws AppException {
        Validators.requireText(name, "Resource name");
        Validators.requireSelection(type, "resource type");
        Validators.requireInt(quantityText, "Quantity", 0, 1000000);
        dataManager.requireLocation(locationId);
        Validators.requireText(unit, "Unit");
        Resource resource = new Resource(dataManager.nextId("RES"), name.trim(), type,
                Integer.parseInt(quantityText.trim()), locationId, unit.trim());
        dataManager.addResource(resource);
        dataManager.notifyChanged();
        return resource;
    }

    public void updateQuantity(String resourceId, String deltaText) throws AppException {
        dataManager.requireResource(resourceId);
        Resource resource = (Resource) dataManager.resourceById(resourceId);
        int delta = Validators.requireIntAny(deltaText, "Quantity change");
        int newQuantity = resource.getQuantity() + delta;
        if (newQuantity < 0) {
            throw new AppException("Stock cannot go below zero (current: " + resource.getQuantity() + ").");
        }
        resource.setQuantity(newQuantity);
        dataManager.notifyChanged();
    }

    public void deleteResource(String resourceId) throws AppException {
        dataManager.requireResource(resourceId);
        Resource resource = (Resource) dataManager.resourceById(resourceId);
        dataManager.pushDeleted(resource);
        dataManager.removeResource(resourceId);
        dataManager.notifyChanged();
    }

    public void allocate(String resourceId, String disasterId, String quantityText, String byUser) throws AppException {
        dataManager.requireResource(resourceId);
        dataManager.requireDisaster(disasterId);
        Resource resource = (Resource) dataManager.resourceById(resourceId);
        Disaster disaster = (Disaster) dataManager.disasterById(disasterId);
        int quantity = Validators.requireInt(quantityText, "Quantity", 1, 1000000);
        if (quantity > resource.getQuantity()) {
            throw new AppException("Only " + resource.getQuantity() + " " + resource.getUnit()
                    + " in stock; cannot allocate " + quantity + ".");
        }
        if (!disaster.isActiveLike()) {
            throw new AppException("Resources can only be allocated to an active disaster.");
        }
        resource.setQuantity(resource.getQuantity() - quantity);
        Allocation record = new Allocation(dataManager.nextId("ALC"), resourceId, resource.getName(),
                disasterId, quantity, System.currentTimeMillis(), byUser);
        dataManager.data().getAllocations().add(record);
        dataManager.notifyChanged();
    }

    public List<Allocation> allocationsForDisaster(String disasterId) {
        List<Allocation> result = new ArrayList<Allocation>();
        List<Allocation> allocations = dataManager.data().getAllocations();
        for (int i = 0; i < allocations.size(); i++) {
            if (allocations.get(i).getDisasterId().equals(disasterId)) {
                result.add(allocations.get(i));
            }
        }
        return result;
    }

    public List<Resource> allResources() {
        return new ArrayList<Resource>(dataManager.data().getResources());
    }

    public List<Resource> search(String keyword, String typeFilter, String statusFilter) {
        List<Resource> result = new ArrayList<Resource>();
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Resource> resources = dataManager.data().getResources();
        for (int i = 0; i < resources.size(); i++) {
            Resource r = resources.get(i);
            if (!needle.isEmpty() && !r.getName().toLowerCase().contains(needle)
                    && !r.getId().toLowerCase().contains(needle)) {
                continue;
            }
            if (typeFilter != null && !typeFilter.equals("All") && !r.getType().equals(typeFilter)) {
                continue;
            }
            if (statusFilter != null && !statusFilter.equals("All") && !r.statusLabel().equals(statusFilter)) {
                continue;
            }
            result.add(r);
        }
        return result;
    }

    public List<String> greedyAllocate(List<SupplyNeed> needs) {
        List<SupplyNeed> ordered = new ArrayList<SupplyNeed>(needs);
        for (int i = 1; i < ordered.size(); i++) {
            SupplyNeed key = ordered.get(i);
            int j = i - 1;
            while (j >= 0 && ordered.get(j).urgency < key.urgency) {
                ordered.set(j + 1, ordered.get(j));
                j--;
            }
            ordered.set(j + 1, key);
        }
        List<String> log = new ArrayList<String>();
        for (int i = 0; i < ordered.size(); i++) {
            SupplyNeed need = ordered.get(i);
            Resource resource = (Resource) dataManager.resourceById(need.resourceId);
            if (resource == null) {
                log.add(need.label + ": resource missing");
                continue;
            }
            int granted = Math.min(need.requested, resource.getQuantity());
            if (granted <= 0) {
                log.add(need.label + ": OUT OF STOCK (need " + need.requested + ")");
                continue;
            }
            resource.setQuantity(resource.getQuantity() - granted);
            if (granted < need.requested) {
                log.add(need.label + ": partial - " + granted + " of " + need.requested + " " + resource.getUnit());
            } else {
                log.add(need.label + ": " + granted + " " + resource.getUnit() + " granted");
            }
        }
        dataManager.notifyChanged();
        return log;
    }

    public static class SupplyNeed {

        public final String resourceId;
        public final String label;
        public final int requested;
        public final int urgency;

        public SupplyNeed(String resourceId, String label, int requested, int urgency) {
            this.resourceId = resourceId;
            this.label = label;
            this.requested = requested;
            this.urgency = urgency;
        }
    }
}
