/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.xebia.vertx.store;

import java.util.UUID;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 *
 * @author Xebia
 */
public class StoreVerticle extends Verticle {

    private long orderRate;
    private int quantity;
    private String id;
    private long periodicId;

    @Override
    public void start(Future<Void> startedResult) {

        orderRate = container.config().getLong("orderRate", 1000);
        quantity = container.config().getInteger("quantity", 10);
        id = "store-" + UUID.randomUUID().toString();

        /*
         * Send periodicaly an order to every factory
         */
        periodicId = vertx.setPeriodic(orderRate, l -> {
            JsonObject order = new JsonObject();
            order.putString("action", "order");
            order.putString("from", id);
            order.putNumber("quantity", quantity);
            vertx.eventBus().publish("city.factory", order);
        });
        /*
         * the starting of the module is successful
         */
        startedResult.setResult(null);
    }

}
