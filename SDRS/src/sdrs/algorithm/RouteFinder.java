package sdrs.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sdrs.model.RoadSegment;

public class RouteFinder {

    private final Graph graph;

    public RouteFinder(Graph graph) {
        this.graph = graph;
    }

    public RouteResult findRoute(String startId, String goalId, boolean byTime) {
        List<String> empty = new ArrayList<String>();
        if (startId == null || goalId == null || startId.equals(goalId)) {
            return new RouteResult(empty, new ArrayList<RouteStep>(), 0, 0, false);
        }

        List<String> settled = new ArrayList<String>();
        Map<String, Double> best = new HashMap<String, Double>();
        Map<String, String> previous = new HashMap<String, String>();
        best.put(startId, Double.valueOf(0));

        while (true) {
            String current = pickCheapestUnsettled(best, settled);
            if (current == null) {
                break;
            }
            if (current.equals(goalId)) {
                break;
            }
            settled.add(current);
            double costSoFar = best.get(current).doubleValue();
            List<RoadSegment> links = graph.neighbors(current);
            for (int i = 0; i < links.size(); i++) {
                RoadSegment road = links.get(i);
                if (road.isBlocked()) {
                    continue;
                }
                String next = road.otherEnd(current);
                double candidate = costSoFar + road.cost(byTime);
                Double known = best.get(next);
                if (known == null || candidate < known.doubleValue()) {
                    best.put(next, Double.valueOf(candidate));
                    previous.put(next, current);
                }
            }
        }

        if (!best.containsKey(goalId)) {
            return new RouteResult(empty, new ArrayList<RouteStep>(), 0, 0, false);
        }

        List<String> pathIds = new ArrayList<String>();
        String walk = goalId;
        pathIds.add(walk);
        while (!walk.equals(startId)) {
            walk = previous.get(walk);
            pathIds.add(0, walk);
        }

        List<RouteStep> steps = new ArrayList<RouteStep>();
        double totalKm = 0;
        double totalMin = 0;
        for (int i = 1; i < pathIds.size(); i++) {
            RoadSegment road = bestRoad(pathIds.get(i - 1), pathIds.get(i), byTime);
            steps.add(new RouteStep(pathIds.get(i), "", road.getDistanceKm(), road.getTravelMinutes()));
            totalKm += road.getDistanceKm();
            totalMin += road.getTravelMinutes();
        }
        return new RouteResult(pathIds, steps, totalKm, totalMin, true);
    }

    private String pickCheapestUnsettled(Map<String, Double> best, List<String> settled) {
        String chosen = null;
        double cheapest = Double.MAX_VALUE;
        for (Map.Entry<String, Double> entry : best.entrySet()) {
            if (!settled.contains(entry.getKey()) && entry.getValue().doubleValue() < cheapest) {
                cheapest = entry.getValue().doubleValue();
                chosen = entry.getKey();
            }
        }
        return chosen;
    }

    private RoadSegment bestRoad(String fromId, String toId, boolean byTime) {
        RoadSegment best = null;
        List<RoadSegment> links = graph.neighbors(fromId);
        for (int i = 0; i < links.size(); i++) {
            RoadSegment road = links.get(i);
            if (road.isBlocked() || !road.otherEnd(fromId).equals(toId)) {
                continue;
            }
            if (best == null || road.cost(byTime) < best.cost(byTime)) {
                best = road;
            }
        }
        return best;
    }
}
