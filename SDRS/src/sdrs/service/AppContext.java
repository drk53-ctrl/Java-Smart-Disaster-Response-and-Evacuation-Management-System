package sdrs.service;

public class AppContext {

    private static AppContext instance;

    private final DataManager dataManager;
    private final CitizenService citizenService;
    private final DisasterService disasterService;
    private final ShelterService shelterService;
    private final TeamService teamService;
    private final VehicleService vehicleService;
    private final ResourceService resourceService;
    private final NetworkService networkService;
    private final RouteService routeService;
    private final EvacuationService evacuationService;
    private final ResponseService responseService;

    private AppContext() {
        dataManager = new DataManager();
        citizenService = new CitizenService(dataManager);
        disasterService = new DisasterService(dataManager);
        shelterService = new ShelterService(dataManager);
        teamService = new TeamService(dataManager);
        vehicleService = new VehicleService(dataManager);
        resourceService = new ResourceService(dataManager);
        networkService = new NetworkService(dataManager);
        routeService = new RouteService(dataManager);
        routeService.setNetwork(networkService);
        evacuationService = new EvacuationService(dataManager, routeService, shelterService);
        responseService = new ResponseService(dataManager, disasterService, evacuationService,
                routeService, resourceService);
    }

    public static synchronized AppContext get() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public DataManager dataManager() {
        return dataManager;
    }

    public CitizenService citizens() {
        return citizenService;
    }

    public DisasterService disasters() {
        return disasterService;
    }

    public ShelterService shelters() {
        return shelterService;
    }

    public TeamService teams() {
        return teamService;
    }

    public VehicleService vehicles() {
        return vehicleService;
    }

    public ResourceService resources() {
        return resourceService;
    }

    public NetworkService network() {
        return networkService;
    }

    public RouteService routes() {
        return routeService;
    }

    public EvacuationService evacuations() {
        return evacuationService;
    }

    public ResponseService response() {
        return responseService;
    }
}
