package logic;

import java.util.ArrayList;
import java.util.List;

public class MarketLogic {
    private ArrayList<Order> buyOrders = new ArrayList<>();
    private ArrayList<Order> sellOrders = new ArrayList<>();
    private ArrayList<> transactionHistory = new ArrayList<>();

    public void addOrder(Order order){
        if(order.isBuyer()){
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
        matchOrders();
    }

    public void matchOrders(){
        // logic of trading here
        // and also add to transaction history and update market price
    }
}
