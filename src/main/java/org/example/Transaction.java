package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a completed stock transaction
 */
public class Transaction {
    private final long buyOrderId;
    private final long sellOrderId;
    private final String buyer;
    private final String seller;
    private final Stock stock;
    private final int quantity;
    private final double pricePerShare;
    private final LocalDateTime timestamp;

    public Transaction(BuyOrder buyOrder, SellOrder sellOrder, int quantity) {
        this.buyOrderId = buyOrder.getOrderId();
        this.sellOrderId = sellOrder.getOrderId();
        this.buyer = buyOrder.getTraderName();
        this.seller = sellOrder.getTraderName();
        this.stock = buyOrder.getStock();
        this.quantity = quantity;
        this.pricePerShare = buyOrder.getPricePerShare();
        this.timestamp = LocalDateTime.now();
    }

    public long getBuyOrderId() {
        return buyOrderId;
    }

    public long getSellOrderId() {
        return sellOrderId;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getSeller() {
        return seller;
    }

    public Stock getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }

    public double getTotalValue() {
        return quantity * pricePerShare;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return String.format("[%s] TRANSACTION: %s bought %d shares of %s from %s @ $%.2f/share (Total: $%.2f) [Buy Order #%d, Sell Order #%d]",
            timestamp.format(formatter), buyer, quantity, stock.getSymbol(), seller, 
            pricePerShare, getTotalValue(), buyOrderId, sellOrderId);
    }
}
