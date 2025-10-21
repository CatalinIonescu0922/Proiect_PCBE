package logic;

import java.util.List;
import java.util.Random;

public class Buyers implements Client{
    private int buyerId;
    private List<Company> companies;

    Random random = new Random();

    public Buyers(int buyerId, List<Company> companies) {
        this.buyerId = buyerId;
        this.companies = companies;
    }

    public void placeOrder() {
        Company company = companies.get(random.nextInt(companies.size()));
        int nrShares = 1 + random.nextInt(1000);
        double price = company.getCurrentMarketPrice() - random.nextDouble() * 2;

        Order order = new Order(company, true, nrShares, price);
    }

    public int getClientId() {
        return buyerId;
    }
}
