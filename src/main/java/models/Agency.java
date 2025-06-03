package models;

public record Agency(String agencyId, String agencyName) {
    @Override
    public String toString() {
        return "Agency{" +
                "agencyId='" + agencyId + '\'' +
                "agencyName='" + agencyName + '\'' +
                '}';
    }
}


