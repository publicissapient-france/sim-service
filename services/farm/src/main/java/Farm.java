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

        String id = UUID.randomUUID().toString();
        JsonObject adresses = config.getObject("adresses");
        Integer cost = config.getInteger("cost");

        sayHello(eventBus, adresses, id);


        Handler<Message<JsonObject>> myHandler = message -> {
            Number quantity = message.body().getNumber("quantity");
            if (quantity != null) {
                container.logger().info(("Farm get a message for stock " + quantity));
                JsonObject reply = new JsonObject();
                reply.putString("from", "farm " + id);
                reply.putString("action", "response");
                Integer availableStock = stock - promisedStock;
                int availableQuantity = Math.min(availableStock, quantity.intValue());
                promisedStock += availableStock;
                container.logger().info(("Farm reply with " + availableQuantity));
                reply.putNumber("quantity", availableQuantity);
                reply.putNumber("cost", (availableQuantity * cost));
                message.reply(reply);
            }
        };
        eventBus.registerHandler(adresses.getString("farm"), myHandler);

        long timerID = vertx.setPeriodic(config.getInteger("frequency"), new Handler<Long>() {
            public void handle(Long timerID) {
                stock++;
                container.logger().info("New stock=" + stock);
            }
        });


    }

    private void sayHello(EventBus eventBus, JsonObject jsonObject, String id) {
        JsonObject hello = new JsonObject();
        hello.putString("action", "hello");
        hello.putString("from", "farm "+id);
        hello.putString("type", "farm");
        hello.putString("version", "1.0");
        eventBus.publish(jsonObject.getString("city"), hello);
    }
}
