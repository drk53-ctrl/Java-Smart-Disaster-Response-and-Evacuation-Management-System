package sdrs.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sdrs.model.RoadSegment;

public class Graph {

    private final Map<String, List<RoadSegment>> adjacency = new HashMap<String, List<RoadSegment>>();
    private final List<RoadSegment> edges = new ArrayList<RoadSegment>();

    public void addEdge(RoadSegment road) {
        edges.add(road);
        addHalf(road.getFromId(), road);
        addHalf(road.getToId(), road);
    }

    public void removeEdge(RoadSegment road) {
        edges.remove(road);
        removeHalf(road.getFromId(), road);
        removeHalf(road.getToId(), road);
    }

    private void addHalf(String nodeId, RoadSegment road) {
        List<RoadSegment> list = adjacency.get(nodeId);
        if (list == null) {
            list = new ArrayList<RoadSegment>();
            adjacency.put(nodeId, list);
        }
        list.add(road);
    }

    private void removeHalf(String nodeId, RoadSegment road) {
        List<RoadSegment> list = adjacency.get(nodeId);
        if (list != null) {
            list.remove(road);
        }
    }

    public List<RoadSegment> neighbors(String locationId) {
        List<RoadSegment> list = adjacency.get(locationId);
        if (list == null) {
            return new ArrayList<RoadSegment>();
        }
        return new ArrayList<RoadSegment>(list);
    }

    public List<RoadSegment> getEdges() {
        return new ArrayList<RoadSegment>(edges);
    }

    public boolean areConnected(String fromId, String toId) {
        return bfsReachable(fromId).contains(toId);
    }

    public List<String> bfsReachable(String startId) {
        List<String> visited = new ArrayList<String>();
        if (!adjacency.containsKey(startId)) {
            return visited;
        }
        List<String> frontier = new ArrayList<String>();
        frontier.add(startId);
        visited.add(startId);
        while (!frontier.isEmpty()) {
            String current = frontier.remove(0);
            List<RoadSegment> links = adjacency.get(current);
            if (links == null) {
                continue;
            }
            for (int i = 0; i < links.size(); i++) {
                RoadSegment road = links.get(i);
                if (road.isBlocked()) {
                    continue;
                }
                String next = road.otherEnd(current);
                if (!visited.contains(next)) {
                    visited.add(next);
                    frontier.add(next);
                }
            }
        }
        return visited;
    }

    public List<RoadSegment> criticalRoads(String startId) {
        List<String> reachable = bfsReachable(startId);
        List<RoadSegment> critical = new ArrayList<RoadSegment>();
        for (int i = 0; i < edges.size(); i++) {
            RoadSegment road = edges.get(i);
            if (road.isBlocked()) {
                continue;
            }
            if (reachable.contains(road.getFromId()) && reachable.contains(road.getToId())) {
                Graph copy = copyWithout(road);
                if (!copy.bfsReachable(startId).containsAll(reachable)) {
                    critical.add(road);
                }
            }
        }
        return critical;
    }

    private Graph copyWithout(RoadSegment excluded) {
        Graph copy = new Graph();
        for (int i = 0; i < edges.size(); i++) {
            RoadSegment road = edges.get(i);
            if (road != excluded && !road.isBlocked()) {
                copy.addEdge(road);
            }
        }
        return copy;
    }

    public List<List<String>> enumeratePaths(String startId, String goalId, int limit) {
        List<List<String>> results = new ArrayList<List<String>>();
        if (!adjacency.containsKey(startId) || !adjacency.containsKey(goalId)) {
            return results;
        }
        List<String> path = new ArrayList<String>();
        List<RoadSegment> usedRoads = new ArrayList<RoadSegment>();
        dfs(startId, goalId, path, usedRoads, results, limit);
        return results;
    }

    private void dfs(String current, String goal, List<String> path, List<RoadSegment> usedRoads,
                     List<List<String>> results, int limit) {
        path.add(current);
        if (current.equals(goal)) {
            results.add(new ArrayList<String>(path));
        } else {
            List<RoadSegment> links = adjacency.get(current);
            if (links != null && results.size() < limit) {
                for (int i = 0; i < links.size() && results.size() < limit; i++) {
                    RoadSegment road = links.get(i);
                    if (road.isBlocked()) {
                        continue;
                    }
                    String next = road.otherEnd(current);
                    if (!path.contains(next)) {
                        usedRoads.add(road);
                        dfs(next, goal, path, usedRoads, results, limit);
                        usedRoads.remove(usedRoads.size() - 1);
                    }
                }
            }
        }
        path.remove(path.size() - 1);
    }
}
