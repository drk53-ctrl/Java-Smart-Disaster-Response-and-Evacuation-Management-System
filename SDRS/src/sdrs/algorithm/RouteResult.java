package sdrs.algorithm;

import java.util.List;

public class RouteResult {

    private final List<String> pathIds;
    private final List<RouteStep> steps;
    private final double totalDistanceKm;
    private final double totalMinutes;
    private final boolean reachable;

    public RouteResult(List<String> pathIds, List<RouteStep> steps, double totalDistanceKm,
                       double totalMinutes, boolean reachable) {
        this.pathIds = pathIds;
        this.steps = steps;
        this.totalDistanceKm = totalDistanceKm;
        this.totalMinutes = totalMinutes;
        this.reachable = reachable;
    }

    public List<String> getPathIds() {
        return pathIds;
    }

    public List<RouteStep> getSteps() {
        return steps;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public double getTotalMinutes() {
        return totalMinutes;
    }

    public boolean isReachable() {
        return reachable;
    }

    public String summary(boolean byTime) {
        if (!reachable) {
            return "No route available (blocked roads may isolate the destination).";
        }
        String mode = byTime ? "fastest" : "shortest";
        return mode + " route: " + pathIds.size() + " stops, "
                + String.format("%.1f", totalDistanceKm) + " km, "
                + String.format("%.0f", totalMinutes) + " min";
    }
}
