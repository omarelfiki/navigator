package models;

public class CalendarDates {
    private Service service;
    private String date;
    private String exceptionType;

    public CalendarDates(Service service, String date, String exceptionType) {
        this.service = service;
        this.date = date;
        this.exceptionType = exceptionType;
    }

    public String getDate() {
        return date;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public Service getService() {
        return service;
    }

    @Override
    public String toString() {
        return "CalendarDates{" +
                "serviceId='" + (service != null ? service.getServiceId() : "null") + '\'' +
                ", date='" + date + '\'' +
                ", exceptionType='" + exceptionType + '\'' +
                '}';
    }
}

