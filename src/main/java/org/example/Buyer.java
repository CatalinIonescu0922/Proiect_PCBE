package org.example;

import java.util.ArrayList;
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
    private final int minDelayMs;
    private final int maxDelayMs;
    private final int maxOrders;
    private final List<Stock> availableStocks;
    private final List<Long> activeOrderIds;

    public Buyer(String buyerName, StockExchange exchange, int minShares, int maxShares, 
                 int minDelayMs, int maxDelayMs, int maxOrders) {
        this.buyerName = buyerName;
        this.exchange = exchange;
        this.random = new Random();
        this.minShares = minShares;
        this.maxShares = maxShares;
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.maxOrders = maxOrders;
        this.availableStocks = exchange.getAllStocks();
        this.activeOrderIds = new ArrayList<>();
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
                if (availableStocks.isEmpty()) continue;

                Stock stock = availableStocks.get(random.nextInt(availableStocks.size()));

                int quantity = minShares + random.nextInt(maxShares - minShares + 1);
                
                // Place buy order at current stock price
                BuyOrder order = new BuyOrder(buyerName, stock, quantity);
                exchange.placeBuyOrder(order);
                activeOrderIds.add(order.getOrderId());
                ordersPlaced++;
                
                // Randomly cancel or edit previous orders
                if (!activeOrderIds.isEmpty() && random.nextDouble() < 0.2) { // 20% chance
                    Thread.sleep(random.nextInt(300) + 100);
                    long randomOrderId = activeOrderIds.get(random.nextInt(activeOrderIds.size()));
                    
                    double action = random.nextDouble();
                    if (action < 0.5) {
                        // Cancel order
                        if (exchange.cancelBuyOrderById(randomOrderId)) {
                            activeOrderIds.remove(randomOrderId);
                        }
                    } else {
                        // Edit order quantity
                        int newQuantity = minShares + random.nextInt(maxShares - minShares + 1);
                        if (!exchange.editBuyOrder(randomOrderId, newQuantity)) {
                            // Order doesn't exist anymore, remove from our list
                            activeOrderIds.remove(randomOrderId);
                        }
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
