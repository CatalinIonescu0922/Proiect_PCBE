package org.example;

/**
 * Represents a sell order (ask) for stocks
 */
public class SellOrder extends Order {
    
    public SellOrder(String traderName, Stock stock, int quantity, double pricePerShare) {
        super(traderName, stock, quantity, pricePerShare);
    }

    @Override
    public String getOrderType() {
        return "SELL";
    }
}
