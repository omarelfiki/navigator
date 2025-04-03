public class Service {
    private String serviceID;

    public Service(String serviceID) {
        this.serviceID = serviceID;
    }
    public String getServiceID() {
        return serviceID;
    }
    public String toString() {
        return "ServiceID:" + serviceID;
    }
}
