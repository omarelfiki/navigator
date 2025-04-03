public class Calendar {
    private Service service;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;
    private String startDate;
    private String endDate;

    public Calendar( Service service, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday , String startDate, String endDate) {
    this.service = service;
    this.monday = monday;
    this.tuesday = tuesday;
    this.wednesday = wednesday;
    this.thursday = thursday;
    this.friday = friday;
    this.saturday = saturday;
    this.sunday = sunday;
    this.startDate = startDate;
    this.endDate = endDate;
    }
    public Service getService() {
        return service;
    }
    public boolean getMonday() {
        return monday;
    }
    public boolean getTuesday() {
        return tuesday;
    }
    public boolean getWednesday() {
        return wednesday;
    }
    public boolean getThursday() {
        return thursday;
    }
    public boolean getFriday() {
        return friday;
    }
    public boolean getSaturday() {
        return saturday;
    }
    public boolean getSunday() {
        return sunday;
    }
    public String getStartDate() {
        return startDate;
    }
    public String getEndDate() {
        return endDate;

    }
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
