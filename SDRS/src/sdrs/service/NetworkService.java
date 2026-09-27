package sdrs.service;

import sdrs.model.Location;
import sdrs.model.RoadSegment;
import sdrs.util.AppException;
import sdrs.util.Validators;

public class NetworkService {

    private final DataManager dataManager;

    public NetworkService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Location addLocation(String name, String type) throws AppException {
        Validators.requireText(name, "Location name");
        Validators.requireSelection(type, "location type");
        int count = dataManager.data().getLocations().size();
        double angle = (Math.PI * 2 * count) / Math.max(6, count + 2);
        double radius = 34.0;
        double x = 50 + radius * Math.cos(angle);
        double y = 50 + radius * Math.sin(angle);
        Location location = new Location(dataManager.nextId("LOC"), name.trim(), type, x, y);
        dataManager.addLocation(location);
        dataManager.notifyChanged();
        return location;
    }

    public void deleteLocation(String locationId) throws AppException {
        dataManager.requireLocation(locationId);
        java.util.List<RoadSegment> connected = roadsTouching(locationId);
        if (!connected.isEmpty()) {
            throw new AppException("Remove the " + connected.size() + " road(s) connected to this location first.");
        }
        dataManager.removeLocation(locationId);
        dataManager.notifyChanged();
    }

    public RoadSegment addRoad(String fromId, String toId, String distanceText, String timeText,
                               String name) throws AppException {
        Validators.requireSelection(fromId, "start location");
        Validators.requireSelection(toId, "end location");
        if (fromId.equals(toId)) {
            throw new AppException("A road cannot connect a location to itself.");
        }
        double distance = parseDouble(distanceText, "Distance (km)");
        double minutes = parseDouble(timeText, "Travel time (min)");
        if (distance <= 0 || distance > 5000) {
            throw new AppException("Distance must be between 0 and 5000 km.");
        }
        if (minutes <= 0 || minutes > 5000) {
            throw new AppException("Travel time must be between 0 and 5000 minutes.");
        }
        Validators.requireText(name, "Road name");
        if (findRoad(fromId, toId, name.trim()) != null) {
            throw new AppException("A road with this name already connects these locations.");
        }
        RoadSegment road = new RoadSegment(fromId, toId, distance, minutes, name.trim());
        dataManager.addRoad(road);
        dataManager.notifyChanged();
        return road;
    }

    public void setRoadStatus(RoadSegment road, String status) {
        road.setStatus(status);
        dataManager.notifyChanged();
    }

    public void deleteRoad(RoadSegment road) {
        dataManager.removeRoad(road);
        dataManager.notifyChanged();
    }

    public RoadSegment findRoad(String fromId, String toId, String name) {
        java.util.List<RoadSegment> roads = dataManager.data().getRoads();
        for (int i = 0; i < roads.size(); i++) {
            RoadSegment road = roads.get(i);
            boolean direct = road.getFromId().equals(fromId) && road.getToId().equals(toId);
            boolean reverse = road.getFromId().equals(toId) && road.getToId().equals(fromId);
            if ((direct || reverse) && road.getName().equalsIgnoreCase(name)) {
                return road;
            }
        }
        return null;
    }

    public java.util.List<RoadSegment> criticalRoadsFor(String startId, RoadSegment candidate) {
        sdrs.algorithm.Graph graph = new sdrs.algorithm.Graph();
        java.util.List<RoadSegment> roads = dataManager.data().getRoads();
        for (int i = 0; i < roads.size(); i++) {
            RoadSegment road = roads.get(i);
            if (!road.isBlocked()) {
                graph.addEdge(road);
            }
        }
        java.util.List<String> before = graph.bfsReachable(startId);
        graph.removeEdge(candidate);
        java.util.List<String> after = graph.bfsReachable(startId);
        java.util.List<RoadSegment> lost = new java.util.ArrayList<RoadSegment>();
        for (int i = 0; i < before.size(); i++) {
            if (!after.contains(before.get(i))) {
                lost.add(candidate);
                break;
            }
        }
        return lost;
    }

    public java.util.List<RoadSegment> roadsTouching(String locationId) {
        java.util.List<RoadSegment> result = new java.util.ArrayList<RoadSegment>();
        java.util.List<RoadSegment> roads = dataManager.data().getRoads();
        for (int i = 0; i < roads.size(); i++) {
            RoadSegment road = roads.get(i);
            if (road.getFromId().equals(locationId) || road.getToId().equals(locationId)) {
                result.add(road);
            }
        }
        return result;
    }

    private double parseDouble(String text, String label) throws AppException {
        Validators.requireText(text, label);
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException ex) {
            throw new AppException(label + " must be a number.");
        }
    }
}
