package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Buyer thread that randomly buys stocks from the exchange
 */
public class Buyer extends Client implements Callable<String>{

    public Buyer(String buyerName, StockExchange exchange, int minShares, int maxShares, 
                 int minDelayMs, int maxDelayMs, int maxOrders) {
        super(buyerName, exchange, minShares, maxShares, 
                 minDelayMs, maxDelayMs, maxOrders);
    }

    @Override
    public String call() throws Exception {
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
                BuyOrder order = new BuyOrder(name, stock, quantity);
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
        
        Logger.logEvent(name + " has finished trading (placed " + ordersPlaced + " orders)");
        return "🔵 " + name + " has finished trading";
    }
}
