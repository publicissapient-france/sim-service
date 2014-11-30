package fr.xebia.vertx.factory;

import fr.xebia.vertx.factory.message.FactoryMessageBuilder;
import fr.xebia.vertx.factory.message.FactoryMessageAnalyser;
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
    private FactoryMessageAnalyser messageQualificator;

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
        messageQualificator = new FactoryMessageAnalyser();
    }

    private void startListeningStoreOrders() {
        eventBus.registerHandler("/city/factory", order -> {
            if (messageQualificator.isJsonBody(order)) {
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
            if (messageQualificator.isJsonBody(incomingMessage)) {
                JsonObject messageData = (JsonObject) incomingMessage.body();
                if (messageQualificator.isFromFarm(messageData)) {
                    container.logger().info("Factory " + id + " receive ack from farm");
                    incomingMessage.reply(messageBuilder.buildAck(messageData));
                } else if (messageQualificator.isInfoFromBank(messageData)) {
                    handleBankMessage(messageData);
                }
            }
        });
    }

    private void startSendingPeriodicallyRequestToFarms() {
        farmRequetRate = vertx.setPeriodic(config.getLong("farmRequestRate"), l -> {
            eventBus.publish("/city/farm", messageBuilder.buildFarmRequest(id, 10));
            container.logger().info("Factory " + id + " just send a request for resources to all farm.");
        });
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
                break;
        }
    }
    
    private void takeBackPendingOrder() {
        boolean goOn;
        do {
            goOn = !waitingOrder.isEmpty() && acceptOrder(waitingOrder.poll());
        } while (goOn);
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
