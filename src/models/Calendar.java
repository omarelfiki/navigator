public record Calendar(Service service, boolean monday, boolean tuesday, boolean wednesday, boolean thursday,
                       boolean friday, boolean saturday, boolean sunday, String startDate, String endDate) {
    @Override
    public String toString() {
        return "serviceID=" + (service != null ? service.getServiceId() : "null") +
                ", monday=" + monday +
                ", tuesday=" + tuesday +
                ", wednesday=" + wednesday +
                ", thursday=" + thursday +
                ", friday=" + friday +
                ", saturday=" + saturday +
                ", sunday=" + sunday +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }

}
