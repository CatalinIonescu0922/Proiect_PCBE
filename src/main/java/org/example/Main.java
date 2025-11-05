package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Clear previous logs
        Logger.clearLogs();
        
        // Create the stock exchange
        StockExchange exchange = new StockExchange();
        
        // Add real stocks with initial prices
        exchange.addStock(new Stock("AAPL", 178.50));  // Apple
        exchange.addStock(new Stock("GOOGL", 141.80)); // Google
        exchange.addStock(new Stock("MSFT", 378.91));  // Microsoft
        exchange.addStock(new Stock("TSLA", 242.84));  // Tesla
        exchange.addStock(new Stock("AMZN", 178.25));  // Amazon
        exchange.addStock(new Stock("NVDA", 495.22));  // NVIDIA
        exchange.addStock(new Stock("META", 512.32));  // Meta
        exchange.addStock(new Stock("NFLX", 628.73));  // Netflix
        
        // Start the exchange
        exchange.start();
        
        // Create buyer threads with names
        List<Buyer> buyers = new ArrayList<>();
        buyers.add(new Buyer("Buyer-Alice", exchange, 5, 50, 200, 1000, 8));
        buyers.add(new Buyer("Buyer-Bob", exchange, 10, 100, 300, 1200, 7));
        buyers.add(new Buyer("Buyer-Charlie", exchange, 3, 30, 150, 800, 10));
        buyers.add(new Buyer("Buyer-Diana", exchange, 8, 60, 250, 1100, 9));
        buyers.add(new Buyer("Buyer-Ethan", exchange, 15, 80, 400, 1500, 6));
        
        // Create seller threads with names
        List<Seller> sellers = new ArrayList<>();
        sellers.add(new Seller("Seller-Frank", exchange, 5, 50, 250, 1000, 8));
        sellers.add(new Seller("Seller-Grace", exchange, 10, 100, 350, 1200, 7));
        sellers.add(new Seller("Seller-Henry", exchange, 3, 30, 200, 800, 10));
        sellers.add(new Seller("Seller-Ivy", exchange, 8, 60, 300, 1100, 9));
        sellers.add(new Seller("Seller-Jack", exchange, 15, 80, 450, 1500, 6));
        
        List<Future<String>> sellerFutures = new ArrayList<>();
        List<Future<String>> buyersFutures = new ArrayList<>();
        // Start all threads
        for (Buyer buyer : buyers) {
            Future<String> future = executor.submit(buyer);
            buyersFutures.add(future);
        }
        
        for (Seller seller : sellers) {
            Future<String> future = executor.submit(seller);
            sellerFutures.add(future);
        }
        
        // Wait for all threads to complete
        for (Future<String> future : sellerFutures) {
            try {
                String result = future.get();   //Read the output for each seller thread
                System.out.println("Task completed: " + result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        for (Future<String> future : buyersFutures) {
            try {
                String result = future.get();  //Read the output for each buyer thread
                System.out.println("Task completed: " + result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
    }
}