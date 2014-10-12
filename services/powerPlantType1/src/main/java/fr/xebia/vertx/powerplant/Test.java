package fr.xebia.vertx.powerplant;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created by Xebia on 12/10/2014.
 */
public class Test extends Verticle {

    public void start() {
        final EventBus eventBus = vertx.eventBus();
        JsonObject config = container.config();

        container.deployVerticle(config.getString("pathToVerticle"), config, (event) -> {
            container.logger().debug(event.succeeded());
            try {
                Thread.currentThread().sleep(1000l);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            JsonObject request = new JsonObject();
            request.putString("from", "testFactory");
            request.putString("action", "request");
            request.putNumber("quantity", 10);
            Handler<Message<JsonObject>> handler = message -> container.logger().info(message.body());
            eventBus.send(config.getString("powerPlant.address"), request, handler);
        });
    }
}
