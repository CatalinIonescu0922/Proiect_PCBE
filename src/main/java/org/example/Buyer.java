package org.example;

import java.util.List;
import java.util.Random;

/**
 * Buyer thread that randomly buys stocks from the exchange
 */
public class Buyer extends Thread {
    private final String buyerName;
    private final StockExchange exchange;
    private final Random random;
    private final int minShares;
    private final int maxShares;
    private final double priceVariance; // percentage variance from current price
    private final int minDelayMs;
    private final int maxDelayMs;
    private final int maxOrders;

    public Buyer(String buyerName, StockExchange exchange, int minShares, int maxShares, 
                 double priceVariance, int minDelayMs, int maxDelayMs, int maxOrders) {
        this.buyerName = buyerName;
        this.exchange = exchange;
        this.random = new Random();
        this.minShares = minShares;
        this.maxShares = maxShares;
        this.priceVariance = priceVariance;
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.maxOrders = maxOrders;
        setName(buyerName);
    }

    @Override
    public void run() {
        int ordersPlaced = 0;
        
        while (exchange.isRunning() && ordersPlaced < maxOrders) {
            try {
                // Random delay before placing next order
                int delay = minDelayMs + random.nextInt(maxDelayMs - minDelayMs + 1);
                Thread.sleep(delay);

                if (!exchange.isRunning()) break;

                // Randomly select a stock
                List<Stock> stocks = exchange.getAllStocks();
                if (stocks.isEmpty()) continue;
                
                Stock stock = stocks.get(random.nextInt(stocks.size()));
                
                // Random quantity
                int quantity = minShares + random.nextInt(maxShares - minShares + 1);
                
                // Price with variance (buyers might pay slightly more)
                double currentPrice = stock.getCurrentPrice();
                double priceAdjustment = 1.0 + (random.nextDouble() * priceVariance * 2 - priceVariance);
                double bidPrice = currentPrice * priceAdjustment;
                bidPrice = Math.round(bidPrice * 100.0) / 100.0; // Round to 2 decimals
                
                // Place buy order
                BuyOrder order = new BuyOrder(buyerName, stock, quantity, bidPrice);
                exchange.placeBuyOrder(order);
                ordersPlaced++;
                
                // Small chance to cancel or modify the order after a short delay
                if (random.nextDouble() < 0.15) { // 15% chance
                    Thread.sleep(random.nextInt(500) + 100);
                    int currentQuantity = exchange.getOrderQuantity(order);
                    if (random.nextBoolean() && currentQuantity > 0) {
                        exchange.cancelBuyOrder(order);
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        Logger.logEvent(buyerName + " has finished trading (placed " + ordersPlaced + " orders)");
        System.out.println("🔵 " + buyerName + " has finished trading");
    }
}
