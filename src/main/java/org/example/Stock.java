package org.example;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a stock with a symbol/name and current market price
 */
public class Stock {
    private final String symbol;
    private final AtomicReference<Double> currentPrice;

    public Stock(String symbol, double initialPrice) {
        this.symbol = symbol;
        this.currentPrice = new AtomicReference<>(initialPrice);
    }

    public String getSymbol() {
        return symbol;
    }

    public double getCurrentPrice() {
        return currentPrice.get();
    }

    public void setCurrentPrice(double newPrice) {
        this.currentPrice.set(newPrice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return Objects.equals(symbol, stock.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f)", symbol, currentPrice.get());
    }
}
