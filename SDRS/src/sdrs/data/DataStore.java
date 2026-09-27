package sdrs.data;

public interface DataStore {

    SystemData load();

    void save(SystemData data);
}
