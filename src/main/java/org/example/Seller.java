package org.example;

import java.util.concurrent.Callable;


/**
 * Seller thread that randomly sells stocks on the exchange
 */
public class Seller extends Client implements Callable<String> {
    public Seller(String name, StockExchange exchange, int minShares, int maxShares, 
                  int minDelayMs, int maxDelayMs, int maxOrders) {
        super(name, exchange, minShares, maxShares, 
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
                
                // Place sell order at current stock price
                SellOrder order = new SellOrder(name, stock, quantity);
                exchange.placeSellOrder(order);
                activeOrderIds.add(order.getOrderId());
                ordersPlaced++;
                
                // Randomly cancel or edit previous orders
                if (!activeOrderIds.isEmpty() && random.nextDouble() < 0.2) { // 20% chance
                    Thread.sleep(random.nextInt(300) + 100);
                    long randomOrderId = activeOrderIds.get(random.nextInt(activeOrderIds.size()));
                    
                    double action = random.nextDouble();
                    if (action < 0.5) {
                        // Cancel order
                        if (exchange.cancelSellOrderById(randomOrderId)) {
                            activeOrderIds.remove(randomOrderId);
                        }
                    } else {
                        // Edit order quantity
                        int newQuantity = minShares + random.nextInt(maxShares - minShares + 1);
                        if (!exchange.editSellOrder(randomOrderId, newQuantity)) {
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
        return "🔴 " + name + " has finished trading";
    }
}
