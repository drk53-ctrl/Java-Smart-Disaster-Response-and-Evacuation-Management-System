package sdrs.service;

import java.util.ArrayList;
import java.util.List;

import sdrs.algorithm.Graph;
import sdrs.algorithm.RouteResult;
import sdrs.model.Citizen;
import sdrs.model.Disaster;
import sdrs.model.Evacuation;
import sdrs.model.Shelter;
import sdrs.util.AppException;
import sdrs.util.Validators;

public class EvacuationService {

    private final DataManager dataManager;
    private final RouteService routeService;
    private final ShelterService shelterService;

    public EvacuationService(DataManager dataManager, RouteService routeService,
                             ShelterService shelterService) {
        this.dataManager = dataManager;
        this.routeService = routeService;
        this.shelterService = shelterService;
    }

    public Evacuation createEvacuation(String citizenId, String disasterId, String peopleText) throws AppException {
        dataManager.requireCitizen(citizenId);
        dataManager.requireDisaster(disasterId);
        int people = Validators.requireInt(peopleText, "People to evacuate", 1, 500);
        Citizen citizen = (Citizen) dataManager.citizenById(citizenId);
        List<Evacuation> existing = dataManager.data().getEvacuations();
        for (int i = 0; i < existing.size(); i++) {
            Evacuation ev = existing.get(i);
            if (ev.getCitizenId().equals(citizenId) && ev.getDisasterId().equals(disasterId)
                    && !Evacuation.STATUS_EVACUATED.equals(ev.getStatus())) {
                throw new AppException("An active evacuation already exists for this citizen and disaster.");
            }
        }
        if (!citizen.getDisasterIds().contains(disasterId)) {
            citizen.getDisasterIds().add(disasterId);
        }
        Evacuation ev = new Evacuation(dataManager.nextId("EVC"), citizenId, disasterId, people,
                System.currentTimeMillis());
        dataManager.data().getEvacuations().add(ev);
        citizen.setEvacuationStatus(Evacuation.STATUS_PENDING);
        dataManager.notifyChanged();
        return ev;
    }

    public void deleteEvacuation(String evacuationId) throws AppException {
        Evacuation target = null;
        List<Evacuation> evacuations = dataManager.data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            if (evacuations.get(i).getId().equals(evacuationId)) {
                target = evacuations.get(i);
            }
        }
        if (target == null) {
            throw new AppException("Evacuation record does not exist.");
        }
        if (Evacuation.STATUS_EVACUATING.equals(target.getStatus())) {
            releaseShelterIfAny(target);
        }
        if (Evacuation.STATUS_EVACUATED.equals(target.getStatus())) {
            releaseShelterIfAny(target);
        }
        restoreCitizenStatus(target);
        dataManager.data().getEvacuations().remove(target);
        dataManager.notifyChanged();
    }

    private void restoreCitizenStatus(Evacuation target) {
        Citizen citizen = (Citizen) dataManager.citizenById(target.getCitizenId());
        if (citizen == null) {
            return;
        }
        boolean stillActive = false;
        List<Evacuation> evacuations = dataManager.data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            Evacuation ev = evacuations.get(i);
            if (ev.getCitizenId().equals(citizen.getId()) && ev != target) {
                stillActive = true;
            }
        }
        if (!stillActive) {
            citizen.setEvacuationStatus(Citizen.EV_PENDING);
        }
    }

    private void releaseShelterIfAny(Evacuation ev) {
        if (ev.getShelterId() == null) {
            return;
        }
        Shelter shelter = (Shelter) dataManager.shelterById(ev.getShelterId());
        if (shelter != null) {
            shelter.release(ev.getPeopleCount());
        }
        ev.setShelterId(null);
    }

    public void setStatus(String evacuationId, String newStatus) throws AppException {
        Evacuation ev = byId(evacuationId);
        if (ev == null) {
            throw new AppException("Evacuation record does not exist.");
        }
        if (!Evacuation.STATUS_PENDING.equals(newStatus) && !Evacuation.STATUS_ASSIGNED.equals(newStatus)
                && !Evacuation.STATUS_EVACUATING.equals(newStatus) && !Evacuation.STATUS_EVACUATED.equals(newStatus)) {
            throw new AppException("Unknown evacuation status.");
        }
        String oldStatus = ev.getStatus();
        if (Evacuation.STATUS_ASSIGNED.equals(newStatus) && ev.getShelterId() == null) {
            throw new AppException("Assign a shelter before marking as Assigned.");
        }
        if (Evacuation.STATUS_EVACUATED.equals(newStatus) && ev.getShelterId() == null) {
            throw new AppException("Assign a shelter before marking as Evacuated.");
        }
        if (!Evacuation.STATUS_EVACUATED.equals(oldStatus) && Evacuation.STATUS_EVACUATED.equals(newStatus)) {
            Citizen citizen = (Citizen) dataManager.citizenById(ev.getCitizenId());
            if (citizen != null) {
                citizen.setEvacuationStatus(Citizen.EV_EVACUATED);
            }
        }
        if (Evacuation.STATUS_EVACUATED.equals(oldStatus) && !Evacuation.STATUS_EVACUATED.equals(newStatus)) {
            Citizen citizen = (Citizen) dataManager.citizenById(ev.getCitizenId());
            if (citizen != null) {
                citizen.setEvacuationStatus(newStatus.toLowerCase().startsWith("p") ? Citizen.EV_PENDING
                        : (Evacuation.STATUS_ASSIGNED.equals(newStatus) ? Citizen.EV_ASSIGNED : Citizen.EV_EVACUATING));
            }
        }
        ev.setStatus(newStatus);
        dataManager.notifyChanged();
    }

    public Evacuation assignShelter(String evacuationId, String shelterId) throws AppException {
        Evacuation ev = byId(evacuationId);
        if (ev == null) {
            throw new AppException("Evacuation record does not exist.");
        }
        dataManager.requireShelter(shelterId);
        if (Evacuation.STATUS_EVACUATED.equals(ev.getStatus())) {
            throw new AppException("This evacuation is already completed.");
        }
        Shelter shelter = (Shelter) dataManager.shelterById(shelterId);
        if (shelter.getAvailableCapacity() < ev.getPeopleCount()) {
            throw new AppException(shelter.getName() + " has only " + shelter.getAvailableCapacity()
                    + " place(s) free; " + ev.getPeopleCount() + " required.");
        }
        releaseShelterIfAny(ev);
        shelter.admit(ev.getPeopleCount());
        ev.setShelterId(shelterId);
        if (Evacuation.STATUS_PENDING.equals(ev.getStatus())) {
            ev.setStatus(Evacuation.STATUS_ASSIGNED);
        }
        Citizen citizen = (Citizen) dataManager.citizenById(ev.getCitizenId());
        if (citizen != null && !Citizen.EV_EVACUATED.equals(citizen.getEvacuationStatus())) {
            citizen.setEvacuationStatus(Citizen.EV_ASSIGNED);
        }
        dataManager.notifyChanged();
        return ev;
    }

    public String autoAssignShelter(String evacuationId) throws AppException {
        Evacuation ev = byId(evacuationId);
        if (ev == null) {
            throw new AppException("Evacuation record does not exist.");
        }
        Shelter best = findBestShelter(ev);
        if (best == null) {
            throw new AppException("No reachable shelter has " + ev.getPeopleCount()
                    + " free place(s). Add capacity or open another shelter.");
        }
        assignShelter(evacuationId, best.getId());
        return best.getName() + " (" + routeDescription(ev, best) + ")";
    }

    public Shelter findBestShelter(Evacuation ev) {
        Citizen citizen = (Citizen) dataManager.citizenById(ev.getCitizenId());
        String fromId = citizen == null ? null : citizen.getLocationId();
        if (fromId == null) {
            return null;
        }
        Graph graph = routeService.buildGraph();
        sdrs.algorithm.RouteFinder finder = new sdrs.algorithm.RouteFinder(graph);
        Shelter best = null;
        double bestCost = Double.MAX_VALUE;
        List<Shelter> shelters = dataManager.data().getShelters();
        for (int i = 0; i < shelters.size(); i++) {
            Shelter shelter = shelters.get(i);
            if (Shelter.STATUS_CLOSED.equals(shelter.getStatus())
                    || shelter.getAvailableCapacity() < ev.getPeopleCount()) {
                continue;
            }
            RouteResult route = finder.findRoute(fromId, shelter.getLocationId(), true);
            if (route.isReachable() && route.getTotalMinutes() < bestCost) {
                bestCost = route.getTotalMinutes();
                best = shelter;
            }
        }
        return best;
    }

    public String routeDescription(Evacuation ev, Shelter shelter) {
        Citizen citizen = (Citizen) dataManager.citizenById(ev.getCitizenId());
        if (citizen == null || citizen.getLocationId() == null) {
            return "route unknown";
        }
        RouteResult route = routeService.findRoute(citizen.getLocationId(), shelter.getLocationId(), true);
        if (!route.isReachable()) {
            return "no open road route";
        }
        return String.format("%.1f km, %.0f min", Double.valueOf(route.getTotalDistanceKm()),
                Double.valueOf(route.getTotalMinutes()));
    }

    public List<String> autoAssignAllPending() {
        List<String> log = new ArrayList<String>();
        java.util.PriorityQueue<PrioritizableView> queue =
                new java.util.PriorityQueue<PrioritizableView>();
        List<Evacuation> evacuations = dataManager.data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            Evacuation ev = evacuations.get(i);
            if (!Evacuation.STATUS_PENDING.equals(ev.getStatus()) || ev.getShelterId() != null) {
                continue;
            }
            Citizen citizen = (Citizen) dataManager.citizenById(ev.getCitizenId());
            if (citizen != null) {
                queue.add(new PrioritizableView(citizen.getPriorityScore(), ev.getId()));
            }
        }
        while (!queue.isEmpty()) {
            PrioritizableView view = queue.poll();
            try {
                String shelterName = autoAssignShelter(view.evacuationId);
                log.add("Evacuation " + view.evacuationId + " assigned to " + shelterName);
            } catch (AppException ex) {
                log.add("Evacuation " + view.evacuationId + " NOT assigned: " + ex.getMessage());
            }
        }
        return log;
    }

    public List<Evacuation> allEvacuations() {
        return new ArrayList<Evacuation>(dataManager.data().getEvacuations());
    }

    public List<Evacuation> byStatus(String status) {
        List<Evacuation> result = new ArrayList<Evacuation>();
        List<Evacuation> evacuations = dataManager.data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            if (evacuations.get(i).getStatus().equals(status)) {
                result.add(evacuations.get(i));
            }
        }
        return result;
    }

    public List<Evacuation> forDisaster(String disasterId) {
        List<Evacuation> result = new ArrayList<Evacuation>();
        List<Evacuation> evacuations = dataManager.data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            if (evacuations.get(i).getDisasterId().equals(disasterId)) {
                result.add(evacuations.get(i));
            }
        }
        return result;
    }

    public Evacuation byId(String id) {
        List<Evacuation> evacuations = dataManager.data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            if (evacuations.get(i).getId().equals(id)) {
                return evacuations.get(i);
            }
        }
        return null;
    }

    public List<Citizen> affectedCitizens(Disaster disaster) {
        List<Citizen> result = new ArrayList<Citizen>();
        List<String> ids = disaster.getCitizenIds();
        for (int i = 0; i < ids.size(); i++) {
            Citizen citizen = (Citizen) dataManager.citizenById(ids.get(i));
            if (citizen != null) {
                result.add(citizen);
            }
        }
        return result;
    }

    private static class PrioritizableView implements Comparable<PrioritizableView> {

        private final int score;
        private final String evacuationId;

        PrioritizableView(int score, String evacuationId) {
            this.score = score;
            this.evacuationId = evacuationId;
        }

        public int compareTo(PrioritizableView other) {
            return other.score - score;
        }
    }
}
