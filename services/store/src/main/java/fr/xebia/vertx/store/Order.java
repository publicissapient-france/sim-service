/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.xebia.vertx.store;

/**
 *
 * An order.
 *
 * @author xebia
 */
public class Order {

    private final long quantity;
    private final long cost;

    private Order(long quantity, long cost) {
        this.quantity = quantity;
        this.cost = cost;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getCost() {
        return cost;
    }

    public static Order getInstance(long quantity, long cost) {
        return new Order(quantity, cost);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (int) (this.quantity ^ (this.quantity >>> 32));
        hash = 47 * hash + (int) (this.cost ^ (this.cost >>> 32));
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
        return true;
    }



}
