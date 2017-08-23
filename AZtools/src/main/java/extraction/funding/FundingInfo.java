package extraction.funding;

public class FundingInfo {
    public String agency;
    public String license;

    public String getAgency() {  return agency; }
    public String getLicense() { return license; }

    public FundingInfo(){}

    public void setAgency(String agency) { this.agency = agency; }
    public void setLicense(String license) { this.license = license; }

}
