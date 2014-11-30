package fr.xebia.vertx.factory;

import fr.xebia.vertx.factory.message.FactoryMessageBuilder;
import fr.xebia.vertx.factory.message.FactoryMessageQualificator;
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
    private JsonObject config;
    private FactoryMessageBuilder messageBuilder;
    private FactoryMessageQualificator messageQualificator;

    @Override
    public void start() {

        initInstanceFields();
        startListeningStoreOrders();
        startPeriodicHello();
        startListeningOnPrivateFactoryChannel();
        startSendingPeriodicallyRequestToFarms();

    }

    private void initInstanceFields() {
        eventBus = vertx.eventBus();
        waitingOrder = new LinkedList<>();
        id = "store-" + UUID.randomUUID().toString();
        config = container.config();
        messageBuilder = new FactoryMessageBuilder(eventBus, id);
        messageQualificator = new FactoryMessageQualificator();
    }

    private void startListeningStoreOrders() {
        eventBus.registerHandler("/city/factory", order -> {
            if (order.body() != null && order.body() instanceof JsonObject) {
                container.logger().info("Factory " + id + " receive an order");
                if (messageQualificator.validateOrder((JsonObject) order.body())) {
                    acceptOrder((JsonObject) order.body());
                }
            }
        });
    }

    private void startPeriodicHello() {
        helloTakId = vertx.setPeriodic(config.getLong("helloRate"), l -> {
            eventBus.publish("/city", messageBuilder.buildHelloMessage(id, config.getString("version", "unknown")));
            container.logger().info("Factory " + id + " just send hello to every one !");
        });
    }

    private void startListeningOnPrivateFactoryChannel() {
        eventBus.registerHandler("/city/factory/" + id, incomingMessage -> {
            if (incomingMessage.body() != null && incomingMessage.body() instanceof JsonObject) {
                JsonObject messageData = (JsonObject) incomingMessage.body();
                if (messageQualificator.isFromFarm(messageData)) {
                    container.logger().info("Factory " + id + " receive some resources");
                    incomingMessage.reply(messageBuilder.buildAck(messageData));
                } else if (messageQualificator.isInfoFromBank(messageData)) {
                    handleBankMessage(messageData);
                }
            }
        });
    }

    private void startSendingPeriodicallyRequestToFarms() {
        JsonObject farmRequest = new JsonObject().putString("action", "request").putString("from", id).putNumber(
                "quantity",
                10);
        farmRequetRate = vertx.setPeriodic(config.getLong("farmRequestRate"), l -> {
            eventBus.publish("/city/farm", farmRequest);
            container.logger().info("Factory " + id + " just send a request for resources to all farm !");
        });
    }
    
    private void takeBackPendingOrder() {
        boolean goOn;
        do {
            goOn = !waitingOrder.isEmpty() && acceptOrder(waitingOrder.poll());
        } while (goOn);
    }

    private void handleBankMessage(JsonObject bankMessage) {
        int quantity = bankMessage.getNumber("quantity").intValue();
        switch (bankMessage.getString("action")) {
            case "purchase":
                stock += quantity;
                takeBackPendingOrder();
                break;
            case "sale":
                quantitySalled += quantity;
                stock -= quantity;
                break;
            case "Cost": //todo : define behavior;
                break;
            default:
                container.logger().info("Unknow action in a message from the bank : " + bankMessage.getString("action"));
                ;
        }
    }

    private boolean acceptOrder(JsonObject storeOrder) {
        boolean res;
        if (checkStock(storeOrder)) {
            res = true;
            String replyAdress = "/city/store/" + storeOrder.getString("from");
            eventBus.send(replyAdress, messageBuilder.buildOrderResponse(storeOrder));
        } else {
            res = false;
            waitingOrder.offer(storeOrder);
        }
        return res;
    }

    private boolean checkStock(JsonObject order) {
        return order.getNumber("quantity").longValue() <= stock;
    }
}
