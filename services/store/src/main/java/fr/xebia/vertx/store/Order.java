/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.xebia.vertx.store;

import java.util.Objects;

/**
 *
 * An order.
 *
 * @author xebia
 */
public class Order {

    private final long quantity;
    private final long cost;
    private final String orderID;

    private Order(long quantity, long cost, String orderID) {
        this.quantity = quantity;
        this.cost = cost;
        this.orderID = orderID;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getCost() {
        return cost;
    }

    public static Order getInstance(long quantity, long cost, String orderID) {
        return new Order(quantity, cost, orderID);
    }

    public String getOrderID() {
        return orderID;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (int) (this.quantity ^ (this.quantity >>> 32));
        hash = 59 * hash + (int) (this.cost ^ (this.cost >>> 32));
        hash = 59 * hash + Objects.hashCode(this.orderID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Order other = (Order) obj;
        if (this.quantity != other.quantity) {
            return false;
        }
        if (this.cost != other.cost) {
            return false;
        }
        if (!Objects.equals(this.orderID, other.orderID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Order{" + "quantity=" + quantity + ", cost=" + cost + ", orderID=" + orderID + '}';
    }

}
