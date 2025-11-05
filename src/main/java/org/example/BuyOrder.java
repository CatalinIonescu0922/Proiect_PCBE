package org.example;

/**
 * Represents a buy order (bid) for stocks
 */
public class BuyOrder extends Order {
    
    public BuyOrder(String traderName, Stock stock, int quantity) {
        super(traderName, stock, quantity);
    }

    @Override
    public String getOrderType() {
        return "BUY";
    }
}
