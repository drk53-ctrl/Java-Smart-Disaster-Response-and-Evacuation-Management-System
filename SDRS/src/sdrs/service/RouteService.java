package sdrs.service;

import java.util.ArrayList;
import java.util.List;

import sdrs.algorithm.Graph;
import sdrs.algorithm.RouteFinder;
import sdrs.algorithm.RouteResult;
import sdrs.algorithm.RouteStep;
import sdrs.model.Disaster;
import sdrs.model.Location;
import sdrs.model.RoadSegment;
import sdrs.model.Shelter;

public class RouteService {

    private final DataManager dataManager;
    private NetworkService networkService;

    public RouteService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void setNetwork(NetworkService networkService) {
        this.networkService = networkService;
    }

    public NetworkService network() {
        return networkService;
    }

    public Graph buildGraph() {
        Graph graph = new Graph();
        List<RoadSegment> roads = dataManager.data().getRoads();
        for (int i = 0; i < roads.size(); i++) {
            graph.addEdge(roads.get(i));
        }
        return graph;
    }

    public RouteResult findRoute(String startId, String goalId, boolean byTime) {
        RouteFinder finder = new RouteFinder(buildGraph());
        RouteResult result = finder.findRoute(startId, goalId, byTime);
        return withNames(result);
    }

    public List<RouteResult> alternativeRoutes(String startId, String goalId, boolean byTime, int limit) {
        Graph graph = buildGraph();
        RouteFinder finder = new RouteFinder(graph);
        RouteResult primary = withNames(finder.findRoute(startId, goalId, byTime));
        List<RouteResult> results = new ArrayList<RouteResult>();
        if (primary.isReachable()) {
            results.add(primary);
        }
        Graph graph2 = graph;
        List<List<String>> paths = graph2.enumeratePaths(startId, goalId, 40);
        for (int i = 0; i < paths.size() && results.size() < limit; i++) {
            List<String> path = paths.get(i);
            if (primary.isReachable() && path.equals(primary.getPathIds())) {
                continue;
            }
            results.add(withNames(pathToResult(graph2, path)));
        }
        return results;
    }

    private RouteResult pathToResult(Graph graph, List<String> path) {
        List<RouteStep> steps = new ArrayList<RouteStep>();
        double km = 0;
        double min = 0;
        for (int i = 1; i < path.size(); i++) {
            RoadSegment road = bestEdge(graph, path.get(i - 1), path.get(i));
            steps.add(new RouteStep(path.get(i), locationName(path.get(i)), road.getDistanceKm(),
                    road.getTravelMinutes()));
            km += road.getDistanceKm();
            min += road.getTravelMinutes();
        }
        return new RouteResult(new ArrayList<String>(path), steps, km, min, !path.isEmpty());
    }

    private RoadSegment bestEdge(Graph graph, String fromId, String toId) {
        RoadSegment best = null;
        List<RoadSegment> links = graph.neighbors(fromId);
        for (int i = 0; i < links.size(); i++) {
            RoadSegment road = links.get(i);
            if (road.isBlocked() || !road.otherEnd(fromId).equals(toId)) {
                continue;
            }
            if (best == null || road.getDistanceKm() < best.getDistanceKm()) {
                best = road;
            }
        }
        return best;
    }

    public List<String> blockedRoadImpact(String startId) {
        return buildGraph().bfsReachable(startId);
    }

    public List<sdrs.model.RoadSegment> criticalRoads(String startId) {
        return buildGraph().criticalRoads(startId);
    }

    private RouteResult withNames(RouteResult result) {
        if (!result.isReachable()) {
            return result;
        }
        List<RouteStep> named = new ArrayList<RouteStep>();
        List<RouteStep> steps = result.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            RouteStep step = steps.get(i);
            named.add(new RouteStep(step.getLocationId(), locationName(step.getLocationId()),
                    step.getDistanceKm(), step.getTravelMinutes()));
        }
        return new RouteResult(result.getPathIds(), named, result.getTotalDistanceKm(),
                result.getTotalMinutes(), true);
    }

    public String locationName(String locationId) {
        Location location = (Location) dataManager.locationById(locationId);
        if (location == null) {
            return locationId;
        }
        return location.getName();
    }

    public List<Location> allLocations() {
        return new ArrayList<Location>(dataManager.data().getLocations());
    }

    public List<RoadSegment> roadsTouching(String locationId) {
        List<RoadSegment> result = new ArrayList<RoadSegment>();
        List<RoadSegment> roads = dataManager.data().getRoads();
        for (int i = 0; i < roads.size(); i++) {
            RoadSegment road = roads.get(i);
            if (road.getFromId().equals(locationId) || road.getToId().equals(locationId)) {
                result.add(road);
            }
        }
        return result;
    }

    public List<String> activeDisasterIdsAt(String locationId) {
        List<String> result = new ArrayList<String>();
        List<Disaster> disasters = dataManager.data().getDisasters();
        for (int i = 0; i < disasters.size(); i++) {
            Disaster disaster = disasters.get(i);
            if (locationId.equals(disaster.getLocationId()) && disaster.isActiveLike()) {
                result.add(disaster.getId());
            }
        }
        return result;
    }

    public List<Shelter> sheltersAt(String locationId) {
        List<Shelter> result = new ArrayList<Shelter>();
        List<Shelter> shelters = dataManager.data().getShelters();
        for (int i = 0; i < shelters.size(); i++) {
            if (locationId.equals(shelters.get(i).getLocationId())) {
                result.add(shelters.get(i));
            }
        }
        return result;
    }

    public List<sdrs.model.EmergencyTeam> teamsAt(String locationId) {
        List<sdrs.model.EmergencyTeam> result = new ArrayList<sdrs.model.EmergencyTeam>();
        List<sdrs.model.EmergencyTeam> teams = dataManager.data().getTeams();
        for (int i = 0; i < teams.size(); i++) {
            if (locationId.equals(teams.get(i).getLocationId())) {
                result.add(teams.get(i));
            }
        }
        return result;
    }

    public List<sdrs.model.Vehicle> vehiclesAt(String locationId) {
        List<sdrs.model.Vehicle> result = new ArrayList<sdrs.model.Vehicle>();
        List<sdrs.model.Vehicle> vehicles = dataManager.data().getVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            if (locationId.equals(vehicles.get(i).getLocationId())) {
                result.add(vehicles.get(i));
            }
        }
        return result;
    }

    public List<Shelter> nearestShelters(String fromId, int peopleNeeded, boolean byTime, int limit) {
        List<Shelter> candidates = new ArrayList<Shelter>();
        List<Shelter> shelters = dataManager.data().getShelters();
        for (int i = 0; i < shelters.size(); i++) {
            Shelter shelter = shelters.get(i);
            if (Shelter.STATUS_CLOSED.equals(shelter.getStatus())
                    || shelter.getAvailableCapacity() < peopleNeeded) {
                continue;
            }
            RouteResult route = findRoute(fromId, shelter.getLocationId(), byTime);
            if (route.isReachable()) {
                candidates.add(shelter);
            }
        }
        List<Shelter> sorted = new ArrayList<Shelter>();
        List<Double> costs = new ArrayList<Double>();
        for (int i = 0; i < candidates.size(); i++) {
            Shelter shelter = candidates.get(i);
            RouteResult route = findRoute(fromId, shelter.getLocationId(), byTime);
            double cost = byTime ? route.getTotalMinutes() : route.getTotalDistanceKm();
            int pos = 0;
            while (pos < sorted.size() && costs.get(pos).doubleValue() <= cost) {
                pos++;
            }
            sorted.add(pos, shelter);
            costs.add(pos, Double.valueOf(cost));
        }
        if (sorted.size() > limit) {
            sorted = new ArrayList<Shelter>(sorted.subList(0, limit));
        }
        return sorted;
    }

    public List<RoadSegment> allRoads() {
        return new ArrayList<RoadSegment>(dataManager.data().getRoads());
    }
}
