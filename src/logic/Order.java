package logic;

public class Order {
    private Company company;
    private boolean isBuyer;
    private double price;
    private int nrShares;

    public Order(Company company, boolean isBuyer, int nrShares, double price) {
        this.company = company;
        this.isBuyer = isBuyer;
        this.price = price;
        this.nrShares = nrShares;
    }

    @Override
    public String toString() {
        return "Order [company=" + company + ", isBuyer=" + isBuyer + ", price=" + price + ", nrShares=" + nrShares + "]";
    }

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }
    public boolean isBuyer() {
        return isBuyer;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public int getNrShares() {
        return nrShares;
    }
    public void setNrShares(int nrShares) {
        this.nrShares = nrShares;
    }
}
