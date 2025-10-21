package logic;

import java.util.List;
import java.util.Random;

public class Sellers implements Client{
    private int sellerId;
    private List<Company> companies;
    Random random = new Random();

    public Sellers(int sellerId, List<Company> companies) {
        this.sellerId = sellerId;
        this.companies = companies;
    }

    public int getClientId() {
        return sellerId;
    }

    public void placeOrder() {
        Company company = companies.get(random.nextInt(companies.size()));
        int nrShares = 1 + random.nextInt(1000);
        double price = company.getCurrentMarketPrice() + random.nextDouble() * 2;

        Order order = new Order(company, false, nrShares, price);
    }
}
