package fr.xebia.vertx.store;

import java.util.HashSet;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 *
 * send periodically an order to all factory with variable quantity at variable state.
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

    /*
     *
     */
    private final Set<Order> pendingOrders = new HashSet<>();

    @Override
    public void start(Future<Void> startedResult) {

        initParams();
        JsonObject hello = buildHelloMessage();

        /*
         * first hello publishing.
         */
        vertx.eventBus().publish("/city", hello);
        startPeriodicOrder();
        listeningForFactoryDelivery();

        // all is started, start periodic hello publishing
        helloTakId = vertx.setPeriodic(conf.getLong("helloRate"), l -> {
            vertx.eventBus().publish("/city", hello);
        });

        // launch successful
        startedResult.setResult(null);
    }

    private void initParams() {
        conf = container.config();
        id = "store-" + UUID.randomUUID().toString();
        orderRate = conf.getLong("orderRate", 1000);
        quantity = conf.getLong("quantity", 10);
        cost = conf.getLong("cost", 10);
    }

    private JsonObject buildHelloMessage() {
        /*
         * hello message
         */
        JsonObject hello = new JsonObject()
                .putString("action", "hello")
                .putString("from", id)
                .putString("team", "masters")
                .putString("type", "store")
                .putString("version", conf.getString("version", "unknown"));
        return hello;
    }

    private void startPeriodicOrder() {
        orderTaskId = vertx.setPeriodic(orderRate, l -> {
            Random r = new Random();
            vertx.setTimer(r.nextInt((int) orderRate / 2), l2 -> {
                Order order = Order.getInstance(quantity + r.nextInt((int) quantity / 2), cost + r.nextInt(
                        (int) cost / 2));
                if (!pendingOrders.contains(order)) {
                    JsonObject orderObject = new JsonObject()
                            .putString("action", "request")
                            .putString("from", id)
                            .putNumber("quantity", order.getQuantity())
                            .putNumber("cost", order.getCost());
                    vertx.eventBus().publish("/city/factory", orderObject);
                    pendingOrders.add(order);
                }
            });
        });
    }

    private void listeningForFactoryDelivery() {
        vertx.eventBus().registerHandler("/city/store/" + id, r -> {
            if (r.body() != null && r.body() instanceof JsonObject) {
                JsonObject delivery = (JsonObject) r.body();
                String action = delivery.getString("action", "unknown");
                String from = delivery.getString("from", "unknown");
                Order order = Order.getInstance(delivery.getLong("quantity", 0), delivery.getLong("cost", 0));
                if (isValidDelivery(action, from, order)) {
                    JsonObject reply = buildAck(order);
                    r.reply(reply);
                    JsonObject sale = buildSale(from, order);
                    vertx.eventBus().send("/city/bank", sale);
                    pendingOrders.remove(order);
                }
            }
        });
    }

    private JsonObject buildSale(String from, Order order) {
        // send sale to bank
        JsonObject sale = new JsonObject()
                .putString("action", "sale")
                .putString("from", id)
                .putString("charge", from)
                .putNumber("quantity", order.getQuantity())
                .putNumber("cost", order.getCost());
        return sale;
    }

    private JsonObject buildAck(Order order) {
        // reply to factory
        JsonObject reply = new JsonObject();
        reply.putString("action", "acquittement")
                .putString("from", id)
                .putNumber("quantity", order.getQuantity());
        return reply;
    }

    private boolean isValidDelivery(String action, String from, Order order) {
        return action.equals("response") && !from.equals("unknown") && pendingOrders.contains(order);
    }

}
