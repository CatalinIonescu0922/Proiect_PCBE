package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central stock exchange that manages all orders and matches buyers with sellers
 */
public class StockExchange {
    private final Map<String, Stock> stocks;
    private final List<BuyOrder> buyOrders;
    private final List<SellOrder> sellOrders;
    private final List<Transaction> transactionHistory;
    private volatile boolean running;

    public StockExchange() {
        this.stocks = new ConcurrentHashMap<>();
        this.buyOrders = new CopyOnWriteArrayList<>();
        this.sellOrders = new CopyOnWriteArrayList<>();
        this.transactionHistory = new CopyOnWriteArrayList<>();
        this.running = false;
    }

    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol(), stock);
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }

    public List<Stock> getAllStocks() {
        return new ArrayList<>(stocks.values());
    }

    public synchronized void placeBuyOrder(BuyOrder order) {
        if (!running) return;
        buyOrders.add(order);
        Logger.logOrderPlaced(order);
        System.out.println("✓ " + order);
        matchOrders();
    }

    public synchronized void placeSellOrder(SellOrder order) {
        if (!running) return;
        sellOrders.add(order);
        Logger.logOrderPlaced(order);
        System.out.println("✓ " + order);
        matchOrders();
    }

    public synchronized boolean cancelBuyOrder(BuyOrder order) {
        if (buyOrders.remove(order)) {
            order.cancel();
            Logger.logOrderCancelled(order);
            System.out.println("✗ Cancelled: " + order);
            return true;
        }
        return false;
    }

    public synchronized boolean cancelSellOrder(SellOrder order) {
        if (sellOrders.remove(order)) {
            order.cancel();
            Logger.logOrderCancelled(order);
            System.out.println("✗ Cancelled: " + order);
            return true;
        }
        return false;
    }

    /**
     * Synchronized method to get the current quantity of an order.
     * This ensures thread-safe reading of order state.
     * @param order The order to check
     * @return The current quantity, or -1 if order not found in active orders
     */
    public synchronized int getOrderQuantity(Order order) {
        if (order instanceof BuyOrder) {
            if (buyOrders.contains(order)) {
                return order.getQuantity();
            }
        } else if (order instanceof SellOrder) {
            if (sellOrders.contains(order)) {
                return order.getQuantity();
            }
        }
        return -1; // Order not found or already completed
    }

    private synchronized void matchOrders() {
        List<BuyOrder> toRemoveBuy = new ArrayList<>();
        List<SellOrder> toRemoveSell = new ArrayList<>();
        
        for (BuyOrder buyOrder : buyOrders) {
            if (buyOrder.getQuantity() == 0) {
                toRemoveBuy.add(buyOrder);
                continue;
            }

            for (SellOrder sellOrder : sellOrders) {
                if (sellOrder.getQuantity() == 0) {
                    if (!toRemoveSell.contains(sellOrder)) {
                        toRemoveSell.add(sellOrder);
                    }
                    continue;
                }

                // Check if orders match
                if (buyOrder.getStock().equals(sellOrder.getStock()) &&
                    buyOrder.getPricePerShare() >= sellOrder.getPricePerShare()) {
                    
                    // Execute trade
                    int tradedQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                    
                    Transaction transaction = new Transaction(buyOrder, sellOrder, tradedQuantity);
                    transactionHistory.add(transaction);
                    
                    buyOrder.reduceQuantity(tradedQuantity);
                    sellOrder.reduceQuantity(tradedQuantity);
                    
                    // Update stock price based on the transaction
                    double oldPrice = buyOrder.getStock().getCurrentPrice();
                    double newPrice = sellOrder.getPricePerShare();
                    if (Math.abs(newPrice - oldPrice) > 0.01) {
                        buyOrder.getStock().setCurrentPrice(newPrice);
                        Logger.logPriceChange(buyOrder.getStock(), oldPrice, newPrice);
                    }
                    
                    Logger.logTransaction(transaction);
                    System.out.println("★ " + transaction);
                    
                    if (buyOrder.getQuantity() == 0) {
                        toRemoveBuy.add(buyOrder);
                        break;
                    }
                    
                    if (sellOrder.getQuantity() == 0) {
                        if (!toRemoveSell.contains(sellOrder)) {
                            toRemoveSell.add(sellOrder);
                        }
                    }
                }
            }
        }
        
        // Remove completed orders
        buyOrders.removeAll(toRemoveBuy);
        sellOrders.removeAll(toRemoveSell);
    }

    public List<BuyOrder> getBuyOrders() {
        return new ArrayList<>(buyOrders);
    }

    public List<SellOrder> getSellOrders() {
        return new ArrayList<>(sellOrders);
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    public void start() {
        running = true;
        Logger.logEvent("Stock Exchange STARTED");
        System.out.println("\n" + "=".repeat(80));
        System.out.println("STOCK EXCHANGE SIMULATION STARTED");
        System.out.println("=".repeat(80) + "\n");
    }

    public void stop() {
        running = false;
        Logger.logEvent("Stock Exchange STOPPED");
        System.out.println("\n" + "=".repeat(80));
        System.out.println("STOCK EXCHANGE SIMULATION STOPPED");
        System.out.println("=".repeat(80));
        printSummary();
    }

    public boolean isRunning() {
        return running;
    }

    private void printSummary() {
        System.out.println("\n📊 SIMULATION SUMMARY:");
        System.out.println("   Total Transactions: " + transactionHistory.size());
        System.out.println("   Active Buy Orders: " + buyOrders.size());
        System.out.println("   Active Sell Orders: " + sellOrders.size());
        
        System.out.println("\n📈 STOCK PRICES:");
        for (Stock stock : stocks.values()) {
            System.out.println("   " + stock);
        }
        
        if (!buyOrders.isEmpty()) {
            System.out.println("\n📋 PENDING BUY ORDERS:");
            for (BuyOrder order : buyOrders) {
                System.out.println("   " + order);
            }
        }
        
        if (!sellOrders.isEmpty()) {
            System.out.println("\n📋 PENDING SELL ORDERS:");
            for (SellOrder order : sellOrders) {
                System.out.println("   " + order);
            }
        }
        
        System.out.println("\n📁 Logs written to:");
        System.out.println("   - transactions.log");
        System.out.println("   - events.log");
        System.out.println("   - price_changes.log");
    }
}
