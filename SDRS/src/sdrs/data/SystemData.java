package sdrs.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sdrs.model.Allocation;
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
import sdrs.util.IdGenerator;

public class SystemData implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<User> users = new ArrayList<User>();
    private List<Citizen> citizens = new ArrayList<Citizen>();
    private List<Disaster> disasters = new ArrayList<Disaster>();
    private List<Shelter> shelters = new ArrayList<Shelter>();
    private List<EmergencyTeam> teams = new ArrayList<EmergencyTeam>();
    private List<Resource> resources = new ArrayList<Resource>();
    private List<Vehicle> vehicles = new ArrayList<Vehicle>();
    private List<Evacuation> evacuations = new ArrayList<Evacuation>();
    private List<Location> locations = new ArrayList<Location>();
    private List<RoadSegment> roads = new ArrayList<RoadSegment>();
    private List<Allocation> allocations = new ArrayList<Allocation>();
    private IdGenerator idGenerator = new IdGenerator();

    public List<User> getUsers() {
        return users;
    }

    public List<Citizen> getCitizens() {
        return citizens;
    }

    public List<Disaster> getDisasters() {
        return disasters;
    }

    public List<Shelter> getShelters() {
        return shelters;
    }

    public List<EmergencyTeam> getTeams() {
        return teams;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public List<Evacuation> getEvacuations() {
        return evacuations;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<RoadSegment> getRoads() {
        return roads;
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
}
