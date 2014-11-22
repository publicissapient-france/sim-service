package fr.xebia.vertx.factory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created by Xebia on 12/10/2014.
 */
public class FactoryVerticle extends Verticle {

    private String id;
    /*
     *
     */
    private long quantitySalled;
    private long stock;
    private Queue<JsonObject> waitingOrder;
    /*
     * 
     */
    private long helloTakId;
    private long farmRequetRate;
    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        waitingOrder = new LinkedList<>();
        id = "store-" + UUID.randomUUID().toString();
        JsonObject config = container.config();

        /**
         * Start listening for store orders
         */
        eventBus.registerHandler("/city/factory", order -> {
            if (order.body() != null && order.body() instanceof JsonObject) {
                container.logger().info("Factory " + id + " receive an order");
                JsonObject storeOrder = (JsonObject) order.body();
                if (validateOrder(storeOrder)) {
                    acceptOrder(storeOrder);
                }
            }
        });

        /*
         * hello message
         */
        JsonObject hello = new JsonObject().putString("action", "hello").putString("team", "master").putString("from",
                id).putString("type", "factory").putString("version", config.getString("version", "unknown"));

        //start periodic hello publishing
        helloTakId = vertx.setPeriodic(config.getLong("helloRate"), l -> {
            eventBus.publish("/city", hello);
            container.logger().info("Factory " + id + " just send hello to every one !");
        });

        /**
         * Start listening for farm responses
         */
        eventBus.registerHandler("/city/factory/" + id, incomingMessage -> {
            if (incomingMessage.body() != null && incomingMessage.body() instanceof JsonObject) {
                JsonObject messageData = (JsonObject) incomingMessage.body();
                if (isResourceFromFarm(messageData)) {
                    container.logger().info("Factory " + id + " receive some resources");
                    incomingMessage.reply(buildAck(messageData));
                    stock += messageData.getNumber("quantity").longValue();
                    while (!waitingOrder.isEmpty() && acceptOrder(waitingOrder.poll())) {
                    }
                } else if (isInfoFromBank(messageData)) {
                    // TODO what to do with this info ?
                }
            }
        });

        /*
         * Start sending periodically farm request
         */
        JsonObject farmRequest = new JsonObject().putString("action", "request").putString("from", id).putNumber(
                "quantity",
                10);
        farmRequetRate = vertx.setPeriodic(config.getLong("farmRequestRate"), l -> {
            eventBus.publish("/city/farm", farmRequest);
            container.logger().info("Factory " + id + " just send a request for resources to all farm !");
        });
    }

    private boolean acceptOrder(JsonObject storeOrder) {
        boolean res;
        if (checkStock(storeOrder)) {
            res = true;
            String replyAdress = "/city/store/" + storeOrder.getString("from");
            eventBus.sendWithTimeout(replyAdress, buildOrderResponse(storeOrder), 5000, ack -> {
                if (ack.result().body() != null && ack.result().body() instanceof JsonObject) {
                    JsonObject ackData = (JsonObject) ack.result().body();
                    if (validateAck(ackData)) {
                        stock -= ackData.getNumber("quantity").longValue();
                        quantitySalled += ackData.getNumber("quantity").longValue();
                    }
                }
            });
        } else {
            res = false;
            waitingOrder.offer(storeOrder);
        }
        return res;
    }

    private boolean checkStock(JsonObject order) {
        return order.getNumber("quantity").longValue() <= stock;
    }

    private boolean validateOrder(JsonObject order) {
        return order.getString("action") != null
                && order.getString("action").equals("request")
                && order.getString("from") != null
                && order.getNumber("quantity") != null
                && order.getNumber("cost") != null;
    }

    private JsonObject buildOrderResponse(JsonObject request) {
        return new JsonObject().putString("action", "response")
                .putString("from", id)
                .putNumber("quantity", request.getNumber("quantity"))
                .putNumber("cost", request.getNumber("cost"));
    }
    
    private JsonObject buildAck(JsonObject messageData){
        return new JsonObject().putString("action", "acquittement")
                            .putString("from", id)
                            .putNumber("quantity", messageData.getNumber("quantity"));
    }

    private boolean validateAck(JsonObject ack) {
        return ack.getString("action") != null
                && ack.getString("action").equals("acquittement")
                && ack.getNumber("quantity") != null;
    }

    private boolean isResourceFromFarm(JsonObject data) {
        return data.getString("action") != null
                && data.getString("action").equals("response")
                && data.getNumber("quantity") != null
                && data.getNumber("cost") != null;
    }

    private boolean isInfoFromBank(JsonObject data) {
        return data.getString("action") != null
                && (data.getString("action").equals("purchase")
                    || data.getString("action").equals("sale")
                    || data.getString("action").equals("cost"))
                && data.getString("from") != null
                && data.getNumber("quantity") != null
                && data.getNumber("cost") != null;
    }
}
