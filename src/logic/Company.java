package logic;

public class Company {
    private int companyId;
    private String name;
    private double currentMarketPrice;

    public Company(int companyId, String name, double currentMarketPrice) {
        this.companyId = companyId;
        this.name = name;
        this.currentMarketPrice = currentMarketPrice;
    }

    public void updateMarketPrice(double newPrice) {
        this.currentMarketPrice = newPrice;
    }

    @Override
    public String toString() {
        return "Company [companyId=" + companyId + ", name=" + name + ", currentMarketPrice=" + currentMarketPrice + "]";
    }

    public int getCompanyId() {
        return companyId;
    }
    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getCurrentMarketPrice() {
        return currentMarketPrice;
    }
    public void setCurrentMarketPrice(double currentMarketPrice) {
        this.currentMarketPrice = currentMarketPrice;
    }

}
