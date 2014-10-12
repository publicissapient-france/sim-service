package fr.xebia.vertx.factory;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Xebia on 12/10/2014.
 */
public class Factory extends Verticle {

    private AtomicInteger stock = new AtomicInteger();

    public void start() {
        EventBus eventBus = vertx.eventBus();

        JsonObject config = container.config();

        JsonObject request = new JsonObject();
        request.putString("from", "testFactory");
        request.putString("action", "request");
        request.putNumber("quantity", 10);
        Handler<Message<JsonObject>> handler = message -> container.logger().info(message.body());
        eventBus.send(config.getString("powerPlant.address"), request, handler);
    }
}
