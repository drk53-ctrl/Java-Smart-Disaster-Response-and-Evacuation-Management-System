package sdrs.model;

import java.io.Serializable;

public class Allocation implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String id;
    private final String resourceId;
    private final String resourceName;
    private final String disasterId;
    private final int quantity;
    private final long timestamp;
    private final String byUser;

    public Allocation(String id, String resourceId, String resourceName, String disasterId,
                      int quantity, long timestamp, String byUser) {
        this.id = id;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.disasterId = disasterId;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.byUser = byUser;
    }

    public String getId() {
        return id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getDisasterId() {
        return disasterId;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getByUser() {
        return byUser;
    }
}
