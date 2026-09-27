package sdrs.util;

import java.util.HashMap;
import java.util.Map;

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

public final class SeedData {

    private SeedData() {
    }

    public static SystemData build() {
        SystemData data = new SystemData();

        User admin = new User("USR-1", "admin", PasswordUtil.hash("admin123"),
                "System Administrator", "9000000001", User.ROLE_ADMIN);
        User dmo = new User("USR-2", "dmo", PasswordUtil.hash("dmo123"),
                "K. Anand", "9000000002", User.ROLE_DMO);
        User ro = new User("USR-3", "response", PasswordUtil.hash("response123"),
                "P. Latha", "9000000003", User.ROLE_RO);
        data.getUsers().add(admin);
        data.getUsers().add(dmo);
        data.getUsers().add(ro);

        data.getLocations().add(new Location("LOC-1", "Central Command", Location.TYPE_EMERGENCY_CENTER, 12, 78));
        data.getLocations().add(new Location("LOC-2", "Riverside Colony", Location.TYPE_DISASTER_ZONE, 62, 28));
        data.getLocations().add(new Location("LOC-3", "Lakeview Town", Location.TYPE_DISASTER_ZONE, 90, 62));
        data.getLocations().add(new Location("LOC-4", "North Relief Shelter", Location.TYPE_SHELTER, 68, 8));
        data.getLocations().add(new Location("LOC-5", "East Relief Shelter", Location.TYPE_SHELTER, 96, 86));
        data.getLocations().add(new Location("LOC-6", "City General Hospital", Location.TYPE_HOSPITAL, 54, 54));
        data.getLocations().add(new Location("LOC-7", "Central Fire Station", Location.TYPE_FIRE_STATION, 42, 10));
        data.getLocations().add(new Location("LOC-8", "City Police HQ", Location.TYPE_POLICE_STATION, 8, 50));
        data.getLocations().add(new Location("LOC-9", "Highway Junction", Location.TYPE_JUNCTION, 44, 36));
        data.getLocations().add(new Location("LOC-10", "Old Town Junction", Location.TYPE_JUNCTION, 76, 72));

        data.getRoads().add(new RoadSegment("LOC-1", "LOC-9", 6.0, 12, "Ring Road"));
        data.getRoads().add(new RoadSegment("LOC-9", "LOC-2", 4.5, 9, "Riverside Link"));
        data.getRoads().add(new RoadSegment("LOC-9", "LOC-4", 5.5, 11, "North Avenue"));
        data.getRoads().add(new RoadSegment("LOC-2", "LOC-6", 3.0, 7, "Hospital Road"));
        data.getRoads().add(new RoadSegment("LOC-6", "LOC-10", 4.0, 8, "Lake Bypass"));
        data.getRoads().add(new RoadSegment("LOC-10", "LOC-3", 5.0, 12, "Lakeview Road"));
        data.getRoads().add(new RoadSegment("LOC-3", "LOC-5", 6.5, 14, "East Coast Road"));
        data.getRoads().add(new RoadSegment("LOC-5", "LOC-1", 7.0, 15, "Command Road"));
        data.getRoads().add(new RoadSegment("LOC-7", "LOC-9", 2.5, 5, "Fire Lane"));
        data.getRoads().add(new RoadSegment("LOC-8", "LOC-1", 3.5, 8, "HQ Road"));
        data.getRoads().add(new RoadSegment("LOC-4", "LOC-6", 4.5, 10, "Relief Lane"));
        data.getRoads().add(new RoadSegment("LOC-7", "LOC-2", 5.0, 11, "Rescue Route"));
        data.getRoads().add(new RoadSegment("LOC-8", "LOC-10", 5.5, 12, "Old Town Way"));

        data.getShelters().add(new Shelter("SHL-1", "North Relief Shelter", "LOC-4",
                "North Avenue, near water tank", "R. Bhat", "9800000011", 300, 40));
        data.getShelters().add(new Shelter("SHL-2", "East Relief Shelter", "LOC-5",
                "East Coast Road, school ground", "S. Menon", "9800000022", 250, 250));
        data.getShelters().add(new Shelter("SHL-3", "Old Town Community Hall", "LOC-10",
                "Old Town Junction, hall street", "A. Fernandes", "9800000033", 150, 0));

        data.getTeams().add(new EmergencyTeam("TEAM-1", "Alpha Medical Unit", EmergencyTeam.TYPE_MEDICAL,
                12, "Dr. Rao", "LOC-6"));
        data.getTeams().add(new EmergencyTeam("TEAM-2", "Fire Rescue Squad 1", EmergencyTeam.TYPE_FIRE_RESCUE,
                18, "Capt. Mehta", "LOC-7"));
        data.getTeams().add(new EmergencyTeam("TEAM-3", "Rapid Rescue Team", EmergencyTeam.TYPE_SEARCH_RESCUE,
                10, "Maj. Singh", "LOC-1"));
        data.getTeams().add(new EmergencyTeam("TEAM-4", "Traffic Police Unit", EmergencyTeam.TYPE_POLICE,
                8, "Insp. Khan", "LOC-8"));

        data.getVehicles().add(new Vehicle("VEH-1", Vehicle.TYPE_AMBULANCE, "KA-01-AB-1234", "R. Kumar", "LOC-6"));
        data.getVehicles().add(new Vehicle("VEH-2", Vehicle.TYPE_AMBULANCE, "KA-01-AB-5678", "S. Ali", "LOC-4"));
        data.getVehicles().add(new Vehicle("VEH-3", Vehicle.TYPE_FIRE_TRUCK, "KA-02-FT-9012", "P. Verma", "LOC-7"));
        data.getVehicles().add(new Vehicle("VEH-4", Vehicle.TYPE_RESCUE, "KA-03-RV-3344", "D. Nair", "LOC-1"));
        data.getVehicles().add(new Vehicle("VEH-5", Vehicle.TYPE_SUPPLY, "KA-04-SV-7788", "M. Das", "LOC-9"));

        data.getResources().add(new Resource("RES-1", "Bottled Water", Resource.TYPE_WATER, 1200, "LOC-9", "bottles"));
        data.getResources().add(new Resource("RES-2", "Food Packets", Resource.TYPE_FOOD, 800, "LOC-1", "packets"));
        data.getResources().add(new Resource("RES-3", "Medical Kits", Resource.TYPE_MEDICAL_KIT, 45, "LOC-6", "kits"));
        data.getResources().add(new Resource("RES-4", "Blankets", Resource.TYPE_BLANKET, 350, "LOC-4", "pieces"));
        data.getResources().add(new Resource("RES-5", "Rescue Ropes", Resource.TYPE_RESCUE_EQUIPMENT, 25, "LOC-7", "sets"));
        data.getResources().add(new Resource("RES-6", "Diesel Reserve", Resource.TYPE_FUEL, 900, "LOC-9", "litres"));

        data.getCitizens().add(new Citizen("CIT-1", "Ramesh Iyer", 68, "9811100001",
                "12, Riverside Colony", "LOC-2", 2, Citizen.PRIORITY_CRITICAL, "Insulin dependent"));
        data.getCitizens().add(new Citizen("CIT-2", "Sunita Devi", 34, "9811100002",
                "5, Lakeview Town", "LOC-3", 4, Citizen.PRIORITY_HIGH, "Pregnant"));
        data.getCitizens().add(new Citizen("CIT-3", "Arjun Patel", 9, "9811100003",
                "8, Riverside Colony", "LOC-2", 5, Citizen.PRIORITY_HIGH, ""));
        data.getCitizens().add(new Citizen("CIT-4", "Meena Sharma", 45, "9811100004",
                "21, Lakeview Town", "LOC-3", 3, Citizen.PRIORITY_MEDIUM, ""));
        data.getCitizens().add(new Citizen("CIT-5", "John Mathew", 30, "9811100005",
                "14, Old Town", "LOC-10", 2, Citizen.PRIORITY_LOW, ""));
        data.getCitizens().add(new Citizen("CIT-6", "Lakshmi Narayan", 72, "9811100006",
                "3, Riverside Colony", "LOC-2", 1, Citizen.PRIORITY_CRITICAL, "Heart patient"));
        data.getCitizens().add(new Citizen("CIT-7", "Imran Sheikh", 27, "9811100007",
                "9, Old Town", "LOC-10", 4, Citizen.PRIORITY_MEDIUM, ""));
        data.getCitizens().add(new Citizen("CIT-8", "Baby Ananya", 6, "9811100008",
                "2, Lakeview Town", "LOC-3", 5, Citizen.PRIORITY_CRITICAL, "Asthma"));

        Disaster flood = new Disaster("DIS-1", Disaster.TYPE_FLOOD, "Riverside Colony", "LOC-2",
                Disaster.SEVERITY_CRITICAL, System.currentTimeMillis() - 86400000L, 1200,
                "River overflowed after 48 hours of heavy rain. Low-lying streets are waist-deep in water.");
        flood.setStatus(Disaster.STATUS_ACTIVE);
        flood.getCitizenIds().add("CIT-1");
        flood.getCitizenIds().add("CIT-3");
        flood.getCitizenIds().add("CIT-6");
        data.getDisasters().add(flood);

        Map<String, Integer> counters = new HashMap<String, Integer>();
        counters.put("USR", Integer.valueOf(3));
        counters.put("CIT", Integer.valueOf(8));
        counters.put("DIS", Integer.valueOf(1));
        counters.put("SHL", Integer.valueOf(3));
        counters.put("TEAM", Integer.valueOf(4));
        counters.put("RES", Integer.valueOf(6));
        counters.put("VEH", Integer.valueOf(5));
        counters.put("EVC", Integer.valueOf(0));
        counters.put("LOC", Integer.valueOf(10));
        counters.put("RD", Integer.valueOf(13));
        data.getIdGenerator().getCounters().putAll(counters);

        return data;
    }

    public static Evacuation newEvacuation(String id, String citizenId, String disasterId, int people) {
        return new Evacuation(id, citizenId, disasterId, people, System.currentTimeMillis());
    }
}
