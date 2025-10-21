package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Central stock exchange that manages all orders and matches buyers with sellers
 */
public class StockExchange {
    private final Map<String, Stock> stocks;
    private final Map<String, PriorityBlockingQueue<BuyOrder>> buyBooks;
    private final Map<String, PriorityBlockingQueue<SellOrder>> sellBooks;
    private final Queue<Transaction> transactionHistory;
    private volatile boolean running;

    public StockExchange() {
        this.stocks = new ConcurrentHashMap<>();
        this.buyBooks = new ConcurrentHashMap<>();
        this.sellBooks = new ConcurrentHashMap<>();
        this.transactionHistory = new ConcurrentLinkedQueue<>();
        this.running = false;
    }

    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol(), stock);
        // Buy books are in descending order (highest price first)
        buyBooks.put(stock.getSymbol(), new PriorityBlockingQueue<>(11, 
            (a, b) -> Double.compare(b.getPricePerShare(), a.getPricePerShare())));
        // Sell books are in ascending order (lowest price first)
        sellBooks.put(stock.getSymbol(), new PriorityBlockingQueue<>(11, 
            Comparator.comparingDouble(SellOrder::getPricePerShare)));
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }

    public List<Stock> getAllStocks() {
        return new ArrayList<>(stocks.values());
    }

    public void placeBuyOrder(BuyOrder order) {
        if (!running) return;
        PriorityBlockingQueue<BuyOrder> buyBook = buyBooks.get(order.getStock().getSymbol());
        if (buyBook != null) {
            buyBook.add(order);
            Logger.logOrderPlaced(order);
            System.out.println("✓ " + order);
            matchOrdersForStock(order.getStock());
        }
    }

    public void placeSellOrder(SellOrder order) {
        if (!running) return;
        PriorityBlockingQueue<SellOrder> sellBook = sellBooks.get(order.getStock().getSymbol());
        if (sellBook != null) {
            sellBook.add(order);
            Logger.logOrderPlaced(order);
            System.out.println("✓ " + order);
            matchOrdersForStock(order.getStock());
        }
    }

    public boolean cancelBuyOrder(BuyOrder order) {
        synchronized (order.getStock().getLock()) {
            PriorityBlockingQueue<BuyOrder> buyBook = buyBooks.get(order.getStock().getSymbol());
            if (buyBook != null && buyBook.remove(order)) {
                order.cancel();
                Logger.logOrderCancelled(order);
                System.out.println("✗ Cancelled: " + order);
                return true;
            }
            return false;
        }
    }

    public boolean cancelSellOrder(SellOrder order) {
        synchronized (order.getStock().getLock()) {
            PriorityBlockingQueue<SellOrder> sellBook = sellBooks.get(order.getStock().getSymbol());
            if (sellBook != null && sellBook.remove(order)) {
                order.cancel();
                Logger.logOrderCancelled(order);
                System.out.println("✗ Cancelled: " + order);
                return true;
            }
            return false;
        }
    }

    /**
     * Thread-safe method to get the current quantity of an order.
     * This ensures thread-safe reading of order state by locking the stock.
     * @param order The order to check
     * @return The current quantity, or -1 if order not found in active orders
     */
    public int getOrderQuantity(Order order) {
        synchronized (order.getStock().getLock()) {
            if (order instanceof BuyOrder) {
                PriorityBlockingQueue<BuyOrder> buyBook = buyBooks.get(order.getStock().getSymbol());
                if (buyBook != null && buyBook.contains(order)) {
                    return order.getQuantity();
                }
            } else if (order instanceof SellOrder) {
                PriorityBlockingQueue<SellOrder> sellBook = sellBooks.get(order.getStock().getSymbol());
                if (sellBook != null && sellBook.contains(order)) {
                    return order.getQuantity();
                }
            }
            return -1;
        }
    }

    /**
     * Match orders for a specific stock only.
     * This allows other threads to trade different stocks concurrently.
     * @param stock The stock to match orders for
     */
    private void matchOrdersForStock(Stock stock) {
        PriorityBlockingQueue<BuyOrder> buyBook = buyBooks.get(stock.getSymbol());
        PriorityBlockingQueue<SellOrder> sellBook = sellBooks.get(stock.getSymbol());
        
        if (buyBook == null || sellBook == null) {
            return;
        }
        
        // Lock only for the actual matching
        synchronized (stock.getLock()) {
            // O(n log n) matching: process queue heads only (highest buy vs lowest sell)
            while (!buyBook.isEmpty() && !sellBook.isEmpty()) {
                BuyOrder buyOrder = buyBook.peek();
                SellOrder sellOrder = sellBook.peek();
                
                if (buyOrder == null || sellOrder == null) break;
                
                // Skip zero-quantity orders
                if (buyOrder.getQuantity() == 0) {
                    buyBook.poll();
                    continue;
                }
                if (sellOrder.getQuantity() == 0) {
                    sellBook.poll();
                    continue;
                }
                
                // Check if orders match (highest buy price >= lowest sell price)
                if (buyOrder.getPricePerShare() < sellOrder.getPricePerShare()) {
                    break;
                }
                
                // Execute trade
                int tradedQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                
                Transaction transaction = new Transaction(buyOrder, sellOrder, tradedQuantity);
                transactionHistory.add(transaction);
                
                buyOrder.reduceQuantity(tradedQuantity);
                sellOrder.reduceQuantity(tradedQuantity);
                
                // Remove fully filled orders from queues
                if (buyOrder.getQuantity() == 0) {
                    buyBook.poll();
                }
                if (sellOrder.getQuantity() == 0) {
                    sellBook.poll();
                }
                
                // Update stock price based on the transaction
                double oldPrice = stock.getCurrentPrice();
                double newPrice = sellOrder.getPricePerShare();
                if (Math.abs(newPrice - oldPrice) > 0.01) {
                    stock.setCurrentPrice(newPrice);
                    Logger.logPriceChange(stock, oldPrice, newPrice);
                }
                
                Logger.logTransaction(transaction);
                System.out.println("★ " + transaction);
            }
        }
    }

    public List<BuyOrder> getBuyOrders() {
        List<BuyOrder> allOrders = new ArrayList<>();
        for (PriorityBlockingQueue<BuyOrder> buyBook : buyBooks.values()) {
            allOrders.addAll(buyBook);
        }
        return allOrders;
    }

    public List<SellOrder> getSellOrders() {
        List<SellOrder> allOrders = new ArrayList<>();
        for (PriorityBlockingQueue<SellOrder> sellBook : sellBooks.values()) {
            allOrders.addAll(sellBook);
        }
        return allOrders;
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
        System.out.println("   Active Buy Orders: " + getBuyOrders().size());
        System.out.println("   Active Sell Orders: " + getSellOrders().size());
        
        System.out.println("\n📈 STOCK PRICES:");
        for (Stock stock : stocks.values()) {
            System.out.println("   " + stock);
        }
        
        List<BuyOrder> allBuyOrders = getBuyOrders();
        if (!allBuyOrders.isEmpty()) {
            System.out.println("\n📋 PENDING BUY ORDERS:");
            for (BuyOrder order : allBuyOrders) {
                System.out.println("   " + order);
            }
        }
        
        List<SellOrder> allSellOrders = getSellOrders();
        if (!allSellOrders.isEmpty()) {
            System.out.println("\n📋 PENDING SELL ORDERS:");
            for (SellOrder order : allSellOrders) {
                System.out.println("   " + order);
            }
        }
        
        System.out.println("\n📁 Logs written to:");
        System.out.println("   - transactions.log");
        System.out.println("   - events.log");
        System.out.println("   - price_changes.log");
    }
}
