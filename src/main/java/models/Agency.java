package models;

public record Agency(String agencyId, String agencyName) {
    public Agency() {
        this("N/A", "Unknown");
    }

    public Agency {
        if (agencyId == null || agencyId.isEmpty()) {
            agencyId = "N/A";
        }
        if (agencyName == null || agencyName.isEmpty()) {
            agencyName = "Unknown";
        }
    }

    @Override
    public String toString() {
        return "Agency{" +
                "agencyId='" + agencyId + '\'' +
                "agencyName='" + agencyName + '\'' +
                '}';
    }
}


