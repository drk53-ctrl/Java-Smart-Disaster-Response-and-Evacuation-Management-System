package sdrs.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sdrs.algorithm.DisasterPriorityQueue;
import sdrs.algorithm.Graph;
import sdrs.algorithm.RouteFinder;
import sdrs.algorithm.RouteResult;
import sdrs.algorithm.SortSearch;
import sdrs.data.DataStore;
import sdrs.data.SystemData;
import sdrs.model.Citizen;
import sdrs.model.Disaster;
import sdrs.model.EmergencyTeam;
import sdrs.model.Evacuation;
import sdrs.model.Location;
import sdrs.model.Resource;
import sdrs.model.RoadSegment;
import sdrs.model.Shelter;
import sdrs.model.Vehicle;
import sdrs.service.AppContext;
import sdrs.service.CitizenService;
import sdrs.service.DataManager;
import sdrs.service.DisasterService;
import sdrs.service.EvacuationService;
import sdrs.service.ResourceService;
import sdrs.service.RouteService;
import sdrs.model.Shelter;
import sdrs.model.User;
import sdrs.service.ShelterService;
import sdrs.service.TeamService;
import sdrs.service.VehicleService;
import sdrs.util.AppException;

public class TestHarness {

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) {
        TestHarness harness = new TestHarness();
        harness.run();
        System.out.println();
        System.out.println("RESULT: " + harness.passed + " passed, " + harness.failed + " failed");
        if (harness.failed > 0) {
            System.exit(1);
        }
    }

    public void run() {
        System.out.println("=== SDRS TEST HARNESS ===");
        testAlgorithms();
        testSortAndHeap();
        testUserRecords();
        testFullWorkflow();
        testValidation();
        testPersistence();
        System.out.println("=== DONE ===");
    }

    private void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    private void testAlgorithms() {
        System.out.println("-- Algorithms --");
        testMapIntegration();
    }

    private void testMapIntegration() {
        System.out.println("-- Map / graph integration --");
        DataManager dm = DataManager.forTest();
        RouteService routes = new RouteService(dm);
        routes.setNetwork(new sdrs.service.NetworkService(dm));

        int positioned = 0;
        List<Location> locations = routes.allLocations();
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).hasMapPosition()) {
                positioned++;
            }
        }
        check("All seed locations carry map coordinates", positioned == locations.size()
                && locations.size() >= 10);

        boolean coordinatesInRange = true;
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            if (location.getX() < Location.MAP_MIN || location.getX() > Location.MAP_MAX
                    || location.getY() < Location.MAP_MIN || location.getY() > Location.MAP_MAX) {
                coordinatesInRange = false;
            }
        }
        check("Coordinates within map bounds", coordinatesInRange);

        List<RoadSegment> touching = routes.roadsTouching("LOC-1");
        check("roadsTouching finds seeded roads", touching.size() >= 2);

        check("Disaster detected at Riverside Colony",
                routes.activeDisasterIdsAt("LOC-2").contains("DIS-1"));
        check("Shelter detected at Old Town Junction",
                routes.sheltersAt("LOC-10").size() == 1);

        List<Shelter> nearest = routes.nearestShelters("LOC-2", 1, true, 3);
        check("Nearest-shelter search returns ranked results", nearest.size() >= 1
                && nearest.size() <= 3);
        boolean ascending = true;
        double last = -1;
        for (int i = 0; i < nearest.size(); i++) {
            RouteResult route = routes.findRoute("LOC-2", nearest.get(i).getLocationId(), true);
            if (last >= 0 && route.getTotalMinutes() < last) {
                ascending = false;
            }
            last = route.getTotalMinutes();
        }
        check("Nearest shelters ordered by travel time", ascending);
        check("Nearest shelters exclude closed/full", nearest.get(0).getAvailableCapacity() > 0
                && !Shelter.STATUS_CLOSED.equals(nearest.get(0).getStatus()));

        Location added = null;
        try {
            added = routes.network().addLocation("Map Test Node", Location.TYPE_JUNCTION);
        } catch (AppException ex) {
            check("New location gets auto map position", false);
        }
        check("New location gets auto map position", added != null && added.hasMapPosition());
    }

    private void testSortAndHeap() {
        List<Integer> numbers = new ArrayList<Integer>();
        for (int i = 0; i < 20; i++) {
            numbers.add(Integer.valueOf((i * 37) % 101));
        }
        List<Object> boxed = new ArrayList<Object>(numbers);
        List<Object> sorted = SortSearch.mergeSort(boxed, new SortSearch.Key() {
            public double keyOf(Object item) {
                return ((Integer) item).doubleValue();
            }
        }, false);
        boolean sortedOk = true;
        for (int i = 1; i < sorted.size(); i++) {
            if (((Integer) sorted.get(i)).intValue() < ((Integer) sorted.get(i - 1)).intValue()) {
                sortedOk = false;
            }
        }
        check("MergeSort orders 20 numbers", sortedOk);

        List<String> ids = new ArrayList<String>();
        for (int i = 1; i <= 10; i++) {
            ids.add("ID-" + i);
        }
        check("BinarySearch finds existing", SortSearch.binarySearch(ids, "ID-7") == 6);
        check("BinarySearch reports missing", SortSearch.binarySearch(ids, "ID-99") == -1);

        DisasterPriorityQueue heap = new DisasterPriorityQueue();
        for (int i = 0; i < 5; i++) {
            Disaster d = new Disaster("T-" + i, Disaster.TYPE_FLOOD, "X", null,
                    i % 2 == 0 ? Disaster.SEVERITY_CRITICAL : Disaster.SEVERITY_LOW,
                    0, i * 100, "test");
            heap.insert(d);
        }
        Disaster top = heap.extractMax();
        check("Heap extracts max first", top != null && top.getPriorityScore() == 90);
        boolean descending = true;
        Disaster previous = null;
        while (!heap.isEmpty()) {
            Disaster current = heap.extractMax();
            if (previous != null && current.getPriorityScore() > previous.getPriorityScore()) {
                descending = false;
            }
            previous = current;
        }
        check("Heap keeps priority order", descending);
    }



    private void testUserRecords() {
        System.out.println("-- User records --");
        DataManager fresh = DataManager.forTest();
        boolean adminExists = fresh.userByUsername("admin") != null;
        check("Seed admin account exists", adminExists);
        sdrs.model.User admin = (sdrs.model.User) fresh.userByUsername("admin");
        boolean passwordHashed = admin.getPasswordHash() != null
                && !admin.getPasswordHash().equals("admin123");
        check("Passwords stored as SHA-256 hash, not plain text", passwordHashed);
        User created = null;
        try {
            created = new User("USR-T1", "tester", sdrs.util.PasswordUtil.hash("test123"),
                    "Test User", "9000000099", User.ROLE_DMO);
            fresh.addUser(created);
            fresh.notifyChanged();
        } catch (Exception ex) {
            created = null;
        }
        check("User record can be created", created != null
                && fresh.userByUsername("tester") != null);
        fresh.removeUser("tester");
        fresh.notifyChanged();
        check("User record can be deleted", fresh.userByUsername("tester") == null);
    }

    private void testFullWorkflow() {
        System.out.println("-- Full demo workflow --");
        DataManager dm = DataManager.forTest();
        RouteService routes = new RouteService(dm);
        sdrs.service.NetworkService networkSvc = new sdrs.service.NetworkService(dm);
        routes.setNetwork(networkSvc);
        ShelterService shelters = new ShelterService(dm);
        DisasterService disasters = new DisasterService(dm);
        CitizenService citizens = new CitizenService(dm);
        TeamService teams = new TeamService(dm);
        VehicleService vehicles = new VehicleService(dm);
        ResourceService resources = new ResourceService(dm);
        EvacuationService evacuations = new EvacuationService(dm, routes, shelters);

        try {
            Disaster cyclone = disasters.reportDisaster(Disaster.TYPE_CYCLONE, "Coastal Belt",
                    "LOC-3", Disaster.SEVERITY_HIGH, "600", "Cyclone making landfall near the coast.");
            check("Disaster created", cyclone != null && disasters.allDisasters().size() == 2);

            Citizen extra = citizens.addCitizen("Test Family", "40", "9876500011", "Coastal Belt",
                    "LOC-3", "4", Citizen.PRIORITY_HIGH, "", "", "", "");
            check("Citizen created", extra != null);

            boolean dupRejected = false;
            try {
                citizens.addCitizen("Dup", "30", "9876500011", "X", "LOC-2", "1",
                        Citizen.PRIORITY_LOW, "", "", "", "");
            } catch (AppException ex) {
                dupRejected = true;
            }
            check("Duplicate phone rejected", dupRejected);

            cyclone.getCitizenIds().add("CIT-1");
            cyclone.getCitizenIds().add("CIT-2");
            cyclone.getCitizenIds().add(extra.getId());

            List<Evacuation> created = new ArrayList<Evacuation>();
            for (int i = 0; i < cyclone.getCitizenIds().size(); i++) {
                created.add(evacuations.createEvacuation(cyclone.getCitizenIds().get(i),
                        cyclone.getId(), "2"));
            }
            check("Evacuations created", created.size() == 3);

            teams.assignToDisaster("TEAM-3", cyclone.getId());
            vehicles.assignToDisaster("VEH-4", cyclone.getId());
            resources.allocate("RES-2", cyclone.getId(), "100", "tester");
            check("Team assigned and marked busy",
                    !((EmergencyTeam) dm.teamById("TEAM-3")).isAvailable());
            check("Vehicle assigned", !((Vehicle) dm.vehicleById("VEH-4")).isAvailable());
            check("Resource stock reduced", ((Resource) dm.resourceById("RES-2")).getQuantity() == 700);

            boolean overAllocateRejected = false;
            try {
                resources.allocate("RES-3", cyclone.getId(), "100000", "tester");
            } catch (AppException ex) {
                overAllocateRejected = true;
            }
            check("Over-allocation rejected", overAllocateRejected);

            String best = evacuations.autoAssignShelter(created.get(0).getId());
            check("Auto-assign picks reachable shelter",
                    best != null && (best.contains("Shelter") || best.contains("Hall")));

            List<String> log = evacuations.autoAssignAllPending();
            check("Auto-assign-all processed queue", log.size() >= 2);

            Evacuation first = created.get(0);
            evacuations.setStatus(first.getId(), Evacuation.STATUS_EVACUATING);
            evacuations.setStatus(first.getId(), Evacuation.STATUS_EVACUATED);
            check("Citizen marked evacuated", Citizen.EV_EVACUATED.equals(
                    ((Citizen) dm.citizenById(first.getCitizenId())).getEvacuationStatus()));
            check("Shelter occupancy increased",
                    ((Shelter) dm.shelterById(first.getShelterId())).getCurrentOccupancy() >= 2);

            RouteResult route = routes.findRoute("LOC-1", "LOC-3", false);
            check("Dijkstra route found", route.isReachable() && route.getTotalDistanceKm() > 0);
            check("Route ends at destination",
                    route.getPathIds().get(route.getPathIds().size() - 1).equals("LOC-3"));

            List<RouteResult> alternatives = routes.alternativeRoutes("LOC-1", "LOC-3", false, 3);
            check("Alternative routes produced", alternatives.size() >= 1);

            RoadSegment hospitalRoad = null;
            for (RoadSegment road : routes.allRoads()) {
                if ("Hospital Road".equals(road.getName())) {
                    hospitalRoad = road;
                }
            }
            routes.network().setRoadStatus(hospitalRoad, RoadSegment.STATUS_BLOCKED);
            RouteResult blockedRoute = routes.findRoute("LOC-2", "LOC-6", false);
            check("Blocked road forces detour or isolation",
                    !blockedRoute.isReachable() || blockedRoute.getTotalDistanceKm() > 3.0);
            List<String> reachable = routes.blockedRoadImpact("LOC-1");
            check("BFS impact analysis runs", reachable.contains("LOC-1"));
            routes.network().setRoadStatus(hospitalRoad, RoadSegment.STATUS_OPEN);

            boolean resolveGuard = false;
            try {
                disasters.changeStatus(cyclone, Disaster.STATUS_RESOLVED);
            } catch (AppException ex) {
                resolveGuard = true;
            }
            check("Resolve blocked while evacuations pending", resolveGuard);

            List<Evacuation> all = evacuations.forDisaster(cyclone.getId());
            for (int i = 0; i < all.size(); i++) {
                evacuations.setStatus(all.get(i).getId(), Evacuation.STATUS_EVACUATING);
                evacuations.setStatus(all.get(i).getId(), Evacuation.STATUS_EVACUATED);
            }
            disasters.changeStatus(cyclone, Disaster.STATUS_RESOLVED);
            check("Disaster resolved after evacuations complete",
                    Disaster.STATUS_RESOLVED.equals(cyclone.getStatus()));

            Map<String, Integer> stats = dashboardStats(dm);
            check("Dashboard stats computed", stats.get("citizens") == 9
                    && stats.get("disasters") == 2 && stats.get("resolved") == 1);

            boolean capacityGuard = false;
            try {
                shelters.adjustOccupancy("SHL-2", "100");
            } catch (AppException ex) {
                capacityGuard = true;
            }
            check("Shelter capacity guard", capacityGuard);
        } catch (AppException ex) {
            check("Workflow completed without exception (" + ex.getMessage() + ")", false);
        }
    }

    private void testValidation() {
        System.out.println("-- Validation --");
        DataManager dm = DataManager.forTest();
        ShelterService shelters = new ShelterService(dm);
        sdrs.service.NetworkService network = new sdrs.service.NetworkService(dm);
        boolean badPhone = false;
        try {
            new sdrs.service.CitizenService(dm).addCitizen("X", "30", "123", "Y", "LOC-2", "1",
                    Citizen.PRIORITY_LOW, "", "", "", "");
        } catch (AppException ex) {
            badPhone = true;
        }
        check("Invalid phone rejected", badPhone);
        boolean badAge = false;
        try {
            new sdrs.service.CitizenService(dm).addCitizen("X", "250", "9876500012", "Y", "LOC-2", "1",
                    Citizen.PRIORITY_LOW, "", "", "", "");
        } catch (AppException ex) {
            badAge = true;
        }
        check("Invalid age rejected", badAge);
        boolean badRoad = false;
        try {
            network.addRoad("LOC-1", "LOC-1", "5", "5", "Loop");
        } catch (AppException ex) {
            badRoad = true;
        }
        check("Self road rejected", badRoad);
        try {
            network.addLocation("New Junction", Location.TYPE_JUNCTION);
            check("Location added", true);
        } catch (AppException ex) {
            check("Location added", false);
        }
        try {
            shelters.addShelter("T", "LOC-NOPE", "A", "M", "9876500013", "50");
            check("Unknown location rejected", false);
        } catch (AppException ex) {
            check("Unknown location rejected", true);
        }
    }

    private void testPersistence() {
        System.out.println("-- Persistence --");
        File file = new File("build-test", "test_data.ser");
        deleteQuietly(file);
        DataStore store = new sdrs.data.FileDataStore(file);
        SystemData data = sdrs.util.SeedData.build();
        DataManager dm = new DataManager(data, store);
        try {
            new CitizenService(dm).addCitizen("Persist Test", "33", "9876500099", "Addr", "LOC-2", "2",
                    Citizen.PRIORITY_MEDIUM, "", "", "", "");
        } catch (AppException ex) {
            check("Save during add", false);
        }
        DataStore store2 = new sdrs.data.FileDataStore(file);
        SystemData reloaded = store2.load();
        boolean found = false;
        if (reloaded != null) {
            for (int i = 0; i < reloaded.getCitizens().size(); i++) {
                if ("Persist Test".equals(reloaded.getCitizens().get(i).getName())) {
                    found = true;
                }
            }
        }
        check("Data survives save/reload round-trip", found);
        deleteQuietly(file);
        new File("build-test").delete();
    }

    private Map<String, Integer> dashboardStats(DataManager dm) {
        Map<String, Integer> stats = new HashMap<String, Integer>();
        List<Disaster> disasters = dm.data().getDisasters();
        stats.put("citizens", Integer.valueOf(dm.data().getCitizens().size()));
        stats.put("disasters", Integer.valueOf(disasters.size()));
        int active = 0;
        int critical = 0;
        int resolved = 0;
        for (int i = 0; i < disasters.size(); i++) {
            Disaster d = disasters.get(i);
            if (d.isActiveLike()) {
                active++;
            }
            if (Disaster.SEVERITY_CRITICAL.equals(d.getSeverity()) && d.isActiveLike()) {
                critical++;
            }
            if (Disaster.STATUS_RESOLVED.equals(d.getStatus())) {
                resolved++;
            }
        }
        stats.put("active", Integer.valueOf(active));
        stats.put("critical", Integer.valueOf(critical));
        stats.put("resolved", Integer.valueOf(resolved));
        return stats;
    }

    private void deleteQuietly(File file) {
        if (file.exists()) {
            file.delete();
        }
    }
}
