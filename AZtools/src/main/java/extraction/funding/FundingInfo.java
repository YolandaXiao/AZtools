package extraction.funding;

public class FundingInfo {

    public String agency;
    public String license;

    public FundingInfo() {
        agency = "";
        license = "";
    }

    public String getAgency() {
        return agency;
    }

    public String getLicense() {
        return license;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String toString() {
        if (agency.equals("") || license.equals(null)) {
            return license;
        } else if (license.equals("") || license.equals(null)) {
            return agency;
        } else {
            String funding_info = agency + ":" + license;
            return funding_info;
        }
    }
}
