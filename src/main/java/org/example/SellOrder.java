package org.example;

/**
 * Represents a sell order (ask) for stocks
 */
public class SellOrder extends Order {
    
    public SellOrder(String traderName, Stock stock, int quantity) {
        super(traderName, stock, quantity);
    }

    @Override
    public String getOrderType() {
        return "SELL";
    }
}
