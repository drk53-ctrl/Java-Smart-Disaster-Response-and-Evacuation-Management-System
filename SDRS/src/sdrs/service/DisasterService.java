package sdrs.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sdrs.algorithm.DisasterPriorityQueue;
import sdrs.model.Disaster;
import sdrs.model.EmergencyTeam;
import sdrs.model.Evacuation;
import sdrs.model.Vehicle;
import sdrs.util.AppException;
import sdrs.util.Validators;

public class DisasterService {

    private final DataManager dataManager;

    public DisasterService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Disaster reportDisaster(String type, String locationName, String locationId, String severity,
                                   String affectedText, String description) throws AppException {
        Validators.requireText(type, "Disaster type");
        Validators.requireText(locationName, "Location name");
        Validators.requireSelection(severity, "severity");
        Validators.requireInt(affectedText, "Affected people", 0, 5000000);
        Validators.requireText(description, "Description");
        Disaster disaster = new Disaster(dataManager.nextId("DIS"), type, locationName.trim(), locationId,
                severity, System.currentTimeMillis(), Integer.parseInt(affectedText.trim()), description.trim());
        dataManager.addDisaster(disaster);
        dataManager.notifyChanged();
        return disaster;
    }

    public void updateDisaster(Disaster disaster, String type, String locationName, String locationId,
                               String severity, String affectedText, String description) throws AppException {
        Validators.requireText(type, "Disaster type");
        Validators.requireText(locationName, "Location name");
        Validators.requireSelection(severity, "severity");
        Validators.requireInt(affectedText, "Affected people", 0, 5000000);
        Validators.requireText(description, "Description");
        disaster.setType(type);
        disaster.setLocationName(locationName.trim());
        disaster.setLocationId(locationId);
        disaster.setSeverity(severity);
        disaster.setAffectedPeople(Integer.parseInt(affectedText.trim()));
        disaster.setDescription(description.trim());
        dataManager.notifyChanged();
    }

    public void changeStatus(Disaster disaster, String newStatus) throws AppException {
        if (!Disaster.STATUS_REPORTED.equals(newStatus) && !Disaster.STATUS_ACTIVE.equals(newStatus)
                && !Disaster.STATUS_RESPONSE.equals(newStatus) && !Disaster.STATUS_RESOLVED.equals(newStatus)) {
            throw new AppException("Unknown disaster status.");
        }
        if (Disaster.STATUS_RESOLVED.equals(newStatus)) {
            List<Evacuation> evacuations = dataManager.data().getEvacuations();
            for (int i = 0; i < evacuations.size(); i++) {
                Evacuation ev = evacuations.get(i);
                if (disaster.getId().equals(ev.getDisasterId())
                        && !Evacuation.STATUS_EVACUATED.equals(ev.getStatus())) {
                    throw new AppException("Cannot resolve: evacuation " + ev.getId()
                            + " for this disaster is still " + ev.getStatus() + ".");
                }
            }
        }
        disaster.setStatus(newStatus);
        dataManager.notifyChanged();
    }

    public void deleteDisaster(String disasterId) throws AppException {
        dataManager.requireDisaster(disasterId);
        Disaster disaster = (Disaster) dataManager.disasterById(disasterId);
        dataManager.pushDeleted(disaster);
        List<Evacuation> toRemove = new ArrayList<Evacuation>();
        List<Evacuation> evacuations = dataManager.data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            if (disasterId.equals(evacuations.get(i).getDisasterId())) {
                toRemove.add(evacuations.get(i));
            }
        }
        dataManager.data().getEvacuations().removeAll(toRemove);
        for (int i = 0; i < disaster.getTeamIds().size(); i++) {
            EmergencyTeam team = (EmergencyTeam) dataManager.teamById(disaster.getTeamIds().get(i));
            if (team != null) {
                team.setAvailable(true);
                team.setAssignedDisasterId(null);
            }
        }
        for (int i = 0; i < disaster.getVehicleIds().size(); i++) {
            Vehicle vehicle = (Vehicle) dataManager.vehicleById(disaster.getVehicleIds().get(i));
            if (vehicle != null) {
                vehicle.setAvailable(true);
                vehicle.setAssignedDisasterId(null);
            }
        }
        dataManager.removeDisaster(disasterId);
        dataManager.notifyChanged();
    }

    public List<Disaster> allDisasters() {
        return new ArrayList<Disaster>(dataManager.data().getDisasters());
    }

    public List<Disaster> search(String idLocationType, String severityFilter, String statusFilter) {
        List<Disaster> result = new ArrayList<Disaster>();
        String needle = idLocationType == null ? "" : idLocationType.trim().toLowerCase();
        List<Disaster> disasters = dataManager.data().getDisasters();
        for (int i = 0; i < disasters.size(); i++) {
            Disaster d = disasters.get(i);
            if (!needle.isEmpty()
                    && !d.getLocationName().toLowerCase().contains(needle)
                    && !d.getId().toLowerCase().contains(needle)
                    && !d.getType().toLowerCase().contains(needle)) {
                continue;
            }
            if (severityFilter != null && !severityFilter.equals("All") && !d.getSeverity().equals(severityFilter)) {
                continue;
            }
            if (statusFilter != null && !statusFilter.equals("All") && !d.getStatus().equals(statusFilter)) {
                continue;
            }
            result.add(d);
        }
        return result;
    }

    public List<Disaster> disastersByPriority() {
        DisasterPriorityQueue queue = new DisasterPriorityQueue();
        List<Disaster> disasters = dataManager.data().getDisasters();
        for (int i = 0; i < disasters.size(); i++) {
            if (disasters.get(i).isActiveLike()) {
                queue.insert(disasters.get(i));
            }
        }
        return queue.toSortedList();
    }

    public List<Disaster> criticalAlerts() {
        List<Disaster> ranked = disastersByPriority();
        List<Disaster> alerts = new ArrayList<Disaster>();
        for (int i = 0; i < ranked.size() && alerts.size() < 5; i++) {
            Disaster d = ranked.get(i);
            if (Disaster.SEVERITY_CRITICAL.equals(d.getSeverity())
                    || Disaster.SEVERITY_HIGH.equals(d.getSeverity())) {
                alerts.add(d);
            }
        }
        return alerts;
    }

    public List<Disaster> recentDisasters(int count) {
        List<Disaster> copy = new ArrayList<Disaster>(dataManager.data().getDisasters());
        List<Disaster> sorted = new ArrayList<Disaster>();
        while (!copy.isEmpty()) {
            int newest = 0;
            for (int i = 1; i < copy.size(); i++) {
                if (copy.get(i).getReportedAt() > copy.get(newest).getReportedAt()) {
                    newest = i;
                }
            }
            sorted.add(copy.remove(newest));
        }
        return sorted.subList(0, Math.min(count, sorted.size()));
    }

    public static String formatTime(long millis) {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date(millis));
    }
}
