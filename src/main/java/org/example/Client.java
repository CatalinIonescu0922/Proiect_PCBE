package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

abstract public class Client{
    protected final String name;
    protected final StockExchange exchange;
    protected final Random random;
    protected final int minShares;
    protected final int maxShares;
    protected final int minDelayMs;
    protected final int maxDelayMs;
    protected final int maxOrders;
    protected final List<Stock> availableStocks;
    protected final List<Long> activeOrderIds;

    public Client(String name, StockExchange exchange, int minShares, int maxShares, 
                 int minDelayMs, int maxDelayMs, int maxOrders){
        this.name = name;
        this.exchange = exchange;
        this.random = new Random();
        this.minShares = minShares;
        this.maxShares = maxShares;
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.maxOrders = maxOrders;
        this.availableStocks = exchange.getAllStocks();
        this.activeOrderIds = new ArrayList<>();
    }

}