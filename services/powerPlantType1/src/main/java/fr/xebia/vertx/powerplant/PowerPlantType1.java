package fr.xebia.vertx.powerplant;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Xebia on 12/10/2014.
 */
public class PowerPlantType1 extends Verticle {

    private AtomicInteger stock = new AtomicInteger();

    public void start() {
        EventBus eventBus = vertx.eventBus();
        JsonObject config = container.config();

        Handler<Message<JsonObject>> myHandler = message -> {
            Number quantity = message.body().getNumber("quantity");
            container.logger().info(("PowerPlant get a message for stock " + quantity));
            JsonObject reply = new JsonObject();
            reply.putString("from", "powerPlantType1");
            reply.putString("action", "response");
            if (quantity.intValue() <= stock.intValue()) {
                stock.addAndGet(quantity.intValue() * -1);
                container.logger().info("Stock is ok, remains " + stock.get());
                reply.putNumber("quantity", quantity);
                reply.putNumber("cost", (quantity.intValue() * 10));
            } else {
                reply.putNumber("quantity", 0);
                reply.putNumber("cost", 0);
            }

            message.reply(reply);
        };
        eventBus.registerHandler(config.getString("powerPlant.address"), myHandler);

        long timerID = vertx.setPeriodic(1000, new Handler<Long>() {
            public void handle(Long timerID) {
                stock.addAndGet(1);
                container.logger().info("New stock=" + stock);
            }
        });


    }

}
