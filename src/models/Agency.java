public class Agency {
    private String agencyId;
    private String agencyName;
    private String agencyUrl;
    private String agencyLang;
    private String agencyTimeZone;

    // optional field
    private String agencyFareUrl;

    //  Constructor for required fields only
    public Agency(String agencyName, String agencyUrl, String agencyLang, String agencyTimeZone) {
        this.agencyName = agencyName;
        this.agencyUrl = agencyUrl;
        this.agencyLang = agencyLang;
        this.agencyTimeZone = agencyTimeZone;
    }

    //  Full constructor (required + optional)
    public Agency(String agencyName, String agencyUrl, String agencyLang, String agencyTimeZone, String agencyFareUrl) {
        this(agencyName, agencyUrl, agencyLang, agencyTimeZone);
        this.agencyFareUrl = agencyFareUrl;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public String getAgencyUrl() {
        return agencyUrl;
    }

    public String getAgencyLang() {
        return agencyLang;
    }

    public String getAgencyTimeZone() {
        return agencyTimeZone;
    }

    public String getAgencyFareUrl() {
        return agencyFareUrl;
    }

    @Override
    public String toString() {
        return "models.Agency{" +
                "agencyId='" + agencyId + '\'' +
                "agencyName='" + agencyName + '\'' +
                ", agencyUrl='" + agencyUrl + '\'' +
                ", agencyLang='" + agencyLang + '\'' +
                ", agencyTimeZone='" + agencyTimeZone + '\'' +
                ", agencyFareUrl='" + agencyFareUrl + '\'' +
                '}';
    }
}


