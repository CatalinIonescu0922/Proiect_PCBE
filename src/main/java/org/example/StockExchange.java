package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Central stock exchange that manages all orders and matches buyers with sellers
 */
public class StockExchange {
    private final Map<String, Stock> stocks;
    private final Map<String, LinkedBlockingDeque<BuyOrder>> buyBooks;
    private final Map<String, LinkedBlockingDeque<SellOrder>> sellBooks;
    private final Map<Long, BuyOrder> buyOrdersById;
    private final Map<Long, SellOrder> sellOrdersById;
    private final Queue<Transaction> transactionHistory;
    private volatile boolean running;

    public StockExchange() {
        this.stocks = new ConcurrentHashMap<>();
        this.buyBooks = new ConcurrentHashMap<>();
        this.sellBooks = new ConcurrentHashMap<>();
        this.buyOrdersById = new ConcurrentHashMap<>();
        this.sellOrdersById = new ConcurrentHashMap<>();
        this.transactionHistory = new ConcurrentLinkedQueue<>();
        this.running = false;
    }

    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol(), stock);
        // Buy and sell books maintain chronological order (FIFO)
        buyBooks.put(stock.getSymbol(), new LinkedBlockingDeque<>());
        sellBooks.put(stock.getSymbol(), new LinkedBlockingDeque<>());
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }

    public List<Stock> getAllStocks() {
        return new ArrayList<>(stocks.values());
    }

    public void placeBuyOrder(BuyOrder order) {
        if (!running) return;
        LinkedBlockingDeque<BuyOrder> buyBook = buyBooks.get(order.getStock().getSymbol());
        if (buyBook != null) {
            buyBook.add(order);
            buyOrdersById.put(order.getOrderId(), order);
            Logger.logOrderPlaced(order);
            System.out.println("✓ " + order);
            matchOrdersForStock(order.getStock());
        }
    }

    public void placeSellOrder(SellOrder order) {
        if (!running) return;
        LinkedBlockingDeque<SellOrder> sellBook = sellBooks.get(order.getStock().getSymbol());
        if (sellBook != null) {
            sellBook.add(order);
            sellOrdersById.put(order.getOrderId(), order);
            Logger.logOrderPlaced(order);
            System.out.println("✓ " + order);
            matchOrdersForStock(order.getStock());
        }
    }

    public boolean cancelBuyOrder(BuyOrder order) {
        synchronized (order.getStock().getLock()) {
            LinkedBlockingDeque<BuyOrder> buyBook = buyBooks.get(order.getStock().getSymbol());
            if (buyBook != null && buyBook.remove(order)) {
                buyOrdersById.remove(order.getOrderId());
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
            LinkedBlockingDeque<SellOrder> sellBook = sellBooks.get(order.getStock().getSymbol());
            if (sellBook != null && sellBook.remove(order)) {
                sellOrdersById.remove(order.getOrderId());
                order.cancel();
                Logger.logOrderCancelled(order);
                System.out.println("✗ Cancelled: " + order);
                return true;
            }
            return false;
        }
    }

    public boolean cancelBuyOrderById(long orderId) {
        BuyOrder order = buyOrdersById.get(orderId);
        if (order != null) {
            return cancelBuyOrder(order);
        }
        return false;
    }

    public boolean cancelSellOrderById(long orderId) {
        SellOrder order = sellOrdersById.get(orderId);
        if (order != null) {
            return cancelSellOrder(order);
        }
        return false;
    }

    public boolean editBuyOrder(long orderId, int newQuantity) {
        if (newQuantity <= 0) return false;
        
        BuyOrder order = buyOrdersById.get(orderId);
        if (order == null) return false;
        
        Stock stock = order.getStock();
        LinkedBlockingDeque<BuyOrder> buyBook = buyBooks.get(stock.getSymbol());
        
        if (buyBook != null) {
            synchronized (stock.getLock()) {
                if (buyBook.remove(order)) {
                    int oldQuantity = order.getQuantity();
                    BuyOrder newOrder = new BuyOrder(order.getTraderName(), stock, newQuantity);
                    buyBook.add(newOrder);
                    buyOrdersById.remove(orderId);
                    buyOrdersById.put(newOrder.getOrderId(), newOrder);
                    Logger.logEvent(String.format("Order #%d quantity edited: %d -> %d shares", 
                        orderId, oldQuantity, newQuantity));
                    System.out.println(String.format("✎ Edited: BUY Order #%d - %s: %d -> %d shares",
                        orderId, stock.getSymbol(), oldQuantity, newQuantity));
                    matchOrdersForStock(stock);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean editSellOrder(long orderId, int newQuantity) {
        if (newQuantity <= 0) return false;
        
        SellOrder order = sellOrdersById.get(orderId);
        if (order == null) return false;
        
        Stock stock = order.getStock();
        LinkedBlockingDeque<SellOrder> sellBook = sellBooks.get(stock.getSymbol());
        
        if (sellBook != null) {
            synchronized (stock.getLock()) {
                if (sellBook.remove(order)) {
                    int oldQuantity = order.getQuantity();
                    SellOrder newOrder = new SellOrder(order.getTraderName(), stock, newQuantity);
                    sellBook.add(newOrder);
                    sellOrdersById.remove(orderId);
                    sellOrdersById.put(newOrder.getOrderId(), newOrder);
                    Logger.logEvent(String.format("Order #%d quantity edited: %d -> %d shares", 
                        orderId, oldQuantity, newQuantity));
                    System.out.println(String.format("✎ Edited: SELL Order #%d - %s: %d -> %d shares",
                        orderId, stock.getSymbol(), oldQuantity, newQuantity));
                    matchOrdersForStock(stock);
                    return true;
                }
            }
        }
        return false;
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
                LinkedBlockingDeque<BuyOrder> buyBook = buyBooks.get(order.getStock().getSymbol());
                if (buyBook != null && buyBook.contains(order)) {
                    return order.getQuantity();
                }
            } else if (order instanceof SellOrder) {
                LinkedBlockingDeque<SellOrder> sellBook = sellBooks.get(order.getStock().getSymbol());
                if (sellBook != null && sellBook.contains(order)) {
                    return order.getQuantity();
                }
            }
            return -1;
        }
    }

    /**
     * Match orders for a specific stock only.
     * Orders are matched in chronological order, starting with the latest orders.
     * The stock price is updated after each transaction.
     * @param stock The stock to match orders for
     */
    private void matchOrdersForStock(Stock stock) {
        LinkedBlockingDeque<BuyOrder> buyBook = buyBooks.get(stock.getSymbol());
        LinkedBlockingDeque<SellOrder> sellBook = sellBooks.get(stock.getSymbol());
        
        if (buyBook == null || sellBook == null) {
            return;
        }
        
        // Lock only for the actual matching
        synchronized (stock.getLock()) {
            // Process latest orders first (from the end of the deque)
            while (!buyBook.isEmpty() && !sellBook.isEmpty()) {
                // Get latest orders (most recent)
                BuyOrder buyOrder = buyBook.peekLast();
                SellOrder sellOrder = sellBook.peekLast();
                
                if (buyOrder == null || sellOrder == null) break;
                
                // Skip zero-quantity orders
                if (buyOrder.getQuantity() == 0) {
                    buyBook.pollLast();
                    continue;
                }
                if (sellOrder.getQuantity() == 0) {
                    sellBook.pollLast();
                    continue;
                }
                
                // Execute trade at current stock price
                int tradedQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                
                Transaction transaction = new Transaction(buyOrder, sellOrder, tradedQuantity);
                transactionHistory.add(transaction);
                
                buyOrder.reduceQuantity(tradedQuantity);
                sellOrder.reduceQuantity(tradedQuantity);
                
                // Remove fully filled orders from queues and maps
                if (buyOrder.getQuantity() == 0) {
                    buyBook.pollLast();
                    buyOrdersById.remove(buyOrder.getOrderId());
                }
                if (sellOrder.getQuantity() == 0) {
                    sellBook.pollLast();
                    sellOrdersById.remove(sellOrder.getOrderId());
                }
                
                // Update stock price after the transaction
                // Price moves based on supply/demand: slight random fluctuation
                double oldPrice = stock.getCurrentPrice();
                double priceChange = (Math.random() - 0.5) * 0.02 * oldPrice; // +/- 1% random change
                double newPrice = oldPrice + priceChange;
                newPrice = Math.round(newPrice * 100.0) / 100.0; // Round to 2 decimals
                
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
        for (LinkedBlockingDeque<BuyOrder> buyBook : buyBooks.values()) {
            allOrders.addAll(buyBook);
        }
        return allOrders;
    }

    public List<SellOrder> getSellOrders() {
        List<SellOrder> allOrders = new ArrayList<>();
        for (LinkedBlockingDeque<SellOrder> sellBook : sellBooks.values()) {
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
