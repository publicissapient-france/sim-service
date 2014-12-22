import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.UUID;

/**
 * Created by Xebia on 12/10/2014.
 */
public class Farm extends Verticle {

    private Integer stock = 0;

    private Integer promisedStock = 0;

    public void start() {
        EventBus eventBus = vertx.eventBus();
        JsonObject config = container.config();

        String id = "farm-" + UUID.randomUUID().toString();
        JsonObject adresses = config.getObject("adresses");
        stock = config.getInteger("initialStock");
        Integer cost = config.getInteger("cost");

        sayHello(eventBus, adresses, id);


        Handler<Message<JsonObject>> requestHandler = message -> {
            container.logger().info("Received a request");
            if (message.body() == null || !(message.body() instanceof JsonObject)) {
                container.logger().warn("Message rejected " + message.body());
                return;
            }
            Integer quantity = message.body().getInteger("quantity");
            if (quantity != null) {
                container.logger().info(("Farm get a message for stock " + quantity));
                JsonObject offer = new JsonObject();
                offer.putString("from", id);
                offer.putString("action", "response");
                Integer availableStock = stock - promisedStock;
                int availableQuantity = Math.min(availableStock, quantity);
                promisedStock += availableQuantity;
                container.logger().info(("Farm reply with " + availableQuantity));
                offer.putNumber("quantity", availableQuantity);
                offer.putNumber("cost", (availableQuantity * cost));
                container.logger().info("stock=" + stock + " promisedStock=" + promisedStock);
                Handler<AsyncResult<Message<JsonObject>>> handlerResponse = response -> {
                    if (response.succeeded()) {
                        container.logger().info("Received reply for an offer");
                        if (response.result().body() == null || !(response.result().body() instanceof JsonObject)) {
                            container.logger().warn("Message rejected " + message.body());
                            promisedStock -= availableQuantity;
                            container.logger().info("stock=" + stock + " promisedStock=" + promisedStock);
                            return;
                        }
                        JsonObject bill = new JsonObject();
                        bill.putString("action", "purchase");
                        bill.putString("from", id);
                        bill.putString("charge", response.result().body().getString("from"));
                        bill.putNumber("quantity", availableQuantity);
                        bill.putNumber("cost", availableQuantity * cost);
                        promisedStock -= availableQuantity;
                        stock -= availableQuantity;
                        container.logger().info("An offer has been accepted: stock=" + stock + " promisedStock=" + promisedStock);
                        eventBus.send(adresses.getString("bank"), bill);
                    } else {
                        container.logger().info("No reply for an offer");
                        promisedStock -= availableQuantity;
                        container.logger().info("stock=" + stock + " promisedStock=" + promisedStock);
                    }
                };
                eventBus.sendWithTimeout(adresses.getString("factory") + message.body().getString("from"), offer, config.getInteger("timeoutReply"), handlerResponse);
            }
        };
        eventBus.registerHandler(adresses.getString("farm"), requestHandler);

        vertx.setPeriodic(config.getInteger("heartBeat"), timerId -> sayHello(eventBus, adresses, id));

        vertx.setPeriodic(config.getInteger("frequency"), timerID1 -> {
            if (stock < config.getInteger("maxStock")) {
                stock++;
                container.logger().info("New stock=" + stock);
            }
        });


    }

    private void sayHello(EventBus eventBus, JsonObject adresses, String id) {
        container.logger().info("Say hello");
        JsonObject hello = new JsonObject();
        hello.putString("action", "hello");
        hello.putString("from", id);
        hello.putString("team", "masters");
        hello.putString("type", "farm");
        hello.putString("version", "1.0");
        eventBus.publish(adresses.getString("city"), hello);
    }
}
