package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging events and transactions to files
 */
public class Logger {
    private static final String TRANSACTIONS_FILE = "transactions.log";
    private static final String EVENTS_FILE = "events.log";
    private static final String PRICE_CHANGES_FILE = "price_changes.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static synchronized void logTransaction(Transaction transaction) {
        writeToFile(TRANSACTIONS_FILE, transaction.toString());
    }

    public static synchronized void logEvent(String event) {
        String timestamp = LocalDateTime.now().format(formatter);
        writeToFile(EVENTS_FILE, String.format("[%s] %s", timestamp, event));
    }

    public static synchronized void logPriceChange(Stock stock, double oldPrice, double newPrice) {
        String timestamp = LocalDateTime.now().format(formatter);
        String message = String.format("[%s] PRICE CHANGE: %s from $%.2f to $%.2f (%.2f%%)",
            timestamp, stock.getSymbol(), oldPrice, newPrice, 
            ((newPrice - oldPrice) / oldPrice) * 100);
        writeToFile(PRICE_CHANGES_FILE, message);
    }

    public static synchronized void logOrderPlaced(Order order) {
        String timestamp = LocalDateTime.now().format(formatter);
        String message = String.format("[%s] ORDER PLACED: %s", timestamp, order);
        writeToFile(EVENTS_FILE, message);
    }

    public static synchronized void logOrderCancelled(Order order) {
        String timestamp = LocalDateTime.now().format(formatter);
        String message = String.format("[%s] ORDER CANCELLED: %s", timestamp, order);
        writeToFile(EVENTS_FILE, message);
    }

    public static synchronized void logOrderModified(Order order, int oldQuantity, int newQuantity) {
        String timestamp = LocalDateTime.now().format(formatter);
        String message = String.format("[%s] ORDER MODIFIED: %s - Quantity changed from %d to %d",
            timestamp, order, oldQuantity, newQuantity);
        writeToFile(EVENTS_FILE, message);
    }

    private static void writeToFile(String filename, String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            writer.println(message);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    public static void clearLogs() {
        clearFile(TRANSACTIONS_FILE);
        clearFile(EVENTS_FILE);
        clearFile(PRICE_CHANGES_FILE);
    }

    private static void clearFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, false))) {
            // Just opening in write mode clears the file
        } catch (IOException e) {
            System.err.println("Error clearing log file: " + e.getMessage());
        }
    }
}
