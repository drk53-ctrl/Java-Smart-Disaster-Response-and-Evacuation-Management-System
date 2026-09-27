package sdrs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sdrs.data.DataStore;
import sdrs.data.FileDataStore;
import sdrs.data.SystemData;
import sdrs.model.Citizen;
import sdrs.model.Disaster;
import sdrs.model.Evacuation;
import sdrs.model.EmergencyTeam;
import sdrs.model.Location;
import sdrs.model.Resource;
import sdrs.model.RoadSegment;
import sdrs.model.Shelter;
import sdrs.model.User;
import sdrs.model.Vehicle;
import sdrs.util.AppException;
import sdrs.util.IdGenerator;

public class DataManager {

    private final SystemData data;
    private final DataStore store;
    private final Map<String, Object> citizenIndex = new HashMap<String, Object>();
    private final Map<String, Object> disasterIndex = new HashMap<String, Object>();
    private final Map<String, Object> shelterIndex = new HashMap<String, Object>();
    private final Map<String, Object> teamIndex = new HashMap<String, Object>();
    private final Map<String, Object> resourceIndex = new HashMap<String, Object>();
    private final Map<String, Object> vehicleIndex = new HashMap<String, Object>();
    private final Map<String, Object> userIndex = new HashMap<String, Object>();
    private final Map<String, Object> locationIndex = new HashMap<String, Object>();
    private final List<Runnable> listeners = new ArrayList<Runnable>();
    private final List<String> lastSavedNotes = new ArrayList<String>();
    private final List<Object> deletedStack = new ArrayList<Object>();
    private final java.util.LinkedList<String> taskQueue = new java.util.LinkedList<String>();

    public DataManager() {
        this.store = new FileDataStore(new java.io.File("data", "sdrs_data.ser"));
        SystemData loaded = this.store.load();
        if (loaded == null) {
            loaded = sdrs.util.SeedData.build();
            this.lastSavedNotes.add("No existing data file found - demo data was created.");
        } else {
            this.lastSavedNotes.add("Data loaded from " + storeDescription());
            migrateLegacyMapPositions(loaded);
        }
        this.data = loaded;
        migrateLegacyMapPositions(this.data);
        rebuildIndexes();
        save();
    }

    private void migrateLegacyMapPositions(SystemData candidate) {
        java.util.List<Location> locations = candidate.getLocations();
        if (locations.isEmpty()) {
            return;
        }
        boolean nonePositioned = true;
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            boolean hasUsablePosition = location.hasMapPosition()
                    && (Math.abs(location.getX()) > 1e-9 || Math.abs(location.getY()) > 1e-9);
            if (hasUsablePosition) {
                nonePositioned = false;
                break;
            }
        }
        if (!nonePositioned) {
            return;
        }
        SystemData reference = sdrs.util.SeedData.build();
        Map<String, double[]> namedPositions = new HashMap<String, double[]>();
        for (int i = 0; i < reference.getLocations().size(); i++) {
            Location seed = reference.getLocations().get(i);
            namedPositions.put(seed.getName(), new double[] {seed.getX(), seed.getY()});
        }
        int restored = 0;
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            double[] position = namedPositions.get(location.getName());
            if (position != null) {
                location.setX(position[0]);
                location.setY(position[1]);
            } else {
                double angle = (Math.PI * 2 * i) / locations.size();
                location.setX(50 + 34 * Math.cos(angle));
                location.setY(50 + 34 * Math.sin(angle));
            }
            restored++;
        }
        this.lastSavedNotes.add("Map coordinates restored for " + restored
                + " locations (data file was from an older version).");
    }

    public DataManager(SystemData preloaded, DataStore store) {
        this.data = preloaded;
        this.store = store;
        migrateLegacyMapPositions(this.data);
        rebuildIndexes();
    }

    public static DataManager forTest() {
        return new DataManager(sdrs.util.SeedData.build(), new DataStore() {
            public SystemData load() {
                return null;
            }

            public void save(SystemData dataObject) {
            }
        });
    }

    private String storeDescription() {
        return "data/sdrs_data.ser";
    }

    public SystemData data() {
        return data;
    }

    public IdGenerator ids() {
        return data.getIdGenerator();
    }

    private void rebuildIndexes() {
        citizenIndex.clear();
        disasterIndex.clear();
        shelterIndex.clear();
        teamIndex.clear();
        resourceIndex.clear();
        vehicleIndex.clear();
        userIndex.clear();
        locationIndex.clear();
        for (int i = 0; i < data.getCitizens().size(); i++) {
            citizenIndex.put(data.getCitizens().get(i).getId(), data.getCitizens().get(i));
        }
        for (int i = 0; i < data.getDisasters().size(); i++) {
            disasterIndex.put(data.getDisasters().get(i).getId(), data.getDisasters().get(i));
        }
        for (int i = 0; i < data.getShelters().size(); i++) {
            shelterIndex.put(data.getShelters().get(i).getId(), data.getShelters().get(i));
        }
        for (int i = 0; i < data.getTeams().size(); i++) {
            teamIndex.put(data.getTeams().get(i).getId(), data.getTeams().get(i));
        }
        for (int i = 0; i < data.getResources().size(); i++) {
            resourceIndex.put(data.getResources().get(i).getId(), data.getResources().get(i));
        }
        for (int i = 0; i < data.getVehicles().size(); i++) {
            vehicleIndex.put(data.getVehicles().get(i).getId(), data.getVehicles().get(i));
        }
        for (int i = 0; i < data.getUsers().size(); i++) {
            userIndex.put(data.getUsers().get(i).getUsername(), data.getUsers().get(i));
        }
        for (int i = 0; i < data.getLocations().size(); i++) {
            locationIndex.put(data.getLocations().get(i).getId(), data.getLocations().get(i));
        }
    }

    public Object citizenById(String id) {
        return citizenIndex.get(id);
    }

    public Object disasterById(String id) {
        return disasterIndex.get(id);
    }

    public Object shelterById(String id) {
        return shelterIndex.get(id);
    }

    public Object teamById(String id) {
        return teamIndex.get(id);
    }

    public Object resourceById(String id) {
        return resourceIndex.get(id);
    }

    public Object vehicleById(String id) {
        return vehicleIndex.get(id);
    }

    public Object userByUsername(String username) {
        return userIndex.get(username);
    }

    public Object locationById(String id) {
        return locationIndex.get(id);
    }

    public void addCitizen(Citizen c) {
        data.getCitizens().add(c);
        citizenIndex.put(c.getId(), c);
    }

    public void removeCitizen(String id) {
        Citizen c = (Citizen) citizenIndex.remove(id);
        data.getCitizens().remove(c);
    }

    public void addDisaster(Disaster d) {
        data.getDisasters().add(d);
        disasterIndex.put(d.getId(), d);
    }

    public void removeDisaster(String id) {
        Disaster d = (Disaster) disasterIndex.remove(id);
        data.getDisasters().remove(d);
    }

    public void addShelter(Shelter s) {
        data.getShelters().add(s);
        shelterIndex.put(s.getId(), s);
    }

    public void removeShelter(String id) {
        Shelter s = (Shelter) shelterIndex.remove(id);
        data.getShelters().remove(s);
    }

    public void addTeam(EmergencyTeam t) {
        data.getTeams().add(t);
        teamIndex.put(t.getId(), t);
    }

    public void removeTeam(String id) {
        EmergencyTeam t = (EmergencyTeam) teamIndex.remove(id);
        data.getTeams().remove(t);
    }

    public void addResource(Resource r) {
        data.getResources().add(r);
        resourceIndex.put(r.getId(), r);
    }

    public void removeResource(String id) {
        Resource r = (Resource) resourceIndex.remove(id);
        data.getResources().remove(r);
    }

    public void addVehicle(Vehicle v) {
        data.getVehicles().add(v);
        vehicleIndex.put(v.getId(), v);
    }

    public void removeVehicle(String id) {
        Vehicle v = (Vehicle) vehicleIndex.remove(id);
        data.getVehicles().remove(v);
    }

    public void addUser(User u) {
        data.getUsers().add(u);
        userIndex.put(u.getUsername(), u);
    }

    public void removeUser(String username) {
        User u = (User) userIndex.remove(username);
        data.getUsers().remove(u);
    }

    public void addLocation(Location l) {
        data.getLocations().add(l);
        locationIndex.put(l.getId(), l);
    }

    public void removeLocation(String id) {
        Location l = (Location) locationIndex.remove(id);
        data.getLocations().remove(l);
    }

    public void addRoad(RoadSegment r) {
        data.getRoads().add(r);
    }

    public void removeRoad(RoadSegment r) {
        data.getRoads().remove(r);
    }

    public void pushDeleted(Object entity) {
        deletedStack.add(entity);
    }

    public boolean hasDeletedHistory() {
        return !deletedStack.isEmpty();
    }

    public String undoLastDelete() throws AppException {
        if (deletedStack.isEmpty()) {
            throw new AppException("Nothing to undo.");
        }
        Object entity = deletedStack.remove(deletedStack.size() - 1);
        if (entity instanceof Citizen) {
            addCitizen((Citizen) entity);
            return "Citizen " + ((Citizen) entity).getName() + " restored.";
        }
        if (entity instanceof Disaster) {
            addDisaster((Disaster) entity);
            return "Disaster " + entity.toString() + " restored.";
        }
        if (entity instanceof Shelter) {
            addShelter((Shelter) entity);
            return "Shelter " + ((Shelter) entity).getName() + " restored.";
        }
        if (entity instanceof EmergencyTeam) {
            addTeam((EmergencyTeam) entity);
            return "Team " + ((EmergencyTeam) entity).getName() + " restored.";
        }
        if (entity instanceof Resource) {
            addResource((Resource) entity);
            return "Resource " + ((Resource) entity).getName() + " restored.";
        }
        if (entity instanceof Vehicle) {
            addVehicle((Vehicle) entity);
            return "Vehicle " + ((Vehicle) entity).getType() + " restored.";
        }
        if (entity instanceof Location) {
            addLocation((Location) entity);
            return "Location " + ((Location) entity).getName() + " restored.";
        }
        return "Entry restored.";
    }

    public void enqueueTask(String description) {
        taskQueue.addLast(description);
    }

    public String processNextTask() {
        return taskQueue.pollFirst();
    }

    public String peekNextTask() {
        return taskQueue.peekFirst();
    }

    public java.util.List<String> pendingTasks() {
        return new ArrayList<String>(taskQueue);
    }

    public void requireCitizen(String id) throws AppException {
        if (citizenIndex.get(id) == null) {
            throw new AppException("Citizen " + id + " does not exist.");
        }
    }

    public void requireDisaster(String id) throws AppException {
        if (disasterIndex.get(id) == null) {
            throw new AppException("Disaster " + id + " does not exist.");
        }
    }

    public void requireShelter(String id) throws AppException {
        if (shelterIndex.get(id) == null) {
            throw new AppException("Shelter " + id + " does not exist.");
        }
    }

    public void requireTeam(String id) throws AppException {
        if (teamIndex.get(id) == null) {
            throw new AppException("Team " + id + " does not exist.");
        }
    }

    public void requireResource(String id) throws AppException {
        if (resourceIndex.get(id) == null) {
            throw new AppException("Resource " + id + " does not exist.");
        }
    }

    public void requireVehicle(String id) throws AppException {
        if (vehicleIndex.get(id) == null) {
            throw new AppException("Vehicle " + id + " does not exist.");
        }
    }

    public void requireLocation(String id) throws AppException {
        if (locationIndex.get(id) == null) {
            throw new AppException("Location " + id + " does not exist.");
        }
    }

    public void save() {
        store.save(data);
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void notifyChanged() {
        save();
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).run();
        }
    }

    public List<String> startupNotes() {
        return new ArrayList<String>(lastSavedNotes);
    }

    public boolean removeListener(Runnable listener) {
        return listeners.remove(listener);
    }

    public String nextId(String prefix) {
        return data.getIdGenerator().next(prefix);
    }

    public void touch() {
        notifyChanged();
    }
}
