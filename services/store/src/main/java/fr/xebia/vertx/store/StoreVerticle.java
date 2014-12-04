package fr.xebia.vertx.store;

import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Random;
import java.util.UUID;

/**
 *
 * send periodically an order to all factory with variable quantity at variable
 * state.
 *
 * @author xebia
 */
public class StoreVerticle extends Verticle {

    /*
     *
     */
    private JsonObject conf;
    private String id;
    private long orderRate;
    private long quantity;
    private long cost;
    /*
     * 
     */
    private long helloTakId;
    private long orderTaskId;

    @Override
    public void start(Future<Void> startedResult) {

        conf = container.config();
        id = "store-" + UUID.randomUUID().toString();
        orderRate = conf.getLong("orderRate", 1000);
        quantity = conf.getLong("quantity", 10);
        cost = conf.getLong("cost", 10);

        /*
         * hello message
         */
        JsonObject hello = new JsonObject();
        hello.putString("action", "hello");
        hello.putString("from", id);
        hello.putString("team", "masters");
        hello.putString("type", "store");
        hello.putString("version", conf.getString("version", "unknown"));
        /*
         * first hello publishing.
         */
        vertx.eventBus().publish("/city", hello);

        /*
         * start peridic order publishing
         */
        orderTaskId = vertx.setPeriodic(orderRate, l -> {
            Random r = new Random();
            vertx.setTimer(r.nextInt((int) orderRate / 2), l2 -> {
                JsonObject order = new JsonObject();
                order.putString("action", "request");
                order.putString("from", id);
                order.putNumber("quantity", quantity + r.nextInt((int) quantity / 2));
                order.putNumber("cost", cost + r.nextInt((int) cost / 2));
                vertx.eventBus().publish("/city/factory", order);
            });
        });
        /*
         * listen for response from factories
         */
        vertx.eventBus().registerHandler("/city/store/" + id, r -> {
            if (r.body() != null && r.body() instanceof JsonObject) {
                JsonObject delivery = (JsonObject) r.body();
                String action = delivery.getString("action", "unknown");
                String from = delivery.getString("from", "unknown");
                Long orderQuantity = delivery.getLong("quantity", 0);
                Long orderCost = delivery.getLong("cost", 0);
                if (action.equals("response") && !from.equals("unknown") && orderQuantity > 0 && orderCost > 0) {
                    // reply to factory
                    JsonObject reply = new JsonObject();
                    reply.putString("action", "acquittement");
                    reply.putString("from", id);
                    reply.putNumber("quantity", orderQuantity);
                    r.reply(reply);
                    // send sale to bank
                    JsonObject sale = new JsonObject();
                    sale.putString("action", "sale");
                    sale.putString("from", id);
                    sale.putString("charge", from);
                    sale.putNumber("quantity", orderQuantity);
                    sale.putNumber("cost", orderCost);
                    vertx.eventBus().send("/city/bank", sale);
                }
            }
        });

        // all is started, start periodic hello publishing
        helloTakId = vertx.setPeriodic(conf.getLong("helloRate"), l -> {
            vertx.eventBus().publish("/city", hello);
        });

        // launch successful
        startedResult.setResult(null);
    }

}
