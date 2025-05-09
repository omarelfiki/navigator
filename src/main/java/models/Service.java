package models;

public class Service {
    private final String serviceId;

    public Service(String serviceID) {
        this.serviceId = serviceID;
    }
    public String getServiceId() {
        return serviceId;
    }
    public String toString() {
        return "ServiceID:" + serviceId;
    }
}
