package org.example;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for buy and sell orders
 */
public abstract class Order {
    private static final AtomicLong orderIdGenerator = new AtomicLong(1);
    
    private final long orderId;
    private final String traderName;
    private final Stock stock;
    private volatile int quantity;
    private final LocalDateTime timestamp;
    private volatile boolean cancelled;

    public Order(String traderName, Stock stock, int quantity) {
        this.orderId = orderIdGenerator.getAndIncrement();
        this.traderName = traderName;
        this.stock = stock;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
        this.cancelled = false;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getTraderName() {
        return traderName;
    }

    public Stock getStock() {
        return stock;
    }

    public synchronized int getQuantity() {
        return quantity;
    }

    public synchronized void reduceQuantity(int amount) {
        this.quantity -= amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public abstract String getOrderType();

    @Override
    public String toString() {
        return String.format("%s Order #%d [%s] - %s: %d shares", 
            getOrderType(), orderId, traderName, stock.getSymbol(), quantity);
    }
}
