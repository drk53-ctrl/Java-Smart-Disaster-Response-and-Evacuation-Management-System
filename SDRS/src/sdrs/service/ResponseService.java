package sdrs.service;

import java.util.ArrayList;
import java.util.List;

import sdrs.algorithm.DisasterPriorityQueue;
import sdrs.algorithm.RouteResult;
import sdrs.model.Citizen;
import sdrs.model.Disaster;
import sdrs.model.EmergencyTeam;
import sdrs.model.Evacuation;
import sdrs.model.Resource;
import sdrs.model.Shelter;
import sdrs.model.Vehicle;

public class ResponseService {

    private final DataManager dataManager;
    private final DisasterService disasterService;
    private final EvacuationService evacuationService;
    private final RouteService routeService;
    private final ResourceService resourceService;

    public ResponseService(DataManager dataManager, DisasterService disasterService,
                           EvacuationService evacuationService, RouteService routeService,
                           ResourceService resourceService) {
        this.dataManager = dataManager;
        this.disasterService = disasterService;
        this.evacuationService = evacuationService;
        this.routeService = routeService;
        this.resourceService = resourceService;
    }

    public List<Disaster> triageQueue() {
        DisasterPriorityQueue queue = new DisasterPriorityQueue();
        List<Disaster> disasters = dataManager.data().getDisasters();
        for (int i = 0; i < disasters.size(); i++) {
            if (disasters.get(i).isActiveLike()) {
                queue.insert(disasters.get(i));
            }
        }
        return queue.toSortedList();
    }

    public List<Citizen> citizensByPriority(Disaster disaster) {
        List<Citizen> affected = evacuationService.affectedCitizens(disaster);
        List<Citizen> sorted = new ArrayList<Citizen>();
        List<Integer> scores = new ArrayList<Integer>();
        for (int i = 0; i < affected.size(); i++) {
            Citizen citizen = affected.get(i);
            int score = citizen.getPriorityScore();
            int pos = 0;
            while (pos < sorted.size() && scores.get(pos) >= score) {
                pos++;
            }
            sorted.add(pos, citizen);
            scores.add(pos, Integer.valueOf(score));
        }
        return sorted;
    }

    public RouteResult recommendedRoute(Disaster disaster) {
        String startId = emergencyCenterId();
        if (startId == null || disaster.getLocationId() == null) {
            return null;
        }
        return routeService.findRoute(startId, disaster.getLocationId(), true);
    }

    public String emergencyCenterId() {
        List<sdrs.model.Location> locations = dataManager.data().getLocations();
        for (int i = 0; i < locations.size(); i++) {
            if (sdrs.model.Location.TYPE_EMERGENCY_CENTER.equals(locations.get(i).getType())) {
                return locations.get(i).getId();
            }
        }
        if (locations.isEmpty()) {
            return null;
        }
        return locations.get(0).getId();
    }

    public List<Resource> recommendedResources(Disaster disaster) {
        List<Resource> ranked = new ArrayList<Resource>();
        List<Resource> all = dataManager.data().getResources();
        String needKey = typeToResourceNeed(disaster.getType());
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < all.size(); i++) {
                Resource resource = all.get(i);
                boolean relevant = needKey != null && resource.getType().equals(needKey);
                if (pass == 0 && relevant && resource.getQuantity() > 0) {
                    ranked.add(resource);
                }
                if (pass == 1 && !ranked.contains(resource) && resource.getQuantity() > 0) {
                    ranked.add(resource);
                }
            }
        }
        return ranked;
    }

    private String typeToResourceNeed(String disasterType) {
        if (Disaster.TYPE_FLOOD.equals(disasterType) || Disaster.TYPE_CYCLONE.equals(disasterType)
                || Disaster.TYPE_TSUNAMI.equals(disasterType)) {
            return Resource.TYPE_WATER;
        }
        if (Disaster.TYPE_FIRE.equals(disasterType)) {
            return Resource.TYPE_RESCUE_EQUIPMENT;
        }
        if (Disaster.TYPE_EARTHQUAKE.equals(disasterType) || Disaster.TYPE_LANDSLIDE.equals(disasterType)) {
            return Resource.TYPE_MEDICAL_KIT;
        }
        return null;
    }

    public List<Shelter> sheltersForDisaster(Disaster disaster) {
        List<Shelter> result = new ArrayList<Shelter>();
        List<Evacuation> evacuations = evacuationService.forDisaster(disaster.getId());
        for (int i = 0; i < evacuations.size(); i++) {
            Evacuation ev = evacuations.get(i);
            if (ev.getShelterId() == null) {
                continue;
            }
            Shelter shelter = (Shelter) dataManager.shelterById(ev.getShelterId());
            if (shelter != null && !result.contains(shelter)) {
                result.add(shelter);
            }
        }
        return result;
    }

    public List<EmergencyTeam> teamsForDisaster(Disaster disaster) {
        List<EmergencyTeam> result = new ArrayList<EmergencyTeam>();
        for (int i = 0; i < disaster.getTeamIds().size(); i++) {
            EmergencyTeam team = (EmergencyTeam) dataManager.teamById(disaster.getTeamIds().get(i));
            if (team != null) {
                result.add(team);
            }
        }
        return result;
    }

    public List<Vehicle> vehiclesForDisaster(Disaster disaster) {
        List<Vehicle> result = new ArrayList<Vehicle>();
        for (int i = 0; i < disaster.getVehicleIds().size(); i++) {
            Vehicle vehicle = (Vehicle) dataManager.vehicleById(disaster.getVehicleIds().get(i));
            if (vehicle != null) {
                result.add(vehicle);
            }
        }
        return result;
    }

    public String coordinationSummary(Disaster disaster) {
        StringBuilder sb = new StringBuilder();
        sb.append(disaster.getId()).append(" - ").append(disaster.getType())
                .append(" at ").append(disaster.getLocationName())
                .append(" [").append(disaster.getSeverity()).append(" / ").append(disaster.getStatus()).append("]\n");
        sb.append("Priority score: ").append(disaster.getPriorityScore())
                .append(" (rank ").append(triageRank(disaster)).append(" of ")
                .append(triageQueue().size()).append(" active)\n");
        sb.append("Affected citizens: ").append(evacuationService.affectedCitizens(disaster).size())
                .append(" | Teams: ").append(teamsForDisaster(disaster).size())
                .append(" | Vehicles: ").append(vehiclesForDisaster(disaster).size()).append("\n");
        List<Evacuation> evacuations = evacuationService.forDisaster(disaster.getId());
        int pending = 0;
        int evacuated = 0;
        for (int i = 0; i < evacuations.size(); i++) {
            if (Evacuation.STATUS_EVACUATED.equals(evacuations.get(i).getStatus())) {
                evacuated++;
            } else {
                pending++;
            }
        }
        sb.append("Evacuations: ").append(evacuated).append(" completed, ")
                .append(pending).append(" in progress\n");
        RouteResult route = recommendedRoute(disaster);
        if (route != null) {
            sb.append("Recommended route from ").append(routeService.locationName(emergencyCenterId()))
                    .append(": ").append(route.summary(true));
        } else {
            sb.append("Recommended route: unavailable (set disaster location in Route Planning).");
        }
        return sb.toString();
    }

    private int triageRank(Disaster disaster) {
        List<Disaster> ranked = triageQueue();
        for (int i = 0; i < ranked.size(); i++) {
            if (ranked.get(i).getId().equals(disaster.getId())) {
                return i + 1;
            }
        }
        return 0;
    }
}
